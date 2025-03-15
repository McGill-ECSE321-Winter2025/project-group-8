package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

/**
 * Service to handle authentication-related operations.
 * Provides methods for login, logout, and password reset.
 * 
 * @autho Shine111111
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
            return accountOpt.get();
        } else {
            throw new InvalidCredentialsException();
        }
    }

    /**
     * Logs out a user by invalidating their session but not too sure if it works.
     * 
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
     * @return a message indicating that the password has been updated successfully, or an error message if the email is not found
     */
    @Transactional
    public String resetPassword(String email, String newPassword) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            return "Password updated successfully";
        } else {
            return "Email not found";
        }
    }
}
