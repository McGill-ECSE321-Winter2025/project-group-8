package ca.mcgill.ecse321.gameorganizer.integration;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SimpleAuthTest {

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
    private ApplicationContext appContext;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ca.mcgill.ecse321.gameorganizer.services.EventService eventService;
    
    private GameOwner testUser;
    private static final String TEST_PASSWORD = "password123";
    
    @BeforeEach
    public void setup() {
        // Clean repositories
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test user as a GameOwner 
        testUser = new GameOwner("testuser", "testuser@example.com", passwordEncoder.encode(TEST_PASSWORD));
        testUser = (GameOwner) accountRepository.save(testUser);
        System.out.println("Created testUser with ID: " + testUser.getId() + ", email: " + testUser.getEmail());
    }
    
    @AfterEach
    public void cleanup() {
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    @Test
    public void testBasicAuthentication() {
        // Create login request
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword(TEST_PASSWORD);
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create entity
        HttpEntity<AuthenticationDTO> requestEntity = new HttpEntity<>(loginRequest, headers);
        
        // Log information for debugging
        System.out.println("Login Request:");
        System.out.println("URL: http://localhost:" + port + "/auth/login");
        System.out.println("Email: " + testUser.getEmail());
        
        // Send login request
        ResponseEntity<JwtAuthenticationResponse> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            JwtAuthenticationResponse.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        String token = response.getBody().getToken();
        assertNotNull(token);
        System.out.println("Token received: " + token.substring(0, 20) + "...");
        
        // Now try to use token for a GET request
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<?> authEntity = new HttpEntity<>(authHeaders);
        
        // Send GET request to test token
        ResponseEntity<String> getResponse = restTemplate.exchange(
            "/events",
            HttpMethod.GET,
            authEntity,
            String.class
        );
        
        // Assert GET request successful
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        System.out.println("GET request successful");
    }

    @Test
    public void testCreateEventWithService() {
        // Create a game for the event
        Game testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame.setOwner(testUser);
        testGame = gameRepository.save(testGame);
        
        System.out.println("Game created successfully: " + testGame.getId());
        
        // Create event request directly using service
        ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest request = 
            new ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest();
        request.setTitle("New Event");
        request.setDateTime(new java.sql.Date(System.currentTimeMillis()));
        request.setLocation("New Location");
        request.setDescription("New Description");
        request.setMaxParticipants(20);
        request.setFeaturedGame(testGame);
        request.setHost(testUser);
        
        // Call service directly
        System.out.println("Calling eventService.createEvent with email: " + testUser.getEmail());
        
        try {
            ca.mcgill.ecse321.gameorganizer.models.Event event = 
                eventService.createEvent(request, testUser.getEmail());
            
            // Assert event created successfully
            System.out.println("Event created successfully with ID: " + event.getId());
            assertNotNull(event);
            assertEquals("New Event", event.getTitle());
            assertEquals("New Location", event.getLocation());
            assertEquals(20, event.getMaxParticipants());
            
            // Verify event exists in the repository
            assertTrue(eventRepository.findEventById(event.getId()).isPresent());
        } catch (Exception e) {
            System.out.println("Error creating event: " + e.getMessage());
            e.printStackTrace();
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testCreateEventWithHttp() {
        // Create login request
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword(TEST_PASSWORD);
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create entity
        HttpEntity<AuthenticationDTO> requestEntity = new HttpEntity<>(loginRequest, headers);
        
        // Log information for debugging
        System.out.println("Login Request:");
        System.out.println("URL: http://localhost:" + port + "/auth/login");
        System.out.println("Email: " + testUser.getEmail());
        
        // Send login request
        ResponseEntity<JwtAuthenticationResponse> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            JwtAuthenticationResponse.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        String token = response.getBody().getToken();
        assertNotNull(token);
        System.out.println("Token received: " + token.substring(0, 20) + "...");
        
        // Create a game for the event
        Game testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame.setOwner(testUser);
        testGame = gameRepository.save(testGame);
        
        System.out.println("Game created successfully: " + testGame.getId());
        
        // Create event request directly
        ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest createRequest = 
            new ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest();
        createRequest.setTitle("New Event");
        createRequest.setDateTime(new java.sql.Date(System.currentTimeMillis()));
        createRequest.setLocation("New Location");
        createRequest.setDescription("New Description");
        createRequest.setMaxParticipants(20);
        createRequest.setFeaturedGame(testGame);
        createRequest.setHost(testUser);
        
        System.out.println("Creating event directly using service with email: " + testUser.getEmail());
        
        // Call service directly to create the event
        Event event = eventService.createEvent(createRequest, testUser.getEmail());
        
        // Verify event was created
        assertNotNull(event);
        assertNotNull(event.getId());
        assertEquals("New Event", event.getTitle());
        assertEquals("New Location", event.getLocation());
        assertEquals(testUser.getId(), event.getHost().getId());
        
        System.out.println("Event created successfully with ID: " + event.getId());
        
        // Verify it exists in the repository
        assertTrue(eventRepository.findEventById(event.getId()).isPresent());
        
        // Now test the HTTP endpoint is accessible (GET)
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("Authorization", "Bearer " + token);
        HttpEntity<?> authEntity = new HttpEntity<>(authHeaders);
        
        // Send GET request to verify access
        ResponseEntity<String> getResponse = restTemplate.exchange(
            "/events",
            HttpMethod.GET,
            authEntity,
            String.class
        );
        
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        System.out.println("HTTP GET events endpoint accessible with token");
    }

    @Test
    public void testAuthTestEndpoint() {
        // Create login request
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(testUser.getEmail());
        loginRequest.setPassword(TEST_PASSWORD);
        
        // Create headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Create entity
        HttpEntity<AuthenticationDTO> requestEntity = new HttpEntity<>(loginRequest, headers);
        
        // Log information for debugging
        System.out.println("Login Request:");
        System.out.println("URL: http://localhost:" + port + "/auth/login");
        System.out.println("Email: " + testUser.getEmail());
        
        // Send login request
        ResponseEntity<JwtAuthenticationResponse> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            JwtAuthenticationResponse.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        String token = response.getBody().getToken();
        assertNotNull(token);
        System.out.println("Token received: " + token.substring(0, 20) + "...");
        
        // Create headers with authentication
        HttpHeaders authHeaders = new HttpHeaders();
        authHeaders.setContentType(MediaType.APPLICATION_JSON);
        authHeaders.set("Authorization", "Bearer " + token);
        
        // Create entity
        HttpEntity<Void> authEntity = new HttpEntity<>(authHeaders);
        
        // Send GET request to auth-test
        ResponseEntity<String> authTestResponse = restTemplate.exchange(
            "/events/auth-test",
            HttpMethod.GET,
            authEntity,
            String.class
        );
        
        // Verify the response
        System.out.println("Auth Test Response Status: " + authTestResponse.getStatusCode());
        System.out.println("Auth Test Response Body: " + authTestResponse.getBody());
        
        assertEquals(HttpStatus.OK, authTestResponse.getStatusCode());
        assertNotNull(authTestResponse.getBody());
        assertTrue(authTestResponse.getBody().contains("Authenticated as: " + testUser.getEmail()));
    }
} 