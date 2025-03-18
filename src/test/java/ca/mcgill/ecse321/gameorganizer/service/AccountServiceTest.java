package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.dtos.AccountCreationDto;
import ca.mcgill.ecse321.gameorganizer.dtos.AccountResponseDto;
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

import java.util.ArrayList;
import java.util.Date;
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
    private AccountCreationDto regularAccountDto;
    private AccountCreationDto gameOwnerDto;
    private List<Registration> registrations;
    private List<BorrowRequest> borrowRequests;
    private List<Review> reviews;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create sample data for tests
        regularAccount = new Account("Regular User", "regular@test.com", "password123");
        regularAccount.setId(1);

        gameOwnerAccount = new GameOwner("Game Owner", "gameowner@test.com", "password456");
        gameOwnerAccount.setId(2);

        regularAccountDto = new AccountCreationDto(
                "regular@test.com",
                "Regular User",
                "password123",
                false
        );

        gameOwnerDto = new AccountCreationDto(
                "gameowner@test.com",
                "Game Owner",
                "password456",
                true
        );

        // Create sample registrations
        registrations = new ArrayList<>();
        Game testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
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
        AccountResponseDto response = accountService.createAccount(regularAccountDto);

        // Assert
        assertNotNull(response);
        assertEquals("Regular User", response.getName());
        assertEquals("regular@test.com", response.getEmail());
        assertFalse(response.isGameOwner());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testCreateGameOwnerAccountSuccess() {
        // Setup
        when(accountRepository.findByEmail("gameowner@test.com")).thenReturn(Optional.empty());
        when(accountRepository.save(any(GameOwner.class))).thenReturn(gameOwnerAccount);

        // Call the method
        AccountResponseDto response = accountService.createAccount(gameOwnerDto);

        // Assert
        assertNotNull(response);
        assertEquals("Game Owner", response.getName());
        assertEquals("gameowner@test.com", response.getEmail());
        assertTrue(response.isGameOwner());
        verify(accountRepository, times(1)).save(any(GameOwner.class));
    }

    @Test
    public void testCreateAccountWithEmptyEmail() {
        // Setup
        regularAccountDto.setEmail("");

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(regularAccountDto);
        });

        assertEquals("Email address cannot be empty", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountWithNullEmail() {
        // Setup
        regularAccountDto.setEmail(null);

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(regularAccountDto);
        });

        assertEquals("Email address cannot be empty", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreateAccountWithExistingEmail() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(regularAccountDto);
        });

        assertEquals("Email address already in use", exception.getMessage());
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

    @Test
    public void testGetAccountByEmailEmpty() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountByEmail("");
        });
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    public void testGetAccountByEmailNull() {
        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountByEmail(null);
        });
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    // Get Account Info By Email Tests
    @Test
    public void testGetAccountInfoByEmailSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));
        when(registrationRepository.findRegistrationByAttendeeName("Regular User")).thenReturn(registrations);

        // Call the method
        AccountResponseDto response = accountService.getAccountInfoByEmail("regular@test.com");

        // Assert
        assertNotNull(response);
        assertEquals("Regular User", response.getName());
        assertEquals("regular@test.com", response.getEmail());
        assertFalse(response.isGameOwner());
        assertNotNull(response.getRegisteredEvents());
        assertEquals(1, response.getRegisteredEvents().size());
    }

    @Test
    public void testGetAccountInfoByEmailNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountInfoByEmail("nonexistent@test.com");
        });
        assertEquals("Account with email nonexistent@test.com does not exist", exception.getMessage());
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
        AccountCreationDto updateDto = new AccountCreationDto(
                "regular@test.com",
                "Updated User",
                "newpassword123",
                false
        );

        when(accountRepository.findById(1)).thenReturn(Optional.of(regularAccount));

        // Simulate the updated account that will be returned
        Account updatedAccount = new Account("Updated User", "regular@test.com", "newpassword123");
        updatedAccount.setId(1);
        when(accountRepository.save(any(Account.class))).thenReturn(updatedAccount);

        // Call the method
        AccountResponseDto response = accountService.updateAccount(1, updateDto);

        // Assert
        assertNotNull(response);
        assertEquals("Updated User", response.getName());
        assertEquals("regular@test.com", response.getEmail());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testUpdateAccountNotFound() {
        // Setup
        AccountCreationDto updateDto = new AccountCreationDto(
                "nonexistent@test.com",
                "Updated User",
                "newpassword123",
                false
        );

        when(accountRepository.findById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.updateAccount(99, updateDto);
        });
        assertEquals("Account with ID 99 does not exist", exception.getMessage());
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

    @Test
    public void testDeleteAccountSuccess() {
        // Setup
        when(accountRepository.findById(1)).thenReturn(Optional.of(regularAccount));
        doNothing().when(accountRepository).delete(regularAccount);

        // Call the method
        ResponseEntity<String> response = accountService.deleteAccount(1);

        // Assert
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Account with ID 1 has been deleted", response.getBody());
        verify(accountRepository, times(1)).delete(regularAccount);
    }

    @Test
    public void testDeleteAccountNotFound() {
        // Setup
        when(accountRepository.findById(99)).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccount(99);
        });
        assertEquals("Account with ID 99 does not exist", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
    }

    // Upgrade User To Game Owner Tests
    @Test
    public void testUpgradeToGameOwnerSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));
        doNothing().when(accountRepository).delete(regularAccount);

        // Create a GameOwner with the SAME NAME as the regular account
        GameOwner upgradedOwner = new GameOwner("Regular User", "regular@test.com", "password123");
        upgradedOwner.setId(1);

        when(accountRepository.save(any(GameOwner.class))).thenReturn(upgradedOwner);

        when(registrationRepository.findRegistrationByAttendeeName("Regular User")).thenReturn(registrations);
        when(borrowRequestRepository.findBorrowRequestsByRequesterName("Regular User")).thenReturn(borrowRequests);
        when(reviewRepository.findReviewsByReviewerName("Regular User")).thenReturn(reviews);

        // Call the method
        AccountResponseDto response = accountService.upgradeToGameOwner("regular@test.com");

        // Assert
        assertNotNull(response);
        assertEquals("Regular User", response.getName()); // Name should remain the same after upgrade
        assertTrue(response.isGameOwner()); // But account type should be GameOwner
        verify(accountRepository, times(1)).delete(regularAccount);
        verify(accountRepository, times(1)).save(any(GameOwner.class));

        // Verify that associations were transferred
        verify(registrationRepository, times(1)).findRegistrationByAttendeeName("Regular User");
        verify(borrowRequestRepository, times(1)).findBorrowRequestsByRequesterName("Regular User");
        verify(reviewRepository, times(1)).findReviewsByReviewerName("Regular User");
    }

    @Test
    public void testUpgradeToGameOwnerNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.upgradeToGameOwner("nonexistent@test.com");
        });
        assertEquals("Account with email nonexistent@test.com does not exist", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    @Test
    public void testUpgradeToGameOwnerAlreadyGameOwner() {
        // Setup
        when(accountRepository.findByEmail("gameowner@test.com")).thenReturn(Optional.of(gameOwnerAccount));

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.upgradeToGameOwner("gameowner@test.com");
        });
        assertEquals("Account is already a GameOwner", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
        verify(accountRepository, never()).save(any(GameOwner.class));
    }

    // Get All Accounts Tests
    @Test
    public void testGetAllAccountsSuccess() {
        // Setup
        List<Account> accounts = new ArrayList<>();
        accounts.add(regularAccount);
        accounts.add(gameOwnerAccount);

        when(accountRepository.findAll()).thenReturn(accounts);

        // Call the method
        List<AccountResponseDto> result = accountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Regular User", result.get(0).getName());
        assertEquals("Game Owner", result.get(1).getName());
    }

    @Test
    public void testGetAllAccountsEmpty() {
        // Setup
        when(accountRepository.findAll()).thenReturn(new ArrayList<>());

        // Call the method
        List<AccountResponseDto> result = accountService.getAllAccounts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Get All Game Owners Tests
    @Test
    public void testGetAllGameOwnersSuccess() {
        // Setup
        List<Account> accounts = new ArrayList<>();
        accounts.add(regularAccount);
        accounts.add(gameOwnerAccount);

        when(accountRepository.findAll()).thenReturn(accounts);

        // Call the method
        List<AccountResponseDto> result = accountService.getAllGameOwners();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Game Owner", result.get(0).getName());
        assertTrue(result.get(0).isGameOwner());
    }

    @Test
    public void testGetAllGameOwnersEmpty() {
        // Setup
        List<Account> accounts = new ArrayList<>();
        accounts.add(regularAccount); // Only regular account, no game owners

        when(accountRepository.findAll()).thenReturn(accounts);

        // Call the method
        List<AccountResponseDto> result = accountService.getAllGameOwners();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // Authenticate User Tests
    @Test
    public void testAuthenticateUserSuccess() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Call the method
        AccountResponseDto result = accountService.authenticateUser("regular@test.com", "password123");

        // Assert
        assertNotNull(result);
        assertEquals("Regular User", result.getName());
        assertEquals("regular@test.com", result.getEmail());
    }

    @Test
    public void testAuthenticateUserInvalidPassword() {
        // Setup
        when(accountRepository.findByEmail("regular@test.com")).thenReturn(Optional.of(regularAccount));

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.authenticateUser("regular@test.com", "wrongpassword");
        });
        assertEquals("Authentication failed: Invalid credentials", exception.getMessage());
    }

    @Test
    public void testAuthenticateUserNotFound() {
        // Setup
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.authenticateUser("nonexistent@test.com", "password123");
        });
        assertEquals("Account with email nonexistent@test.com does not exist", exception.getMessage());
    }
}