package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RegistrationIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private Account testUser;
    private Game testGame;
    private Event testEvent;
    private HttpHeaders headers;
    private static final String BASE_URL = "/registrations";

    @BeforeEach
    public void setup() {
        // Create test user
        testUser = new Account("testuser", "test@example.com", "password123");
        testUser = accountRepository.save(testUser);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame = gameRepository.save(testGame);

        // Create test event
        testEvent = new Event("Test Event", new Date(), "Test Location", "Test Description", 10, testGame);
        testEvent = eventRepository.save(testEvent);

        // Set up authentication headers
        headers = new HttpHeaders();
        headers.set("User-Id", String.valueOf(testUser.getId()));
    }

    @AfterEach
    public void cleanup() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testRegisterForEvent() {
        // Send registration request
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            Void.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(testUser.getName());
        assertFalse(registrations.isEmpty());
        assertEquals(testEvent.getId(), registrations.get(0).getEventRegisteredFor().getId());
    }

    @Test
    public void testUnregisterFromEvent() {
        // First register for the event
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            Void.class
        );

        // Then unregister
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.DELETE,
            requestEntity,
            Void.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeName(testUser.getName());
        assertTrue(registrations.isEmpty());
    }

    @Test
    public void testGetUserRegistrations() {
        // First register for the event
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            Void.class
        );

        // Get user's registrations
        ResponseEntity<Registration[]> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/my-registrations"),
            HttpMethod.GET,
            requestEntity,
            Registration[].class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(testEvent.getId(), response.getBody()[0].getEventRegisteredFor().getId());
    }

    @Test
    public void testGetEventRegistrations() {
        // First register for the event
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            Void.class
        );

        // Get event's registrations
        ResponseEntity<Registration[]> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.GET,
            requestEntity,
            Registration[].class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().length);
        assertEquals(testUser.getName(), response.getBody()[0].getAttendee().getName());
    }

    @Test
    public void testRegisterForFullEvent() {
        // Fill up the event
        testEvent.setMaxParticipants(1);
        Account otherUser = new Account("other", "other@example.com", "password123");
        otherUser = accountRepository.save(otherUser);
        Registration registration = new Registration(new Date());
        registration.setAttendee(otherUser);
        registration.setEventRegisteredFor(testEvent);
        registrationRepository.save(registration);

        // Try to register
        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testUnauthorizedAccess() {
        // Send request without auth headers
        HttpEntity<?> requestEntity = new HttpEntity<>(new HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
