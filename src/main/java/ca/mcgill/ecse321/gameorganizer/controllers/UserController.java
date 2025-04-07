package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.mcgill.ecse321.gameorganizer.dto.response.UserSummaryDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.stream.Collectors;

/**
 * Controller for user-related endpoints.
 * Provides API endpoints for retrieving user information.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private AccountRepository accountRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    /**
     * Get the profile of the currently authenticated user.
     * 
     * @return A UserSummaryDto containing the user's basic information
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        // Log request details for debugging
        System.out.println("UserController: /users/me request received");
        System.out.println("UserController: Request cookies: " + formatCookies(request));
        
        // Check if the request has X-Remember-Me header
        boolean rememberMe = "true".equalsIgnoreCase(request.getHeader("X-Remember-Me"));
        
        // Get authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // Log authentication details
        if (authentication != null) {
            System.out.println("UserController: Authentication principal: " + authentication.getPrincipal());
            System.out.println("UserController: Authentication name: " + authentication.getName());
            System.out.println("UserController: Authentication authorities: " + 
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", ")));
            System.out.println("UserController: Authentication is authenticated: " + authentication.isAuthenticated());
            System.out.println("UserController: Remember Me header: " + rememberMe);
        } else {
            System.out.println("UserController: Authentication is null in SecurityContextHolder");
        }
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            System.out.println("UserController: User is not authenticated or anonymous");
            
            // Clear isAuthenticated cookie using ResponseCookie for better browser compatibility
            ResponseCookie clearIsAuthenticatedCookie = ResponseCookie.from("isAuthenticated", "false")
                .httpOnly(false) // Allow JS access
                .secure(false) // false for local dev
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax") // Use Lax for better functionality
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, clearIsAuthenticatedCookie.toString());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        try {
            // Get email from authentication principal
            String email = authentication.getName();
            
            System.out.println("UserController: Getting current user for email: " + email);
            
            // Find account by email
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in repository: " + email));
            
            // Check if the account is a GameOwner
            boolean isGameOwner = account instanceof GameOwner;
            
            // Create and return user summary with email and gameOwner status
            UserSummaryDto userSummary = new UserSummaryDto(account.getId(), account.getName(), account.getEmail(), isGameOwner);
            
            System.out.println("UserController: Found user: " + userSummary.getId() + ", " + userSummary.getName() + 
                              ", " + userSummary.getEmail() + ", isGameOwner: " + userSummary.isGameOwner());
            
            // Set cookie maxAge based on rememberMe flag
            int cookieMaxAge = rememberMe 
                ? 30 * 24 * 3600  // 30 days in seconds (if rememberMe is true)
                : -1;      // Session cookie (expires when browser closes)
                
            System.out.println("UserController: Setting isAuthenticated cookie max age to: " + cookieMaxAge + 
                " seconds. Remember me: " + rememberMe);
            
            // Set or refresh the isAuthenticated cookie using ResponseCookie for better browser compatibility
            ResponseCookie.ResponseCookieBuilder isAuthenticatedBuilder = ResponseCookie.from("isAuthenticated", "true")
                .httpOnly(false) // Allow JS access
                .secure(false) // false for local dev
                .path("/")
                .sameSite("Lax"); // Use Lax for better functionality
                
            // Apply maxAge only if rememberMe is true
            if (rememberMe) {
                isAuthenticatedBuilder.maxAge(cookieMaxAge);
            } // otherwise it's a session cookie (expires when browser closes)
            
            ResponseCookie isAuthenticatedCookie = isAuthenticatedBuilder.build();
            response.addHeader(HttpHeaders.SET_COOKIE, isAuthenticatedCookie.toString());
            
            // Return with explicit content type
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(userSummary);
        } catch (Exception e) {
            System.err.println("UserController: Error retrieving current user: " + e.getMessage());
            e.printStackTrace();
            
            // Clear isAuthenticated cookie using ResponseCookie
            ResponseCookie clearIsAuthenticatedCookie = ResponseCookie.from("isAuthenticated", "false")
                .httpOnly(false) // Allow JS access
                .secure(false) // false for local dev
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax") // Use Lax for better functionality
                .build();
            response.addHeader(HttpHeaders.SET_COOKIE, clearIsAuthenticatedCookie.toString());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Helper method to format cookies for logging
     */
    private String formatCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return "No cookies";
        
        StringBuilder sb = new StringBuilder();
        for (Cookie cookie : cookies) {
            // Don't log access token values for security
            String value = cookie.getName().equals("accessToken") ? 
                "[REDACTED]" : cookie.getValue();
            sb.append(cookie.getName()).append("=").append(value).append("; ");
        }
        return sb.toString();
    }
} 