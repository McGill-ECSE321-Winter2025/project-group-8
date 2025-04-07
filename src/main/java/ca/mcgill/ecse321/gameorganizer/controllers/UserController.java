package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.GrantedAuthority;

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
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AccountRepository accountRepository;

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
        } else {
            System.out.println("UserController: Authentication is null in SecurityContextHolder");
        }
        
        if (authentication == null || !authentication.isAuthenticated() || 
            authentication.getName().equals("anonymousUser")) {
            System.out.println("UserController: User is not authenticated or anonymous");
            
            // Set isAuthenticated cookie to false to reflect the actual state
            Cookie isAuthCookie = new Cookie("isAuthenticated", "false");
            isAuthCookie.setPath("/");
            isAuthCookie.setMaxAge(0); // Expire immediately
            response.addCookie(isAuthCookie);
            
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
            
            // Set or refresh the isAuthenticated cookie to ensure frontend state consistency
            Cookie isAuthCookie = new Cookie("isAuthenticated", "true");
            isAuthCookie.setPath("/");
            isAuthCookie.setMaxAge(24 * 3600); // 24 hours 
            response.addCookie(isAuthCookie);
            
            // Return with explicit content type
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(userSummary);
        } catch (Exception e) {
            System.err.println("UserController: Error retrieving current user: " + e.getMessage());
            e.printStackTrace();
            
            // Set isAuthenticated cookie to false on error
            Cookie isAuthCookie = new Cookie("isAuthenticated", "false");
            isAuthCookie.setPath("/");
            isAuthCookie.setMaxAge(0); // Expire immediately
            response.addCookie(isAuthCookie);
            
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

    /**
     * Search for a user by exact email address.
     * 
     * @param email The email to search for
     * @return UserSummaryDto containing user information if found
     */
    @GetMapping("/search/{email}")
    public ResponseEntity<?> searchUserByEmail(@PathVariable String email) {
        try {
            // Find account by email
            Account account = accountRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User with email " + email + " does not exist"));
            
            // Check if the account is a GameOwner
            boolean isGameOwner = account instanceof GameOwner;
            
            // Create and return user summary with details
            UserSummaryDto userSummary = new UserSummaryDto(
                account.getId(), 
                account.getName(), 
                account.getEmail(), 
                isGameOwner
            );
            
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(userSummary);
        } catch (IllegalArgumentException e) {
            // User not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("User not found: " + e.getMessage());
        } catch (Exception e) {
            // Other errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching for user: " + e.getMessage());
        }
    }
} 