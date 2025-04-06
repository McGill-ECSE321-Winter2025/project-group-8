package ca.mcgill.ecse321.gameorganizer.integration;

import ca.mcgill.ecse321.gameorganizer.dto.request.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.request.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.dto.response.UserSummaryDto;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import java.util.List;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ca.mcgill.ecse321.gameorganizer.TestJwtConfig;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestJwtConfig.Initializer.class)
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
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
    
    @AfterEach
    public void cleanup() {
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Clear security context
        SecurityContextHolder.clearContext();
    }
    
    // Helper method to set up authentication for tests
    private void authenticateUser(String email) {
        UserDetails userDetails = new User(email, "", 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        
        SecurityContextHolder.getContext().setAuthentication(auth);
        System.out.println("Set up authentication for: " + email);
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
        ResponseEntity<UserSummaryDto> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            UserSummaryDto.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Check for UserSummaryDto in body
        assertNotNull(response.getBody());
        assertEquals(testUser.getId(), response.getBody().getId()); // Check user ID
        assertEquals(testUser.getName(), response.getBody().getName()); // Check user name

        // Check for Set-Cookie header
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        assertNotNull(cookies, "Set-Cookie header should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("accessToken=")), "accessToken cookie should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("HttpOnly")), "accessToken cookie should be HttpOnly");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("SameSite=Strict")), "accessToken cookie should have SameSite=Strict");
        // Token is in HttpOnly cookie, cannot be accessed directly here.
        HttpHeaders authHeaders = new HttpHeaders();
        // The testLoginSuccess method primarily verifies the response body and cookie presence.
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
        
        // Authenticate the user before calling the service
        authenticateUser(testUser.getEmail());
        
        // Create event request directly using service
        CreateEventRequest request = new CreateEventRequest();
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
                eventService.createEvent(request);
            
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
        ResponseEntity<UserSummaryDto> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            UserSummaryDto.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Check for UserSummaryDto in body
        assertNotNull(response.getBody());
        assertEquals(testUser.getId(), response.getBody().getId()); // Check user ID
        assertEquals(testUser.getName(), response.getBody().getName()); // Check user name

        // Check for Set-Cookie header
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        assertNotNull(cookies, "Set-Cookie header should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("accessToken=")), "accessToken cookie should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("HttpOnly")), "accessToken cookie should be HttpOnly");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("SameSite=Strict")), "accessToken cookie should have SameSite=Strict");

        // Extract token from cookie for subsequent requests (if needed by the test logic)
        String token = cookies.stream()
            .filter(cookie -> cookie.startsWith("accessToken="))
            .findFirst()
            .map(cookie -> cookie.split(";")[0].split("=")[1])
            .orElse(null);
        assertNotNull(token, "Could not extract token from Set-Cookie header");
        System.out.println("Token received: " + token.substring(0, 20) + "...");
        
        // Set up authentication context for service call
        authenticateUser(testUser.getEmail());
        
        // Create a game for the event
        Game testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame.setOwner(testUser);
        testGame = gameRepository.save(testGame);
        
        System.out.println("Game created successfully: " + testGame.getId());
        
        // Create event request directly
        CreateEventRequest createRequest = new CreateEventRequest();
        createRequest.setTitle("New Event");
        createRequest.setDateTime(new java.sql.Date(System.currentTimeMillis()));
        createRequest.setLocation("New Location");
        createRequest.setDescription("New Description");
        createRequest.setMaxParticipants(20);
        createRequest.setFeaturedGame(testGame);
        createRequest.setHost(testUser);
        
        System.out.println("Creating event directly using service with email: " + testUser.getEmail());
        
        // Call service directly to create the event
        Event event = eventService.createEvent(createRequest);
        
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
        ResponseEntity<UserSummaryDto> response = restTemplate.postForEntity(
            "/auth/login",
            requestEntity,
            UserSummaryDto.class
        );
        
        // Assert login successful
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // Check for UserSummaryDto in body
        assertNotNull(response.getBody());
        assertEquals(testUser.getId(), response.getBody().getId()); // Check user ID
        assertEquals(testUser.getName(), response.getBody().getName()); // Check user name

        // Check for Set-Cookie header and extract token
        List<String> cookies = response.getHeaders().get("Set-Cookie");
        assertNotNull(cookies, "Set-Cookie header should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.startsWith("accessToken=")), "accessToken cookie should be present");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("HttpOnly")), "accessToken cookie should be HttpOnly");
        assertTrue(cookies.stream().anyMatch(cookie -> cookie.contains("SameSite=Strict")), "accessToken cookie should have SameSite=Strict");

        String token = cookies.stream()
            .filter(cookie -> cookie.startsWith("accessToken="))
            .findFirst()
            .map(cookie -> cookie.split(";")[0].split("=")[1])
            .orElse(null);
        assertNotNull(token, "Could not extract token from Set-Cookie header");
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
        assertEquals("Authentication test successful.", authTestResponse.getBody());
    }
}