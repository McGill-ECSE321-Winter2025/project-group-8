package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.dtos.AccountCreationDto;
import ca.mcgill.ecse321.gameorganizer.dtos.AccountResponseDto;
import ca.mcgill.ecse321.gameorganizer.dtos.AuthenticationRequestDto;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing account-related operations.
 * <p>
 * This controller exposes the endpoints below:
 * <ul>
 *   <li><strong>POST /accounts</strong>: Create a new account.</li>
 *   <li><strong>GET /accounts</strong>: Get all accounts or game owners.</li>
 *   <li><strong>GET /accounts/{id}</strong>: Get an account by ID.</li>
 *   <li><strong>GET /accounts/email/{email}</strong>: Get an account by email.</li>
 *   <li><strong>PUT /accounts/{id}</strong>: Update an existing account.</li>
 *   <li><strong>DELETE /accounts/{id}</strong>: Delete an account by ID.</li>
 *   <li><strong>DELETE /accounts/email/{email}</strong>: Delete an account by email.</li>
 *   <li><strong>PUT /accounts/upgrade/{email}</strong>: Upgrade an Account to a GameOwner.</li>
 *   <li><strong>POST /accounts/authenticate</strong>: Authenticate a user.</li>
 * </ul>
 * </p>
 *
 * @see ca.mcgill.ecse321.gameorganizer.services.AccountService
 */
@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    /**
     * Constructs a new AccountController with the given AccountService.
     *
     * @param accountService the service used to handle account operations
     */
    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Creates a new account.
     *
     * @param accountCreationDto the request body containing account creation info
     * @return AccountResponseDto with the created account details
     */
    @PostMapping("")
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountCreationDto accountCreationDto) {
        try {
            AccountResponseDto createdAccount = accountService.createAccount(accountCreationDto);
            return new ResponseEntity<>(createdAccount, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieves all accounts.
     *
     * @param gameOwnersOnly flag to retrieve only GameOwner accounts
     * @return List of all accounts or only GameOwner accounts
     */
    @GetMapping("")
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts(
            @RequestParam(required = false, defaultValue = "false") boolean gameOwnersOnly) {
        if (gameOwnersOnly) {
            return ResponseEntity.ok(accountService.getAllGameOwners());
        } else {
            return ResponseEntity.ok(accountService.getAllAccounts());
        }
    }

    /**
     * Retrieves an account by ID.
     *
     * @param id the ID of the account to retrieve
     * @return AccountResponseDto with the account details
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable int id) {
        try {
            AccountResponseDto account = new AccountResponseDto(accountService.getAccountById(id));
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Retrieves an account by email with associated events.
     *
     * @param email the email of the account to retrieve
     * @return AccountResponseDto with account and events details
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<AccountResponseDto> getAccountByEmail(@PathVariable String email) {
        try {
            AccountResponseDto account = accountService.getAccountInfoByEmail(email);
            return ResponseEntity.ok(account);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Updates an existing account.
     *
     * @param id the ID of the account to update
     * @param accountUpdateDto updated account information
     * @return AccountResponseDto with the updated account details
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable int id,
            @Valid @RequestBody AccountCreationDto accountUpdateDto) {
        try {
            AccountResponseDto updatedAccount = accountService.updateAccount(id, accountUpdateDto);
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Deletes an account by ID.
     *
     * @param id the ID of the account to delete
     * @return confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable int id) {
        try {
            return accountService.deleteAccount(id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an account by email.
     *
     * @param email the email of the account to delete
     * @return confirmation message
     */
    @DeleteMapping("/email/{email}")
    public ResponseEntity<String> deleteAccountByEmail(@PathVariable String email) {
        try {
            return accountService.deleteAccountByEmail(email);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Upgrades an account to a GameOwner.
     *
     * @param email the email of the account to upgrade
     * @return AccountResponseDto with the upgraded account details
     */
    @PutMapping("/upgrade/{email}")
    public ResponseEntity<AccountResponseDto> upgradeToGameOwner(@PathVariable String email) {
        try {
            AccountResponseDto upgradedAccount = accountService.upgradeToGameOwner(email);
            return ResponseEntity.ok(upgradedAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Authenticates a user with email and password.
     *
     * @param request authentication request with email and password
     * @return AccountResponseDto if authentication succeeds
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AccountResponseDto> authenticateUser(@Valid @RequestBody AuthenticationRequestDto request) {
        try {
            AccountResponseDto authenticatedAccount =
                    accountService.authenticateUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(authenticatedAccount);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}