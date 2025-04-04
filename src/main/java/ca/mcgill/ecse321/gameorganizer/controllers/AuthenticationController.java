package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.exceptions.EmailNotFoundException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidPasswordException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.security.JwtUtil;
import ca.mcgill.ecse321.gameorganizer.services.AuthenticationService;
import jakarta.servlet.http.HttpSession;

/**
 * Controller to handle authentication-related endpoints.
 * Provides endpoints for login, logout, and password reset.
 *
 * @author Shine111111
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    // Keep AuthenticationService for logout/reset password for now, but not for login
    @Autowired
    private AuthenticationService authenticationService; 

    @Autowired // Inject AuthenticationManager
    private AuthenticationManager authenticationManager;

    @Autowired // Inject AccountRepository to get ID after successful auth
    private AccountRepository accountRepository;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint for user login.
     *
     * @param authenticationDTO the authentication data transfer object containing email and password
     * @return a ResponseEntity containing the JwtAuthenticationResponse if login is successful, or an error message if login fails
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationDTO authenticationDTO) {
        try {
            // Validate input fields
            if (authenticationDTO.getEmail() == null || authenticationDTO.getEmail().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Email is missing
            }
            if (authenticationDTO.getPassword() == null || authenticationDTO.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Password is missing
            }

            // Create authentication token and authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationDTO.getEmail(), authenticationDTO.getPassword()));

            // Set the successful authentication in the SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Find the user account
            Account user = accountRepository.findByEmail(authenticationDTO.getEmail())
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in repository: " + authenticationDTO.getEmail()));

            // Generate JWT token
            String jwt = jwtUtil.generateToken(user.getEmail());

            // Return the token and user info
            return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, user.getId(), user.getEmail()));
        } catch (BadCredentialsException e) {
            // Return 401 UNAUTHORIZED when credentials are invalid
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException e) {
            // Other authentication issues (e.g., user disabled, locked - depends on UserDetails implementation)
            System.err.println("Authentication failed: " + e.getMessage()); // Log other auth errors
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            // Catch unexpected errors during login
            System.err.println("Unexpected error during login: " + e.getMessage());
            e.printStackTrace(); // Log stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint for user logout.
     *
     * @return a ResponseEntity indicating that the user has been logged out successfully
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // With JWT, logout is handled client-side by removing the token
        return ResponseEntity.ok("Successfully logged out");
    }

    /**
     * Endpoint for resetting the user's password.
     *
     * @param email the user's email
     * @param newPassword the new password to set
     * @return a ResponseEntity indicating that the password has been updated successfully, or an error message if the email is not found or the new password is invalid
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        try {
            String result = authenticationService.resetPassword(email, newPassword);
            return ResponseEntity.ok(result);
        } catch (EmailNotFoundException | InvalidPasswordException e) {
            // Return 400 BAD REQUEST when the email is not found or password is invalid
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
