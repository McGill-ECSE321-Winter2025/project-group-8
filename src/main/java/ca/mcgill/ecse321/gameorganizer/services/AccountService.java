package ca.mcgill.ecse321.gameorganizer.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dtos.AccountCreationDto;
import ca.mcgill.ecse321.gameorganizer.dtos.AccountResponseDto;
import ca.mcgill.ecse321.gameorganizer.dtos.EventResponseDto;
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
 * @author @dyune (modified by current author)
 */
@Service
public class AccountService {

    private AccountRepository accountRepository;
    private RegistrationRepository registrationRepository;
    private ReviewRepository reviewRepository;
    private BorrowRequestRepository borrowRequestRepository;

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
     * @param accountCreationDto DTO containing account information to create
     * @return AccountResponseDto containing the created account information
     * @throws IllegalArgumentException if email is invalid or already in use
     */
    @Transactional
    public AccountResponseDto createAccount(AccountCreationDto accountCreationDto) {
        String email = accountCreationDto.getEmail();
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email address cannot be empty");
        }

        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email address already in use");
        }

        Account newAccount;
        if (!accountCreationDto.isGameOwner()) {
            newAccount = new Account(
                    accountCreationDto.getUsername(),
                    accountCreationDto.getEmail(),
                    accountCreationDto.getPassword()
            );
        } else {
            newAccount = new GameOwner(
                    accountCreationDto.getUsername(),
                    accountCreationDto.getEmail(),
                    accountCreationDto.getPassword()
            );
        }

        accountRepository.save(newAccount);
        return new AccountResponseDto(newAccount);
    }

    /**
     * Retrieves an account by email address.
     *
     * @param email The email address of the account to retrieve
     * @return Account object
     * @throws IllegalArgumentException if no account is found with the given email
     */
    @Transactional
    public Account getAccountByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        return accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
    }

    /**
     * Retrieves account information by email.
     *
     * @param email The email of the account to retrieve
     * @return AccountResponseDto containing account details
     * @throws IllegalArgumentException if no account is found with the given email
     */
    @Transactional
    public AccountResponseDto getAccountInfoByEmail(String email) {
        Account account = getAccountByEmail(email);

        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(account.getName());

        List<EventResponseDto> events = registrations.stream()
                .map(registration -> new EventResponseDto(registration.getEventRegisteredFor()))
                .collect(Collectors.toList());

        return new AccountResponseDto(account, events);
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve
     * @return Account object
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
     * @param accountId ID of the account to update
     * @param accountUpdateDto DTO containing updated account information
     * @return AccountResponseDto containing the updated account information
     * @throws IllegalArgumentException if no account is found with the given ID or authentication fails
     */
    @Transactional
    public AccountResponseDto updateAccount(int accountId, AccountCreationDto accountUpdateDto) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new IllegalArgumentException("Account with ID " + accountId + " does not exist")
        );

        // Update the username if provided
        if (accountUpdateDto.getUsername() != null && !accountUpdateDto.getUsername().trim().isEmpty()) {
            account.setName(accountUpdateDto.getUsername());
        }

        // Update the password if provided
        if (accountUpdateDto.getPassword() != null && !accountUpdateDto.getPassword().trim().isEmpty()) {
            account.setPassword(accountUpdateDto.getPassword());
        }

        // Email updates could be added here if needed, with validation for uniqueness

        accountRepository.save(account);
        return new AccountResponseDto(account);
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
        Account accountToDelete = getAccountByEmail(email);
        accountRepository.delete(accountToDelete);
        return ResponseEntity.ok("Account with email " + email + " has been deleted");
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete
     * @return ResponseEntity with deletion confirmation message
     * @throws IllegalArgumentException if no account is found with the given ID
     */
    @Transactional
    public ResponseEntity<String> deleteAccount(int id) {
        Account accountToDelete = getAccountById(id);
        accountRepository.delete(accountToDelete);
        return ResponseEntity.ok("Account with ID " + id + " has been deleted");
    }

    /**
     * Upgrades an Account to a GameOwner, preserving associations.
     *
     * @param email Email of the account to be promoted
     * @return AccountResponseDto containing the upgraded account information
     * @throws IllegalArgumentException if account doesn't exist or is already a GameOwner
     */
    @Transactional
    public AccountResponseDto upgradeToGameOwner(String email) {
        Account account = getAccountByEmail(email);

        if (account instanceof GameOwner) {
            throw new IllegalArgumentException("Account is already a GameOwner");
        }

        // Save original account data
        String originalName = account.getName();
        String originalEmail = account.getEmail();
        String originalPassword = account.getPassword();

        // Transfer associations from old account to new GameOwner
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(originalName);
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findBorrowRequestsByRequesterName(originalName);
        List<Review> reviews = reviewRepository.findReviewsByReviewerName(originalName);

        // Delete original account
        accountRepository.delete(account);

        // Create new GameOwner with original account data
        GameOwner gameOwner = new GameOwner(
                originalName,
                originalEmail,
                originalPassword
        );

        // Save new GameOwner
        GameOwner savedGameOwner = accountRepository.save(gameOwner);

        // Update associations
        for (Registration registration : registrations) {
            registration.setAttendee(savedGameOwner);
        }

        for (BorrowRequest borrowRequest : borrowRequests) {
            borrowRequest.setRequester(savedGameOwner);
        }

        for (Review review : reviews) {
            review.setReviewer(savedGameOwner);
        }

        return new AccountResponseDto(savedGameOwner);
    }

    /**
     * Retrieves all accounts in the system.
     *
     * @return List of all accounts
     */
    @Transactional
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(AccountResponseDto::new)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all GameOwner accounts in the system.
     *
     * @return List of all GameOwner accounts
     */
    @Transactional
    public List<AccountResponseDto> getAllGameOwners() {
        List<Account> allAccounts = accountRepository.findAll();
        List<AccountResponseDto> gameOwners = new ArrayList<>();

        for (Account account : allAccounts) {
            if (account instanceof GameOwner) {
                gameOwners.add(new AccountResponseDto(account));
            }
        }

        return gameOwners;
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param email The email for authentication
     * @param password The password for authentication
     * @return AccountResponseDto if authentication succeeds
     * @throws IllegalArgumentException if authentication fails
     */
    @Transactional
    public AccountResponseDto authenticateUser(String email, String password) {
        Account account = getAccountByEmail(email);

        if (!account.getPassword().equals(password)) {
            throw new IllegalArgumentException("Authentication failed: Invalid credentials");
        }

        return new AccountResponseDto(account);
    }
}