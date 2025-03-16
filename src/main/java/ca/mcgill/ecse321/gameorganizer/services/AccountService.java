package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class that handles business logic for account management operations.
 * Provides methods for creating, retrieving, updating, and deleting user accounts.
 *
 * @author @dyune
 */
@Service
public class AccountService {


    private final AccountRepository accountRepository;


    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    /**
     * Creates a new account in the system.
     *
     * @param aNewAccount The account object to create
     * @return ResponseEntity with creation confirmation message
     * @throws IllegalArgumentException if an account with the same email already exists
     */
    @Transactional
    public ResponseEntity<String> createAccount(Account aNewAccount) {
        String email = aNewAccount.getEmail();

        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Account with email " + email + " already exists");
        }

        if (aNewAccount instanceof GameOwner) {
            accountRepository.save((GameOwner) aNewAccount);
        } else {
            accountRepository.save(aNewAccount);
        }

        return ResponseEntity.ok("Account created");
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
     * @param email The email of the account to update
     * @param newName The new name for the account
     * @param newPassword The new password for the account
     * @return ResponseEntity with update confirmation message
     * @throws IllegalArgumentException if no account is found with the given email
     */
    @Transactional
    public ResponseEntity<String> updateAccountByEmail(String email, String newName, String newPassword) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
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
}
