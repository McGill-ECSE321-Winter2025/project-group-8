package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.dto.requests.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.responses.AccountResponse;
import ca.mcgill.ecse321.gameorganizer.dto.responses.EventResponse;
import ca.mcgill.ecse321.gameorganizer.models.*;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class that handles business logic for account management operations.
 * Provides methods for creating, retrieving, updating, and deleting user accounts.
 *
 * @author @dyune
 */
@Service
public class AccountService {


    private final AccountRepository accountRepository;
    private final RegistrationRepository registrationRepository;
    private final ReviewRepository reviewRepository;
    private final BorrowRequestRepository borrowRequestRepository;


    @Autowired
    public AccountService(
            AccountRepository accountRepository,
            RegistrationRepository registrationRepository,
            ReviewRepository reviewRepository,
            BorrowRequestRepository borrowRequestRepository) {
        this.accountRepository = accountRepository;
        this.registrationRepository = registrationRepository;
        this.reviewRepository = reviewRepository;
        this.borrowRequestRepository = borrowRequestRepository;
    }


    /**
     * Creates a new account in the system.
     *
     * @param request The account information with which an account will be created with
     * @return ResponseEntity with creation confirmation message or an error message
     */

    @Transactional
    public ResponseEntity<String> createAccount(CreateAccountRequest request) {

        String email = request.getEmail();
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid email address");
        }

        if (accountRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email address already in use.");
        }

        if (!request.isGameOwner()) { // not game owner -> make account
            Account aNewAccount = new Account(
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );
            accountRepository.save(aNewAccount);
        } else {
            GameOwner aNewAccount = new GameOwner( // game owner -> make game owner
                    request.getUsername(),
                    request.getPassword(),
                    request.getEmail()
            );
            accountRepository.save(aNewAccount);
        }
        return ResponseEntity.ok("Account created successfully");
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
     * @return ResponseEntity with the information as a body or a Bad Request if
     *         no such account exists
     */

    @Transactional
    public ResponseEntity<?> getAccountInfoByEmail(String email) {
        Account account;

        try {
            account = getAccountByEmail(email);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body("Bad request: no such account exists.");
        }

        // Get account details for DTO, name, account type, registrations
        String accountName = account.getName();

        List<Registration> registrations = registrationRepository
                .findRegistrationByAttendeeName(accountName);

        boolean isGameOwner = account instanceof GameOwner;

        List<EventResponse> events = new ArrayList<>();

        for (Registration registration : registrations) {
            Event event = registration.getEventRegisteredFor();
            EventResponse eventResponse = new EventResponse(event);
            events.add(eventResponse);
        }

        AccountResponse response = new AccountResponse(
                accountName,
                events,
                isGameOwner
        );

        return ResponseEntity.ok(response);

    }


    /**
     * Updates an existing account's information.
     *
     * @param email The email of the account to update
     * @param newName The new name for the account
     * @param newPassword The new password for the account
     * @return ResponseEntity with update confirmation message or failure message
     */

    @Transactional
    public ResponseEntity<String> updateAccountByEmail(String email, String newName, String newPassword) {
        Account account;
        try {
        account = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist");
        } catch (Exception){
            return ResponseEntity.badRequest().body("Bad request: no such account exists.");
        }

        account.setName(newName);
        account.setPassword(newPassword);
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
     * Upgrades an Account to a GameOwner, preserving associations to other objects
     * that refer to the previous account by transferring them to the new GameOwner.
     * Transactional ensures that exceptions do not cause partial commits.
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
            return ResponseEntity
                    .badRequest()
                    .body("Bad request: no such account exists.");
        }

        if (account instanceof GameOwner) {
            return ResponseEntity
                    .badRequest()
                    .body("Bad request: account already a game owner.");
        }

        String accountName = account.getName();

        // Duplicate Account as GameOwner,
        // this may not be very secure but inputs should already
        // have been validated should be okay for now

        GameOwner gameOwner = new GameOwner(
                accountName,
                account.getPassword(),
                account.getEmail()
        );

        // Delete old account
        accountRepository.delete(account);

        // Make new account in its place
        accountRepository.save(gameOwner);

        // Change all Registration, BorrowRequest, Review to point to this new account
        // Transactional makes sure if any exceptions occur, all changes should be rolled back

        List<Registration> registrations = registrationRepository
                .findRegistrationByAttendeeName(accountName);

        for (Registration registration : registrations) {
            registration.setAttendee(gameOwner);
        }

        List<BorrowRequest> borrowRequests = borrowRequestRepository
                .findBorrowRequestsByRequesterName(accountName);

        for (BorrowRequest borrowRequest : borrowRequests) {
            borrowRequest.setRequester(gameOwner);
        }

        List<Review> reviews = reviewRepository
                .findReviewsByReviewerName(accountName);

        for (Review review : reviews) {
            review.setReviewer(gameOwner);
        }
        return ResponseEntity.ok("Account updated to GameOwner successfully");
    }
}
