package ca.mcgill.ecse321.gameorganizer.services;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.PasswordResetDto;
import ca.mcgill.ecse321.gameorganizer.dto.PasswordResetRequestDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.EmailNotFoundException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidPasswordException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidTokenException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import jakarta.servlet.http.HttpSession;

/**
 * Service to handle authentication-related operations.
 * Provides methods for login, logout, and password reset.
 * 
 * @author Shine111111
 */
@Service
public class AuthenticationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final long EXPIRE_TOKEN_AFTER_MINUTES = 15; // Token validity: 15 minutes

    /**
     * Logs in a user by validating their email and password.
     *
     * @param authenticationDTO the authentication data transfer object containing email and password
     * @param session the HTTP session
     * @return the authenticated Account if login is successful
     * @throws InvalidCredentialsException if the email or password is invalid
     */
    public Account login(AuthenticationDTO authenticationDTO, HttpSession session) {
        if (authenticationDTO == null) {
            throw new IllegalArgumentException("Authentication data cannot be null");
        }
        // Optionally, you might also check for null session
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }

        Optional<Account> accountOpt = accountRepository.findByEmail(authenticationDTO.getEmail());
        if (accountOpt.isPresent() && passwordEncoder.matches(authenticationDTO.getPassword(), accountOpt.get().getPassword())) {
            Account account = accountOpt.get();
            session.setAttribute("userId", account.getId());
            return account;
        } else {
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Logs out a user by invalidating their session.
     * TODO: IMPORTANT: Make sure it actually works as intended!!!!
     * @param session the HTTP session
     * @return a message indicating that the user has been logged out successfully
     */
    @Transactional
    public String logout(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        session.invalidate();
        return "Successfully logged out";
    }

    /**
     * Initiates the password reset process for a given email address.
     * Generates a unique token and sets an expiry time.
     *
     * @param requestDto DTO containing the user's email.
     * @throws EmailNotFoundException if the email is not found.
     */
    @Transactional
    public void requestPasswordReset(PasswordResetRequestDto requestDto) {
        if (requestDto == null || requestDto.getEmail() == null || requestDto.getEmail().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        Account account = accountRepository.findByEmail(requestDto.getEmail())
                .orElseThrow(() -> new EmailNotFoundException("Email not found: " + requestDto.getEmail()));

        String token = UUID.randomUUID().toString();
        account.setResetPasswordToken(token);
        account.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(EXPIRE_TOKEN_AFTER_MINUTES));
        accountRepository.save(account);

        // In a real application, you would send an email with the token here.
        // For this task, we just generate and store it.
    }


    /**
     * Performs the password reset using a token.
     *
     * @param resetDto DTO containing the reset token and the new password.
     * @return A success message.
     * @throws InvalidTokenException if the token is invalid, expired, or not found.
     * @throws InvalidPasswordException if the new password is invalid.
     */
    @Transactional
    public String performPasswordReset(PasswordResetDto resetDto) {
        if (resetDto == null || resetDto.getToken() == null || resetDto.getNewPassword() == null) {
            throw new IllegalArgumentException("Reset DTO, token, and new password cannot be null");
        }

        Account account = accountRepository.findByResetPasswordToken(resetDto.getToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid password reset token"));

        if (account.getResetPasswordTokenExpiry() == null || account.getResetPasswordTokenExpiry().isBefore(LocalDateTime.now())) {
            // Clear expired token
            account.setResetPasswordToken(null);
            account.setResetPasswordTokenExpiry(null);
            accountRepository.save(account);
            throw new InvalidTokenException("Password reset token has expired");
        }

        validatePassword(resetDto.getNewPassword());

        account.setPassword(passwordEncoder.encode(resetDto.getNewPassword()));
        account.setResetPasswordToken(null); // Invalidate token after use
        account.setResetPasswordTokenExpiry(null);
        accountRepository.save(account);

        return "Password updated successfully";
    }

    /**
     * Validates the new password.
     * 
     * @param password the new password to validate
     * @throws InvalidPasswordException if the password does not meet the validation criteria
     */
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException("Password cannot be null or empty");
        }
        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }
        // Additional validation criteria can be added here.
    }

}
