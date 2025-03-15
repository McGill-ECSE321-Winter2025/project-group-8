package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Optional;
import java.util.UUID;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.middleware.RequireUser;
import ca.mcgill.ecse321.gameorganizer.middleware.UserContext;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.security.JwtUtil;

@Service
public class AuthenticationService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserContext userContext;

    @Transactional
    public ResponseEntity<String> login(AuthenticationDTO authenticationDTO, HttpSession session) {
        Optional<Account> accountOpt = accountRepository.findByEmail(authenticationDTO.getEmail());
        if (accountOpt.isPresent() && passwordEncoder.matches(authenticationDTO.getPassword(), accountOpt.get().getPassword())) {
            String token = jwtUtil.generateToken(String.valueOf(accountOpt.get().getId()));
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @Transactional
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Successfully logged out");
    }

    @Transactional
    public ResponseEntity<String> resetPassword(String email) {
        Optional<Account> accountOpt = accountRepository.findByEmail(email);
        if (accountOpt.isPresent()) {
            String token = UUID.randomUUID().toString();
            // Save token to the database associated with the user (not shown here)
            String resetLink = "http://example.com/reset-password?token=" + token;
            // emailService.sendEmail(email, "Password Reset Request", "Click the link to reset your password: " + resetLink);
            return ResponseEntity.ok("Password reset link sent to your email");
        } else {
            return ResponseEntity.ok("If the email exists, a reset link will be sent");
        }
    }

    @Transactional
    public ResponseEntity<String> updatePassword(String token, String newPassword) {
        // Validate token and find associated user (not shown here)
        Account account = findAccountByToken(token);
        if (account != null) {
            account.setPassword(passwordEncoder.encode(newPassword));
            accountRepository.save(account);
            return ResponseEntity.ok("Password updated successfully");
        } else {
            return ResponseEntity.status(400).body("Invalid or expired token");
        }
    }

    private Account findAccountByToken(String token) {
        // Implement token validation and user retrieval logic (not shown here)
        return null;
    }

    @RequireUser
    public ResponseEntity<String> someProtectedMethod() {
        Account currentUser = userContext.getCurrentUser();
        // Perform actions with the authenticated user
        return ResponseEntity.ok("Protected method accessed by " + currentUser.getEmail());
    }
}
