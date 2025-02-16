package ca.mcgill.ecse321.gameorganizer.repository;

import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.services.RegistrationService;

@SpringBootTest
public class RegistrationRepositoryTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventRepository eventRepository;

    private Registration testRegistration;
    private Account testAccount;
    private Event testEvent;
    private Date testDate;

    @BeforeEach
    public void setUp() {
        // Clear the database before each test
        registrationRepository.deleteAll();
        accountRepository.deleteAll();
        eventRepository.deleteAll();

        // Create test data
        testDate = new Date();
        
        // Create and save test account
        testAccount = new Account();
        testAccount.setEmail("test@example.com");
        testAccount.setName("Test User");
        testAccount.setPassword("password123"); // Set the required password field
        // Set other required Account properties here
        testAccount = accountRepository.save(testAccount);

        // Create and save test event
        testEvent = new Event();
        testEvent.setTitle("Test Event");
        // Set other required Event properties here
        testEvent = eventRepository.save(testEvent);
    }

    @AfterEach
    public void cleanUp() {
        registrationRepository.deleteAll();
        accountRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    public void testCreateRegistration() {
        // Act
        Registration result = registrationService.createRegistration(testDate, testAccount, testEvent);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(testDate, result.getRegistrationDate());
        assertEquals(testAccount.getId(), result.getAttendee().getId());
        assertEquals(testEvent.getId(), result.getEventRegisteredFor().getId());

        // Verify it's in the database
        Optional<Registration> storedRegistration = registrationRepository.findRegistrationById(result.getId());
        assertTrue(storedRegistration.isPresent());
    }

    @Test
    public void testGetRegistration() {
        // Arrange
        Registration savedRegistration = registrationService.createRegistration(testDate, testAccount, testEvent);

        // Act
        Optional<Registration> result = registrationService.getRegistration(savedRegistration.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(savedRegistration.getId(), result.get().getId());
        assertEquals(testDate, result.get().getRegistrationDate());
    }

    @Test
    public void testGetNonExistentRegistration() {
        // Act
        Optional<Registration> result = registrationService.getRegistration(999);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAllRegistrations() {
        // Arrange
        Registration registration1 = registrationService.createRegistration(testDate, testAccount, testEvent);
        Registration registration2 = registrationService.createRegistration(new Date(), testAccount, testEvent);

        // Act
        Iterable<Registration> results = registrationService.getAllRegistrations();

        // Assert
        int count = 0;
        for (Registration reg : results) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    public void testUpdateRegistration() {
        // Arrange
        Registration savedRegistration = registrationService.createRegistration(testDate, testAccount, testEvent);
        Date newDate = new Date(testDate.getTime() + 86400000); // Next day

        // Create new account and event for update
        Account newAccount = new Account();
        newAccount.setEmail("new@example.com");
        newAccount.setName("New User");
        newAccount.setPassword("newpassword123"); // Set the required password field
        // Set other required Account properties here
        newAccount = accountRepository.save(newAccount);

        Event newEvent = new Event();
        newEvent.setTitle("New Event");
        // Set other required Event properties here
        newEvent = eventRepository.save(newEvent);

        // Act
        Registration result = registrationService.updateRegistration(
            savedRegistration.getId(), 
            newDate, 
            newAccount, 
            newEvent
        );

        // Assert
        assertNotNull(result);
        assertEquals(newDate, result.getRegistrationDate());
        assertEquals(newAccount.getId(), result.getAttendee().getId());
        assertEquals(newEvent.getId(), result.getEventRegisteredFor().getId());

        // Verify changes are persisted
        Optional<Registration> storedRegistration = registrationRepository.findRegistrationById(result.getId());
        assertTrue(storedRegistration.isPresent());
        assertEquals(newDate, storedRegistration.get().getRegistrationDate());
    }

    @Test
    public void testUpdateNonExistentRegistration() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            registrationService.updateRegistration(999, testDate, testAccount, testEvent);
        });
    }

    @Test
    public void testDeleteRegistration() {
        // Arrange
        Registration savedRegistration = registrationService.createRegistration(testDate, testAccount, testEvent);
        assertTrue(registrationRepository.findRegistrationById(savedRegistration.getId()).isPresent());

        // Act
        registrationService.deleteRegistration(savedRegistration.getId());

        // Assert
        assertFalse(registrationRepository.findRegistrationById(savedRegistration.getId()).isPresent());
    }
}