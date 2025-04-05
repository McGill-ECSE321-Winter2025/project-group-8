package ca.mcgill.ecse321.gameorganizer.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder; // Import PasswordEncoder
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.AccountResponse;
import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.EventResponse;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException; // Import UnauthedException
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

/**
 * Service class that handles business logic for account management operations.
 * Provides methods for creating, retrieving, updating, and deleting user accounts.
 *
 * @author @dyune
 */
@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class); // Add Logger

    private final AccountRepository accountRepository;
    private final RegistrationRepository registrationRepository;
    private final ReviewRepository reviewRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final PasswordEncoder passwordEncoder; // Add PasswordEncoder field

    // UserContext removed

    @Autowired
    public AccountService(
            AccountRepository accountRepository,
            RegistrationRepository registrationRepository,
            ReviewRepository reviewRepository,
            BorrowRequestRepository borrowRequestRepository,
            PasswordEncoder passwordEncoder) { // Add PasswordEncoder to constructor
        this.accountRepository = accountRepository;
        this.registrationRepository = registrationRepository;
        this.reviewRepository = reviewRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.passwordEncoder = passwordEncoder; // Initialize PasswordEncoder
    }

    /**
     * Loads user-specific data by username (email in this case).
     * Required by UserDetailsService interface for Spring Security authentication.
     *
     * @param email The email address (used as username) of the user to load.
     * @return UserDetails object containing user information.
     * @throws UsernameNotFoundException if the user with the given email is not found.
     */

    /**
     * Creates a new account in the system.
     *
     * @param request The account information with which an account will be created with
     * @return ResponseEntity with creation confirmation message or an error message
     */
    @Transactional
    public ResponseEntity<String> createAccount(CreateAccountRequest request) {
        try {
            // Validate request
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid email address");
            }
            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            // Check for existing email
            if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email address already in use");
            }

            // Check for existing username
            if (accountRepository.findByName(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body("Username already in use");
            }

            // Create and save account with ENCODED password
            Account account;
            String encodedPassword = passwordEncoder.encode(request.getPassword());
            
            if (request.isGameOwner()) {
                account = new GameOwner(request.getUsername(), request.getEmail(), encodedPassword);
            } else {
                account = new Account(request.getUsername(), request.getEmail(), encodedPassword);
            }

            // Log the account creation details for debugging
            System.out.println("Creating account: " + request.getUsername() + ", " + request.getEmail() + ", isGameOwner: " + request.isGameOwner());

            Account savedAccount = accountRepository.save(account);

            if (savedAccount == null || savedAccount.getId() == 0) {
                return ResponseEntity.status(500).body("Failed to create account");
            }

            return ResponseEntity.status(201).body("Account created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating account: " + e.getMessage());
        }
    }

    /**
     * Retrieves an account by email address.
     *
     * @param email The email address of the account to retrieve
     * @return The Account object
     * @throws IllegalArgumentException if no account is found with the given email
     */
    @Transactional
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
    }

    /**
     * Retrieves user information to display (name, account type, and events registered in)
     *
     * @param email The email of the account info to display
     * @return ResponseEntity with the information as a body or a Bad Request if no such account exists
     */
    @Transactional
    public ResponseEntity<?> getAccountInfoByEmail(String email) {
        Account account;
        try {
            account = getAccountByEmail(email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Bad request: no such account exists.");
        }
        // Retrieve the name; if null or empty, fallback to email.
        String accountName = account.getName();
        if (accountName == null || accountName.trim().isEmpty()) {
            accountName = account.getEmail();
        }
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(accountName);
        boolean isGameOwner = account instanceof GameOwner;
        List<EventResponse> events = new ArrayList<>();
        for (Registration registration : registrations) {
            Event event = registration.getEventRegisteredFor();
            events.add(new EventResponse(event));
        }
        AccountResponse response = new AccountResponse(accountName, events, isGameOwner);
        return ResponseEntity.ok(response);
    }


    /**
     * Retrieves an account by its unique identifier.
     *
     * @param id The ID of the account to retrieve
     * @return The Account object
     * @throws IllegalArgumentException if no account is found with the given ID
     */
    @Transactional
    public Account getAccountById(int id) {
        return accountRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Account with ID " + id + " does not exist")
        );
    }

    /**
     * Updates an existing account's information.
     *
     * @param request DTO with the email to identify the account to update, old password to authenticate this action,
     *                new password in case they want to change password, and new username.
     * @return ResponseEntity with update confirmation message or failure message
     */
    @Transactional
    public ResponseEntity<String> updateAccount(UpdateAccountRequest request) {
        String email = request.getEmail();
        String newUsername = request.getUsername();
        String password = request.getPassword();
        String newPassword = request.getNewPassword();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            // Use exception consistently instead of returning ResponseEntity for auth errors
            throw new UnauthedException("User must be authenticated to update account.");
        }

        String currentUsername;
        Object principal = authentication.getPrincipal();
        // Assuming the principal is the email used for login, as is common with UserDetails
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
             currentUsername = (String) principal;
        }
        else {
            // Handle unexpected principal type
             throw new UnauthedException("Unexpected principal type in SecurityContext.");
        }


        Account currentUser = accountRepository.findByEmail(currentUsername).orElseThrow(
                () -> new UnauthedException("Authenticated user '" + currentUsername + "' not found in database.") // Should ideally not happen if JWT/session is valid
        );

        Account account;
        try {
            account = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
            );

            // Authorization Check: Ensure the authenticated user is the one being updated
            // Use .equals() for safe comparison of IDs (handles both primitives and wrappers)
            if (currentUser.getId() != account.getId()) {
                 throw new UnauthedException("Access denied: You can only update your own account.");
            }

            // Password check using PasswordEncoder
            if (!passwordEncoder.matches(password, account.getPassword())) {
                throw new IllegalArgumentException("Incorrect current password provided.");
            }
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body("Bad request: " + e.getMessage());
        }
        // Update using setName() since the domain model uses "name" for the username.
        account.setName(newUsername);
        if (newPassword != null && !newPassword.isEmpty()) {
            account.setPassword(passwordEncoder.encode(newPassword)); // Encode the new password before saving
        }
        accountRepository.save(account);
        return ResponseEntity.ok("Account updated successfully");
    }

    /**
     * Deletes an account from the system.
     *
     * @param email The email of the account to delete
     * @return ResponseEntity with deletion confirmation message
     * @throws IllegalArgumentException if no account is found with the given email
     */
    @Transactional
    public ResponseEntity<String> deleteAccountByEmail(String email) {
        Account accountToDelete = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
        accountRepository.delete(accountToDelete);
        return ResponseEntity.ok("Account with email " + email + " has been deleted");
    }

    /**
     * Upgrades an Account to a GameOwner, preserving associations to other objects that refer to the previous account
     * by transferring them to the new GameOwner. Transactional ensures that exceptions do not cause partial commits.
     *
     * @param email email of the account trying to be promoted
     * @return ResponseEntity denoting the result of the operation
     * @note If there is any issue during runtime, changes are rolled back
     */
    @Transactional
    public ResponseEntity<String> upgradeUserToGameOwner(String email) {
        Account account;
        try {
            account = getAccountByEmail(email);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Bad request: no such account exists.");
        }
        if (account instanceof GameOwner) {
            return ResponseEntity.badRequest().body("Bad request: account already a game owner.");
        }
        // Retrieve the username from getName(); if null, fallback to email.
        String accountName = account.getName();
        if (accountName == null || accountName.trim().isEmpty()) {
            accountName = account.getEmail();
        }
        GameOwner gameOwner = new GameOwner(accountName, account.getEmail(), account.getPassword());
        accountRepository.delete(account);
        accountRepository.save(gameOwner);
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(accountName);
        for (Registration registration : registrations) {
            registration.setAttendee(gameOwner);
        }
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findBorrowRequestsByRequesterName(accountName);
        for (BorrowRequest borrowRequest : borrowRequests) {
            borrowRequest.setRequester(gameOwner);
        }
        List<Review> reviews = reviewRepository.findReviewsByReviewerName(accountName);
        for (Review review : reviews) {
            review.setReviewer(gameOwner);
        }
        return ResponseEntity.ok("Account updated to GameOwner successfully");
    }
}
