package ca.mcgill.ecse321.gameorganizer.integration;

import java.sql.Date;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer; 
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    private GameOwner testHost;
    private Game testGame;
    private Event testEvent;
    private static final String BASE_URL = "/api/v1/events";
    
    @BeforeEach
public void setup() {
    // Clean repositories first
    eventRepository.deleteAll();
    gameRepository.deleteAll();
    accountRepository.deleteAll();
    
    // Create test host as a GameOwner
    testHost = new GameOwner("host", "host@example.com", passwordEncoder.encode("password123"));
    testHost = (GameOwner) accountRepository.save(testHost);
    
    // Create test game
    testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
    testGame.setOwner(testHost);
    testGame = gameRepository.save(testGame);
    
    // Create test event with a known date (e.g., 2023-03-18)
    Date knownDate = Date.valueOf("2023-03-18");
    testEvent = new Event(
        "Test Event",
        knownDate,
        "Test Location",
        "Test Description",
        10,
        testGame,
        testHost
    );
    testEvent = eventRepository.save(testEvent);
}
    
    @AfterEach
    public void cleanup() {
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    // Build URL using BASE_URL (which already includes /api/v1/events)
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Attempt to login and get a valid JWT token for the test host
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(testHost.getEmail()); // Use testHost's email
        loginRequest.setPassword("password123"); // Use the plain text password used during setup

        ResponseEntity<JwtAuthenticationResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort("/api/v1/auth/login"), // Use the correct login endpoint
            loginRequest,
            JwtAuthenticationResponse.class
        );

        // Ensure login was successful and we received a token
        if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() != null && loginResponse.getBody().getToken() != null) {
            headers.setBearerAuth(loginResponse.getBody().getToken());
        } else {
            // If authentication fails here, something is wrong with the test setup or login endpoint.
            throw new IllegalStateException("Failed to authenticate test host '" + testHost.getEmail() + "' for integration test. Status: " + loginResponse.getStatusCode());
        }
        
        return headers;
    }

    
    // ----- CREATE Tests (4 tests) -----
    
    @Test
    @Order(1)
    public void testCreateEventSuccess() {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("New Event");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("New Location");
        request.setDescription("New Description");
        request.setMaxParticipants(20);
        request.setFeaturedGame(testGame);
        request.setHost(testHost);
        
        // Create HttpEntity with request body and auth headers
        HttpEntity<CreateEventRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestEntity, // Use the entity with headers
            String.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        // Simple string checks
        assertTrue(body.contains("\"title\":\"New Event\""));
        assertTrue(body.contains("\"location\":\"New Location\""));
        assertTrue(body.contains("\"maxParticipants\":20"));
    }
    
    @Test
    @Order(2)
    public void testCreateEventWithMissingTitle() {
        CreateEventRequest request = new CreateEventRequest();
        // Title missing
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("Location");
        request.setDescription("Description");
        request.setMaxParticipants(15);
        request.setFeaturedGame(testGame);
        request.setHost(testHost);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(3)
    public void testCreateEventWithInvalidParticipants() {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Invalid Event");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("Location");
        request.setDescription("Description");
        request.setMaxParticipants(-5);
        request.setFeaturedGame(testGame);
        request.setHost(testHost);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
@Order(4)
public void testCreateEventWithNonExistentHost() {
    CreateEventRequest request = new CreateEventRequest();
    request.setTitle("Event With NonExistent Host");
    request.setDateTime(new Date(System.currentTimeMillis()));
    request.setLocation("Location");
    request.setDescription("Description");
    request.setMaxParticipants(10);
    request.setFeaturedGame(testGame);
    // Create a dummy host that is not persisted
    GameOwner dummyHost = new GameOwner("dummy", "dummy@example.com", "pass");
    request.setHost(dummyHost);

    ResponseEntity<String> response = restTemplate.postForEntity(
        createURLWithPort(BASE_URL),
        request,
        String.class
    );
    
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
}
    
    // ----- UPDATE Tests (3 tests) -----
    
    @Test
    @Order(5)
    public void testUpdateEventSuccess() {
        // Supply a valid date string in the proper format
        String validDate = "2023-03-18";
        String updateUri = BASE_URL + "/" + testEvent.getId() +
                "?title=Updated Event" +
                "&dateTime=" + validDate +
                "&location=Updated Location" +
                "&description=Updated Description" +
                "&maxParticipants=25";
        // Create HttpEntity with auth headers (no body for this PUT with params)
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
                createURLWithPort(updateUri),
                HttpMethod.PUT,
                requestEntity, // Use the entity with headers
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"title\":\"Updated Event\""));
        assertTrue(body.contains("\"location\":\"Updated Location\""));
        assertTrue(body.contains("\"description\":\"Updated Description\""));
        assertTrue(body.contains("\"maxParticipants\":25"));
    }

    
    
    @Test
    @Order(6)
    public void testUpdateEventWithInvalidData() {
        // Update event with invalid maxParticipants (negative)
        String updateUri = BASE_URL + "/" + testEvent.getId() +
                "?title=Updated Event" +
                "&location=Updated Location" +
                "&description=Updated Description" +
                "&maxParticipants=-10";
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(updateUri),
            HttpMethod.PUT,
            null,
            String.class
        );
        // According to the controller, invalid data triggers an exception and returns NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    @Test
    @Order(7)
    public void testUpdateNonExistentEvent() {
        String updateUri = BASE_URL + "/" + UUID.randomUUID() +
                "?title=Updated Event";
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(updateUri),
            HttpMethod.PUT,
            null,
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    // ----- DELETE Tests (3 tests) -----
    
    @Test
    @Order(8)
    public void testDeleteEventSuccess() {
        // Create HttpEntity with auth headers
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.DELETE,
            requestEntity, // Use the entity with headers
            Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(eventRepository.findById(testEvent.getId()).isPresent());
    }
    
    @Test
    @Order(9)
    public void testDeleteNonExistentEvent() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + UUID.randomUUID()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(10)
    public void testDeleteEventTwice() {
        ResponseEntity<Void> response1 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.DELETE,
            null,
            Void.class
        );
        assertEquals(HttpStatus.NO_CONTENT, response1.getStatusCode());
        ResponseEntity<String> response2 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }

    @Test
    @Order(11)
    public void testGetEventByIdSuccess() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            String.class
        );
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("\"title\":\"Test Event\""));
        assertTrue(body.contains("\"location\":\"Test Location\""));
    }

    @Test
    @Order(12)
    public void testGetAllEvents() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL),
            String.class
        );
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Test Event"));
    }

    @Test
    @Order(13)
    public void testGetEventsByNonExistentDate() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-date?date=2025-12-31"),
            String.class
        );
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().contains("Test Event"));
    }

    @Test
    @Order(14)
    public void testGetEventsByGameName() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-game-name?gameName=" + testGame.getName()),
            String.class
        );
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Test Game"));
    }

    @Test
    @Order(15)
    public void testGetEventsByLocation() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-location?location=Test Location"),
            String.class
        );
    
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Test Location"));
    }
    
    // ----- Additional Search Tests -----
    
    @Test
    public void testGetEventsByDate() {
        Date testDate = new Date(testEvent.getDateTime().getTime());
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-date?date=" + testDate),
            String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Test Event"));
    }
    
    @Test
    public void testGetEventsByGameId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-game-id/" + testGame.getId()),
            String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Test Game"));
    }
    
    @Test
    public void testGetEventsByHostId() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/by-host-id/" + testHost.getId()),
            String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("host@example.com"));
    }
}
