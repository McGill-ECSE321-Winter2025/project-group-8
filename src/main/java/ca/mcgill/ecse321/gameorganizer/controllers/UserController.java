package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.dto.response.UserSummaryDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

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
    public ResponseEntity<?> getCurrentUser() {
        // Get authentication from security context
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
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
            
            System.out.println("UserController: Found user: " + userSummary.getId() + ", " + userSummary.getName() + ", " + userSummary.getEmail() + ", isGameOwner: " + userSummary.isGameOwner());
            
            // Return with explicit content type
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json")
                    .body(userSummary);
        } catch (Exception e) {
            System.err.println("Error retrieving current user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 