package ca.mcgill.ecse321.gameorganizer.controllers;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.services.AuthenticationService;

/**
 * Controller to handle authentication-related endpoints.
 * Provides endpoints for login, logout, and password reset.
 * 
 * @author Shine111111
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Endpoint for user login.
     * 
     * @param authenticationDTO the authentication data transfer object containing email and password
     * @param session the HTTP session
     * @return a ResponseEntity containing the LoginResponse if login is successful, or an error message if login fails
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody AuthenticationDTO authenticationDTO, HttpSession session) {
        Account user = authenticationService.login(authenticationDTO, session);
        return ResponseEntity.ok(new LoginResponse(user.getId(), user.getEmail()));
    }

    /**
     * Endpoint for user logout.
     * 
     * @param session the HTTP session
     * @return a ResponseEntity indicating that the user has been logged out successfully
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        return ResponseEntity.ok(authenticationService.logout(session));
    }

    /**
     * Endpoint for resetting the user's password.
     * 
     * @param email the user's email
     * @param newPassword the new password to set
     * @return a ResponseEntity indicating that the password has been updated successfully, or an error message if the email is not found
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email, @RequestParam String newPassword) {
        return ResponseEntity.ok(authenticationService.resetPassword(email, newPassword));
    }
}