package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.dto.response.UserSummaryDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.RegistrationService;
import ca.mcgill.ecse321.gameorganizer.dto.response.RegistrationResponseDto;

import java.util.List;

/**
 * Controller for user-related endpoints.
 * Provides API endpoints for retrieving user information.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private RegistrationService registrationService;

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
            
            // Get user's registered events
            List<RegistrationResponseDto> registrations = registrationService.getAllRegistrationsByUserEmail(email);
            
            // Create and return user summary with details including events
            UserSummaryDto userSummary = new UserSummaryDto(
                account.getId(), 
                account.getName(), 
                account.getEmail(), 
                isGameOwner
            );
            
            // Add the events to the user summary, ensuring each event has complete information
            userSummary.setEvents(registrations);
            
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
            System.err.println("Error in searchUserByEmail: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error searching for user: " + e.getMessage());
        }
    }
} 