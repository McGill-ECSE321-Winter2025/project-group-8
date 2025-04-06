package ca.mcgill.ecse321.gameorganizer.security;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie; // Import Cookie
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.security.core.Authentication;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ATTRIBUTE = "JWT_AUTHENTICATION";
    
    // Add this ThreadLocal to track authentication across filter chain
    private static final ThreadLocal<Authentication> authenticationThreadLocal = new ThreadLocal<>();
    
    private final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final AccountRepository accountRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
                                  AccountRepository accountRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.accountRepository = accountRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.debug("Processing request: {} {}", request.getMethod(), request.getRequestURI());
        
        // Log the security context before we make any changes
        log.debug("Security context BEFORE filter: {}", SecurityContextHolder.getContext());
        log.debug("Security thread before filter: {}", Thread.currentThread().getName());
        
        // Extract and validate token
        String token = extractTokenFromRequest(request);
        log.debug("Extracted token: {}", token != null ? token.substring(0, Math.min(token.length(), 10)) + "..." : "null");
        
        // Make authentication final to fix linter error
        final Authentication[] authRef = {null};
        
        if (token != null) {
            try {
                // No need to check for "Bearer " prefix when reading from cookie
                // if (token.startsWith("Bearer ")) {
                //     token = token.substring(7).trim();
                //     log.debug("Removed 'Bearer ' prefix from token");
                // }
                
                // Check if token is empty after trimming
                if (token.isEmpty()) {
                    log.warn("Token is empty after removing 'Bearer ' prefix");
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                
                // Extract username from token
                String username = jwtUtil.extractUsername(token);
                log.debug("Extracted username from token: {}", username);

                // If we have a username and no existing authentication
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Loading UserDetails for email: {}", username);
                    UserDetails userDetails;
                    try {
                        userDetails = userDetailsService.loadUserByUsername(username);
                        log.debug("Successfully loaded UserDetails for: {}", username);
                    } catch (Exception e) {
                        log.error("Failed to load user details for token validation: {}", e.getMessage(), e);
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        return;
                    }

                    // If token is valid
                    if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                        log.debug("JWT token is valid for user: {}", username);
                        
                        authRef[0] = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        // Add details from request
                        ((UsernamePasswordAuthenticationToken) authRef[0])
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in context
                        SecurityContextHolder.getContext().setAuthentication(authRef[0]);
                        
                        // Store authentication in ThreadLocal to track changes
                        authenticationThreadLocal.set(authRef[0]);
                        
                        log.debug("Authentication set in SecurityContextHolder: {}", authRef[0]);
                        
                        // Store in request attributes for later use
                        request.setAttribute(AUTH_ATTRIBUTE, authRef[0]);
                        
                        // Log security context after setting auth
                        log.debug("Security context AFTER filter: {}", SecurityContextHolder.getContext());
                        log.debug("SecurityContextHolder strategy: {}", SecurityContextHolder.getContextHolderStrategy().getClass().getName());
                    } else {
                        log.warn("Token validation failed for user: {}", username);
                        if (request.getRequestURI().contains("/account/")) {
                            log.warn("Failed token validation on account endpoint: {}", request.getRequestURI());
                            log.warn("Account access denied. Token: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
                        }
                    }
                } else {
                    // Log why we didn't set authentication
                    if (username == null) {
                        log.warn("No username extracted from token");
                    } else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        log.warn("Authentication already exists in security context: {}", 
                            SecurityContextHolder.getContext().getAuthentication());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing JWT token: {}", e.getMessage(), e);
            }
        } else if (request.getRequestURI().contains("/account/") && request.getMethod().equals("GET")) {
            log.warn("No token provided for authenticated account endpoint: {}", request.getRequestURI());
        }
        
        // Store authentication reference for the lambda
        final Authentication auth = authRef[0];
        
        // Create a wrapper for the response to restore authentication after the filter chain
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
            // When the filter chain finishes, we need to ensure authentication context propagation
            @Override
            public void setStatus(int sc) {
                // If we have a 401 error, and it's an authenticated resource, log for debugging
                if (sc == HttpServletResponse.SC_UNAUTHORIZED && auth != null) {
                    log.warn("Downstream filter set 401 response despite valid authentication");
                    super.setStatus(sc);
                    return;
                }
                
                super.setStatus(sc);
            }
        };
        
        try {
            // Proceed with the filter chain, using our custom response wrapper
            filterChain.doFilter(request, responseWrapper);
        } finally {
            // Check if authentication was modified
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();
            Authentication originalAuth = authenticationThreadLocal.get();
            
            if (originalAuth != null && currentAuth == null) {
                log.error("Authentication was removed during filter chain execution!");
                log.error("Original auth: {}", originalAuth);
                
                // Log current security filters to help debug
                log.error("Request URI: {}", request.getRequestURI());
                log.error("Response status: {}", responseWrapper.getStatus());
            }
            
            // Log security context after the filter chain (for debugging purposes)
            log.debug("Security context AFTER filter chain: {}", SecurityContextHolder.getContext());
            
            // Clear ThreadLocal to prevent memory leaks
            authenticationThreadLocal.remove();
        }
    }
    
    /**
     * Extracts JWT token from the 'accessToken' cookie or Authorization header.
     *
     * @param request The HTTP request
     * @return The extracted token from the cookie/header, or null if not present
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // First try to get from cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    String token = cookie.getValue();
                    log.debug("Found 'accessToken' cookie with value: {}",
                        (token != null && token.length() > 15) ? token.substring(0, 15) + "..." : token);
                    return token;
                }
            }
        }
        
        // If no cookie found, try Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Remove "Bearer " prefix
            log.debug("Found token in Authorization header: {}", 
                (token != null && token.length() > 15) ? token.substring(0, 15) + "..." : token);
            return token;
        }
        
        log.debug("No token found in either cookie or Authorization header");
        return null;
    }
}
