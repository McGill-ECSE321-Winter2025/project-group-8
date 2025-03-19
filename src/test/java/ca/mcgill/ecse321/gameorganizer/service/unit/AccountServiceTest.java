package ca.mcgill.ecse321.gameorganizer.service.unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
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
    testAccount.setId(1);
    testAccount.setName(VALID_USERNAME); // Ensure the name is set

    testGameOwner = new GameOwner(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
    testGameOwner.setId(2);
    testGameOwner.setName(VALID_USERNAME); // Ensure the name is set

    createAccountRequest = new CreateAccountRequest(VALID_EMAIL, VALID_USERNAME, VALID_PASSWORD, false);

    updateAccountRequest = new UpdateAccountRequest();
    updateAccountRequest.setEmail(VALID_EMAIL);
    updateAccountRequest.setUsername(NEW_USERNAME);
    updateAccountRequest.setPassword(VALID_PASSWORD);
    updateAccountRequest.setNewPassword(NEW_PASSWORD);
    }


    // -- createAccount -- //

    @Test
    public void testCreateAccountSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        ResponseEntity<String> response = accountService.createAccount(createAccountRequest);

        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testCreateGameOwnerSuccess() {
        CreateAccountRequest gameOwnerRequest = new CreateAccountRequest(VALID_EMAIL, VALID_USERNAME, VALID_PASSWORD, true);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        ResponseEntity<String> response = accountService.createAccount(gameOwnerRequest);

        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testCreateAccountSuccessMultipleMixedAccounts() {
        String email2 = "test2@example.com";
        CreateAccountRequest request2 = new CreateAccountRequest(email2, "user2", "password", true);

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(email2)).thenReturn(Optional.empty());

        // Use a unified stubbing for save that assigns an id based on the object's class.
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account a = invocation.getArgument(0);
            if (a == null) {
                return null;
            }
            if (a instanceof GameOwner) {
                a.setId(2);
            } else {
                a.setId(1);
            }
            return a;
        });

        ResponseEntity<String> response1 = accountService.createAccount(createAccountRequest);
        ResponseEntity<String> response2 = accountService.createAccount(request2);

        assertEquals("Account created successfully", response1.getBody());
        assertEquals("Account created successfully", response2.getBody());

        // Capture the arguments passed to save and verify one plain Account and one GameOwner were saved.
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(2)).save(captor.capture());
        List<Account> savedAccounts = captor.getAllValues();

        boolean foundPlain = false;
        boolean foundGameOwner = false;
        for (Account a : savedAccounts) {
            if (a != null) {
                if (a.getClass().equals(Account.class)) {
                    foundPlain = true;
                } else if (a.getClass().equals(GameOwner.class)) {
                    foundGameOwner = true;
                }
            }
        }
        assertEquals(true, foundPlain, "Plain Account was not saved.");
        assertEquals(true, foundGameOwner, "GameOwner was not saved.");
    }

    @Test
    public void testCreateAccountFailOnDuplicateAccounts() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        ResponseEntity<String> response = accountService.createAccount(createAccountRequest);

        assertEquals("Email address already in use", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountFailOnMissingFields() {
        CreateAccountRequest invalidRequest = new CreateAccountRequest(null, VALID_USERNAME, VALID_PASSWORD, false);

        ResponseEntity<String> response = accountService.createAccount(invalidRequest);

        assertEquals("Invalid email address", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // -- updateAccount -- //

    @Test
    public void testUpdateAccountSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testUpdateGameOwnerSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpdateAccountSuccessNoNewPassword() {
        updateAccountRequest.setNewPassword(null);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    public void testUpdateGameOwnerSuccessNoNewPassword() {
        updateAccountRequest.setNewPassword(null);
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(accountRepository.save(any(GameOwner.class))).thenReturn(testGameOwner);

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Account updated successfully", response.getBody());
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpdateAccountFailOnNonExistentAccount() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Bad request: Account with email " + VALID_EMAIL + " does not exist", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testUpdateAccountFailWrongPassword() {
        updateAccountRequest.setPassword("wrongPassword");
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        ResponseEntity<String> response = accountService.updateAccount(updateAccountRequest);

        assertEquals("Bad request: Passwords do not match", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // -- deleteAccountByEmail -- //

    @Test
    public void testDeleteAccountSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));

        ResponseEntity<String> response = accountService.deleteAccountByEmail(VALID_EMAIL);

        assertEquals("Account with email " + VALID_EMAIL + " has been deleted", response.getBody());
        verify(accountRepository).delete(testAccount);
    }

    @Test
    public void testDeleteGameOwnerSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));

        ResponseEntity<String> response = accountService.deleteAccountByEmail(VALID_EMAIL);

        assertEquals("Account with email " + VALID_EMAIL + " has been deleted", response.getBody());
        verify(accountRepository).delete(testGameOwner);
    }

    @Test
    public void testDeleteAccountFail() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccountByEmail(VALID_EMAIL));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    @Test
    public void testDeleteGameOwnerFail() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> accountService.deleteAccountByEmail(VALID_EMAIL));
        verify(accountRepository, never()).delete(any(Account.class));
    }

    // -- upgradeUserToGameOwner -- //

    @Test
    public void testUpgradeSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeSuccess2() {
        List<Registration> registrations = new ArrayList<>();
        registrations.add(new Registration());

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(registrations);
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeSuccess3() {
        List<BorrowRequest> borrowRequests = new ArrayList<>();
        borrowRequests.add(new BorrowRequest());
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review());

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());
        when(borrowRequestRepository.findBorrowRequestsByRequesterName(VALID_USERNAME)).thenReturn(borrowRequests);
        when(reviewRepository.findReviewsByReviewerName(VALID_USERNAME)).thenReturn(reviews);

        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository).delete(testAccount);
        verify(accountRepository).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeFailUserDNE() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        assertEquals("Bad request: no such account exists.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeFailUserAlreadyGameOwner() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));

        ResponseEntity<String> response = accountService.upgradeUserToGameOwner(VALID_EMAIL);

        assertEquals("Bad request: account already a game owner.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    // -- getAccountInfoByEmail -- //

    @Test
    public void testGetAccountSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testAccount));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        AccountResponse accountResponse = (AccountResponse) response.getBody();
        assertNotNull(accountResponse);
        assertEquals(VALID_USERNAME, accountResponse.getUsername());
        assertEquals(false, accountResponse.isGameOwner());
    }

    @Test
    public void testGetGameOwnerSuccess() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(testGameOwner));
        when(registrationRepository.findRegistrationByAttendeeName(VALID_USERNAME)).thenReturn(new ArrayList<>());

        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        AccountResponse accountResponse = (AccountResponse) response.getBody();
        assertNotNull(accountResponse);
        assertEquals(VALID_USERNAME, accountResponse.getUsername());
        assertEquals(true, accountResponse.isGameOwner());
    }

    @Test
    public void testGetAccountFailUserDNE() {
        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

        ResponseEntity<?> response = accountService.getAccountInfoByEmail(VALID_EMAIL);

        assertEquals("Bad request: no such account exists.", response.getBody());
    }
}
