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

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.security.core.Authentication;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTH_ATTRIBUTE = "JWT_AUTHENTICATION";
    
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
        Authentication authentication = null;
        
        if (token != null) {
            try {
                // Extract username from token
                String username = jwtUtil.extractUsername(token);
                log.debug("Extracted username from token: {}", username);

                // If we have a username and no existing authentication
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.debug("Loading UserDetails for email: {}", username);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    // If token is valid
                    if (jwtUtil.validateToken(token, userDetails.getUsername())) {
                        log.debug("JWT token is valid for user: {}", username);
                        
                        authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                        
                        // Add details from request
                        ((UsernamePasswordAuthenticationToken) authentication)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Set authentication in context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authentication set in SecurityContextHolder: {}", authentication);
                        
                        // Store in request attributes for later use
                        request.setAttribute(AUTH_ATTRIBUTE, authentication);
                        
                        // Log security context after setting auth
                        log.debug("Security context AFTER filter: {}", SecurityContextHolder.getContext());
                        log.debug("SecurityContextHolder strategy: {}", SecurityContextHolder.getContextHolderStrategy().getClass().getName());
                    }
                }
            } catch (Exception e) {
                log.error("Error processing JWT token", e);
            }
        }
        
        // Create a wrapper for the response to restore authentication after the filter chain
        HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response) {
            @Override
            public void setStatus(int sc) {
                super.setStatus(sc);
                
                // If authentication was lost but we have it stored in request attribute
                // This is a workaround for Spring Security filter chain issue
                if (SecurityContextHolder.getContext().getAuthentication() == null && 
                    request.getAttribute(AUTH_ATTRIBUTE) != null &&
                    request.getAttribute(AUTH_ATTRIBUTE) instanceof Authentication) {
                    
                    Authentication storedAuth = (Authentication) request.getAttribute(AUTH_ATTRIBUTE);
                    log.debug("Restoring authentication from request attribute: {}", storedAuth);
                    SecurityContextHolder.getContext().setAuthentication(storedAuth);
                }
            }
        };
        
        // Continue filter chain with wrapped response
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            // After filter chain completes, check if authentication was lost
            if (SecurityContextHolder.getContext().getAuthentication() == null && 
                request.getAttribute(AUTH_ATTRIBUTE) != null &&
                request.getAttribute(AUTH_ATTRIBUTE) instanceof Authentication) {
                
                log.info("Authentication lost after filter chain. Reapplying from request attribute.");
                Authentication storedAuth = (Authentication) request.getAttribute(AUTH_ATTRIBUTE);
                SecurityContextHolder.getContext().setAuthentication(storedAuth);
            }
            
            // Enhanced logging after completing the chain
            log.debug("Request completed, Security context at end of filter: {}", SecurityContextHolder.getContext());
            log.debug("SecurityContext holder strategy class: {}", SecurityContextHolder.getContextHolderStrategy().getClass().getName());
            log.debug("Current thread name: {}, ID: {}", Thread.currentThread().getName(), Thread.currentThread().getId());
            log.debug("Request attribute AUTH_ATTRIBUTE present: {}", request.getAttribute(AUTH_ATTRIBUTE) != null);
        }
    }
    
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No valid Authorization header found");
            return null;
        }

        // Extract the token
        String token = authHeader.substring(7);
        log.debug("JWT token found in header: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
        return token;
    }
}
