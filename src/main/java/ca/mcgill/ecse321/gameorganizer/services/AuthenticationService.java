package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.exceptions.EmailNotFoundException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidPasswordException;
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

    /**
     * Logs in a user by validating their email and password.
     * 
     * @param authenticationDTO the authentication data transfer object containing email and password
     * @param session the HTTP session
     * @return the authenticated Account if login is successful
     * @throws InvalidCredentialsException if the email or password is invalid
     */
    @Transactional
    public Account login(AuthenticationDTO authenticationDTO, HttpSession session) {
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
        session.invalidate();
        return "Successfully logged out";
    }

    /**
     * Resets the user's password.
     * 
     * @param email the user's email
     * @param newPassword the new password to set
     * @return a message indicating that the password has been updated successfully
     * @throws EmailNotFoundException if the email is not found
     * @throws InvalidPasswordException if the new password does not meet the validation criteria
     */
    @Transactional
    public String resetPassword(String email, String newPassword) {
        validatePassword(newPassword);

        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            return "Password updated successfully";
        } else {
            throw new EmailNotFoundException("Email not found");
        }
    }

    /**
     * Validates the new password.
     * 
     * @param password the new password to validate
     * @throws InvalidPasswordException if the password does not meet the validation criteria
     */
    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new InvalidPasswordException("Password must be at least 8 characters long");
        }
        // TODO: Add more validation criteria as needed (e.g., complexity, special characters, etc.)
    }

}
