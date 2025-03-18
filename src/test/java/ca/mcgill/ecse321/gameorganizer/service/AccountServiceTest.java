package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.dtos.AccountResponse;
import ca.mcgill.ecse321.gameorganizer.dtos.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dtos.EventResponse;
import ca.mcgill.ecse321.gameorganizer.dtos.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
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

    private Account regularAccount;
    private GameOwner gameOwnerAccount;
    private CreateAccountRequest regularAccountRequest;
    private CreateAccountRequest gameOwnerRequest;
    private List<Registration> registrations;
    private List<BorrowRequest> borrowRequests;
    private List<Review> reviews;

    @BeforeEach
    public void setUp() {
        // Create sample data for tests
        regularAccount = new Account("Regular User", "password123", "regular@test.com");
        regularAccount.setId(1);

        gameOwnerAccount = new GameOwner("Game Owner", "password456", "gameowner@test.com");
        gameOwnerAccount.setId(2);

        regularAccountRequest = new CreateAccountRequest(
                "regular@test.com",
                "Regular User",
                "password123",
                false
        );

        gameOwnerRequest = new CreateAccountRequest(
                "gameowner@test.com",
                "Game Owner",
                "password456",
                true
        );

        // Create sample registrations
        registrations = new ArrayList<>();
        Game testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        Event testEvent = new Event(
                "Test Event",
                new Date(),
                "Test Location",
                "Test Description",
                10,
                testGame,
                gameOwnerAccount
        );
        testEvent.setId(1);

        Registration registration = new Registration();
        registration.setAttendee(regularAccount);
        registration.setEventRegisteredFor(testEvent);
        registrations.add(registration);

        // Create sample borrow requests
        borrowRequests = new ArrayList<>();
        BorrowRequest borrowRequest = new BorrowRequest(
                new Date(), // startDate
                new Date(System.currentTimeMillis() + 86400000L), // endDate (tomorrow)
                BorrowRequestStatus.PENDING,
                new Date(), // requestDate
                testGame
        );
        borrowRequest.setRequester(regularAccount);
        borrowRequests.add(borrowRequest);

        // Create sample reviews
        reviews = new ArrayList<>();
        Review review = new Review(5, "Great game!", new Date());
        review.setReviewer(regularAccount);
        review.setGameReviewed(testGame);
        reviews.add(review);
    }

    // Create Account Tests
    @Test
    public void testCreateRegularAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(regularAccount);

        // Call the method
        ResponseEntity<String> response = accountService.createAccount(regularAccountRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testCreateGameOwnerAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail("gameowner@test.com")).thenReturn(Optional.empty());
        when(accountRepository.save(any(GameOwner.class))).thenReturn(gameOwnerAccount);

        // Call the method
        ResponseEntity<String> response = accountService.createAccount(gameOwnerRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account created successfully", response.getBody());
        verify(accountRepository, times(1)).save(any(GameOwner.class));
    }

    @Test
    public void testCreateAccountWithEmptyEmail() {
        // Setup
        regularAccountRequest.setEmail("");

        // Call the method
        ResponseEntity<String> response = accountService.createAccount(regularAccountRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid email address", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountWithNullEmail() {
        // Setup
        regularAccountRequest.setEmail(null);

        // Call the method
        ResponseEntity<String> response = accountService.createAccount(regularAccountRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid email address", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountWithExistingEmail() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        ResponseEntity<String> response = accountService.createAccount(regularAccountRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email address already in use.", response.getBody());
        verify(accountRepository, never()).save(any(Account.class));
    }

    // Get Account By Email Tests
    @Test
    public void testGetAccountByEmailSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        Account result = accountService.getAccountByEmail("regular@test.com");

        // Assert
        assertNotNull(result);
        assertEquals("Regular User", result.getName());
        assertEquals("regular@test.com", result.getEmail());
    }

    @Test
    public void testGetAccountByEmailNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountByEmail("nonexistent@test.com");
        });
        assertEquals("Account with email nonexistent@test.com does not exist", exception.getMessage());
    }

    // Get Account Info By Email Tests
    @Test
    public void testGetAccountInfoByEmailSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));
        when(registrationRepository.findRegistrationByAttendeeName("Regular User")).thenReturn(registrations);

        // Call the method
        ResponseEntity<?> response = accountService.getAccountInfoByEmail("regular@test.com");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof AccountResponse);

        AccountResponse accountResponse = (AccountResponse) response.getBody();
        assertEquals("Regular User", accountResponse.getUsername());
        assertFalse(accountResponse.isGameOwner());
        assertEquals(1, accountResponse.getEvents().size());
    }

    @Test
    public void testGetAccountInfoByEmailNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Call the method
        ResponseEntity<?> response = accountService.getAccountInfoByEmail("nonexistent@test.com");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Bad request: no such account exists.", response.getBody());
    }

    // Get Account By ID Tests
    @Test
    public void testGetAccountByIdSuccess() {
        // Setup
        when(accountRepository.findById(1)).thenReturn(Optional.of(regularAccount));

        // Call the method
        Account result = accountService.getAccountById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("Regular User", result.getName());
    }

    @Test
    public void testGetAccountByIdNotFound() {
        // Setup
        when(accountRepository.findById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountById(99);
        });
        assertEquals("Account with ID 99 does not exist", exception.getMessage());
    }

    // Update Account Tests
    @Test
    public void testUpdateAccountSuccess() {
        // Setup
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setEmail("regular@test.com");
        updateRequest.setUsername("Updated User");
        updateRequest.setPassword("password123");
        updateRequest.setNewPassword("newpassword123");

        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        ResponseEntity<String> response = accountService.updateAccount(updateRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account updated successfully", response.getBody());
        assertEquals("Updated User", regularAccount.getName());
        assertEquals("newpassword123", regularAccount.getPassword());
    }

    @Test
    public void testUpdateAccountWithNoNewPassword() {
        // Setup
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setEmail("regular@test.com");
        updateRequest.setUsername("Updated User");
        updateRequest.setPassword("password123");
        updateRequest.setNewPassword(null);

        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        ResponseEntity<String> response = accountService.updateAccount(updateRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account updated successfully", response.getBody());
        assertEquals("Updated User", regularAccount.getName());
        assertEquals("password123", regularAccount.getPassword()); // Password shouldn't change
    }

    @Test
    public void testUpdateAccountWithEmptyNewPassword() {
        // Setup
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setEmail("regular@test.com");
        updateRequest.setUsername("Updated User");
        updateRequest.setPassword("password123");
        updateRequest.setNewPassword("");

        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        ResponseEntity<String> response = accountService.updateAccount(updateRequest);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account updated successfully", response.getBody());
        assertEquals("Updated User", regularAccount.getName());
        assertEquals("password123", regularAccount.getPassword()); // Password shouldn't change
    }

    @Test
    public void testUpdateAccountNotFound() {
        // Setup
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setEmail("nonexistent@test.com");
        updateRequest.setUsername("Updated User");
        updateRequest.setPassword("password123");

        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Call the method
        ResponseEntity<String> response = accountService.updateAccount(updateRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Bad request: Account with email nonexistent@test.com does not exist"));
    }

    @Test
    public void testUpdateAccountWithIncorrectPassword() {
        // Setup
        UpdateAccountRequest updateRequest = new UpdateAccountRequest();
        updateRequest.setEmail("regular@test.com");
        updateRequest.setUsername("Updated User");
        updateRequest.setPassword("wrongpassword");

        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        ResponseEntity<String> response = accountService.updateAccount(updateRequest);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Bad request: Passwords do not match"));
    }

    // Delete Account Tests
    @Test
    public void testDeleteAccountByEmailSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));
        doNothing().when(accountRepository).delete(regularAccount);

        // Call the method
        ResponseEntity<String> response = accountService.deleteAccountByEmail("regular@test.com");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account with email regular@test.com has been deleted", response.getBody());
        verify(accountRepository, times(1)).delete(regularAccount);
    }

    @Test
    public void testDeleteAccountByEmailNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccountByEmail("nonexistent@test.com");
        });
        assertEquals("Account with email nonexistent@test.com does not exist", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
    }

    // Upgrade User To Game Owner Tests
    @Test
    public void testUpgradeUserToGameOwnerSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));
        doNothing().when(accountRepository).delete(regularAccount);
        when(accountRepository.save(any(GameOwner.class))).thenReturn(gameOwnerAccount);

        when(registrationRepository.findRegistrationByAttendeeName("Regular User")).thenReturn(registrations);
        when(borrowRequestRepository.findBorrowRequestsByRequesterName("Regular User")).thenReturn(borrowRequests);
        when(reviewRepository.findReviewsByReviewerName("Regular User")).thenReturn(reviews);

        // Call the method
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner("regular@test.com");

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account updated to GameOwner successfully", response.getBody());
        verify(accountRepository, times(1)).delete(regularAccount);
        verify(accountRepository, times(1)).save(any(GameOwner.class));

        // Verify that associations were transferred
        verify(registrationRepository, times(1)).findRegistrationByAttendeeName("Regular User");
        verify(borrowRequestRepository, times(1)).findBorrowRequestsByRequesterName("Regular User");
        verify(reviewRepository, times(1)).findReviewsByReviewerName("Regular User");
    }

    @Test
    public void testUpgradeUserToGameOwnerNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Call the method
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner("nonexistent@test.com");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Bad request: no such account exists.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeUserToGameOwnerAlreadyGameOwner() {
        // Setup
        when(accountRepository.findByEmail("gameowner@test.com")).thenReturn(Optional.of(gameOwnerAccount));

        // Call the method
        ResponseEntity<String> response = accountService.upgradeUserToGameOwner("gameowner@test.com");

        // Assert
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Bad request: account already a game owner.", response.getBody());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }
}