package ca.mcgill.ecse321.gameorganizer.service.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gameorganizer.dto.AccountResponse;
import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @InjectMocks
    private AccountService accountService;

    // Test constants
    private static final String VALID_USERNAME = "testUser";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String NEW_USERNAME = "newUsername";
    private static final String NEW_PASSWORD = "newPassword123";

    private Account testAccount;
    private GameOwner testGameOwner;
    private CreateAccountRequest createAccountRequest;
    private UpdateAccountRequest updateAccountRequest;

    @BeforeEach
    public void setup() {
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
        testGameOwner = new GameOwner(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
        
        createAccountRequest = new CreateAccountRequest(VALID_EMAIL, VALID_USERNAME, VALID_PASSWORD, false);
        
        updateAccountRequest = new UpdateAccountRequest();
        updateAccountRequest.setEmail(VALID_EMAIL);
        updateAccountRequest.setUsername(NEW_USERNAME);
        updateAccountRequest.setPassword(VALID_PASSWORD);
        updateAccountRequest.setNewPassword(NEW_PASSWORD);
    }

    @Test
    public void testCreateAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Test
        ResponseEntity<String> response = accountService.createAccount(createAccountRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testCreateGameOwnerSuccess() {
        // Setup
        CreateAccountRequest gameOwnerRequest = new CreateAccountRequest(VALID_EMAIL, VALID_USERNAME, VALID_PASSWORD, true);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        // Test
        ResponseEntity<String> response = accountService.createAccount(gameOwnerRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testCreateAccountSuccessMultipleMixedAccounts() {
        // Setup
        String email2 = "test2@example.com";
        CreateAccountRequest request2 = new CreateAccountRequest(email2, "user2", "password", true);

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(email2)).thenReturn(Optional.empty());

        // Test
        ResponseEntity<String> response1 = accountService.createAccount(createAccountRequest);
        ResponseEntity<String> response2 = accountService.createAccount(request2);

        // Verify
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        verify(accountRepository).save(any(Account.class));
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testCreateAccountFailOnDuplicateAccounts() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        // Test
        ResponseEntity<String> response = accountService.createAccount(createAccountRequest);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email address already in use.", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountFailOnMissingFields() {
        // Setup
        CreateAccountRequest invalidRequest = new CreateAccountRequest(null, VALID_USERNAME, VALID_PASSWORD, false);

        // Test
        ResponseEntity<String> response = accountService.createAccount(invalidRequest);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid email address", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testUpdateAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testUpdateGameOwnerSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpdateAccountSuccessNoNewPassword() {
        // Setup
        updateAccountRequest.setNewPassword(null);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testUpdateGameOwnerSuccessNoNewPassword() {
        // Setup
        updateAccountRequest.setNewPassword(null);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpdateAccountFailOnNonExistentAccount() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: Account with email " + VALID_EMAIL + " does not exist", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testUpdateAccountFailWrongPassword() {
        // Setup
        updateAccountRequest.setPassword("wrongPassword");
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        // Test
        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: Passwords do not match", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testDeleteAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        // Test
        ResponseEntity<String> response = accountService.deleteAccountByEmail(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account with email " + VALID_EMAIL + " has been deleted", response.getBody());
        verify(accountRepository).delete(testAccount);
    }

    @Test
    public void testDeleteGameOwnerSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));

        // Test
        ResponseEntity<String> response = accountService.deleteAccountByEmail(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account with email " + VALID_EMAIL + " has been deleted", response.getBody());
        verify(accountRepository).delete(testGameOwner);
    }

    @Test
    public void testDeleteAccountFail() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccountByEmail(VALID_EMAIL));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    public void testUpgradeSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        // Test
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeSuccess2() {
        // Setup
        List<Registration> registrations = new ArrayList<>();
        registrations.add(new Registration());
        
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(registrations);
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        // Test
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeSuccess3() {
        // Setup
        List<BorrowRequest> borrowRequests = new ArrayList<>();
        borrowRequests.add(new BorrowRequest());
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review());
        
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(borrowRequests);
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(reviews);

        // Test
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeFailUserDNE() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        // Test
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: no such account exists.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeFailUserAlreadyGameOwner() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));

        // Test
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: account already a game owner.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    @Test
    public void testGetAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        // Test
        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse accountResponse = (AccountResponse) response.getBody();
        assertEquals(VALID_USERNAME, accountResponse.getUsername());
        assertEquals(false, accountResponse.isGameOwner());
    }

    @Test
    public void testGetGameOwnerSuccess() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        // Test
        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse accountResponse = (AccountResponse) response.getBody();
        assertEquals(VALID_USERNAME, accountResponse.getUsername());
        assertEquals(true, accountResponse.isGameOwner());
    }

    @Test
    public void testGetAccountFailUserDNE() {
        // Setup
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        // Test
        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad request: no such account exists.", response.getBody());
    }
}
