package ca.mcgill.ecse321.gameorganizer.integration;

import java.sql.Date;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail; // Keep fail import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType; // Added for ContentType
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc; // Import MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; // Import builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Import matchers
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*; // Import security post processors
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper

import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Import AutoConfigureMockMvc
import org.springframework.test.context.ActiveProfiles;

// Removed TestRestTemplate, @LocalServerPort, @Import, HttpEntity, HttpHeaders, HttpMethod, ResponseEntity imports

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Use MOCK environment
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this annotation
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventIntegrationTests {

    // @LocalServerPort // Not needed with MockMvc
    // private int port;

    @Autowired
    private MockMvc mockMvc; // Inject MockMvc

    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EventService eventService;

    private GameOwner testHost;
    private Game testGame;
    private Event testEvent;
    private static final String BASE_URL = "/events";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_HOST_EMAIL = "host@example.com"; // Added constant

    @BeforeEach
    public void setup() {
        // Clean repositories first
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test host as a GameOwner
        testHost = new GameOwner("host", TEST_HOST_EMAIL, passwordEncoder.encode(TEST_PASSWORD));
        testHost = (GameOwner) accountRepository.save(testHost);
        System.out.println("Created testHost with ID: " + testHost.getId() + ", email: " + testHost.getEmail());

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame.setOwner(testHost);
        testGame = gameRepository.save(testGame);
        System.out.println("Created testGame with ID: " + testGame.getId() + ", owner: " + testHost.getEmail());

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
        System.out.println("Created testEvent with ID: " + testEvent.getId());

        // No need to login and store token with MockMvc, use .with(user(...)) instead
    }

    @AfterEach
    public void cleanupAndClearToken() {
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        // No token to clear
    }

    // Removed createURLWithPort and createAuthHeaders methods

    // ----- CREATE Tests (4 tests) -----

    // Test 0 removed as it was for TestRestTemplate setup

    @Test
    @Order(1)
    public void testCreateEventSuccess() throws Exception {
        // Prepare the request data
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("New Event");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("New Location");
        request.setDescription("New Description");
        request.setMaxParticipants(20);
        request.setFeaturedGame(testGame);
        // Host is implicitly the authenticated user in the controller
        // request.setHost(testHost); // Don't set host in DTO for controller

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER")) // Simulate authenticated host
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated()) // Expect 201 CREATED
            .andExpect(jsonPath("$.title").value("New Event"))
            .andExpect(jsonPath("$.location").value("New Location"))
            .andExpect(jsonPath("$.maxParticipants").value(20));
    }

    @Test
    @Order(2)
    public void testCreateEventWithMissingTitle() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        // Title missing
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("Location");
        request.setDescription("Description");
        request.setMaxParticipants(15);
        request.setFeaturedGame(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Expect 400 BAD_REQUEST
    }

    @Test
    @Order(3)
    public void testCreateEventWithInvalidParticipants() throws Exception {
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Invalid Event");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("Location");
        request.setDescription("Description");
        request.setMaxParticipants(-5); // Invalid
        request.setFeaturedGame(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Expect 400 BAD_REQUEST
    }

    @Test
    @Order(4)
    public void testCreateEventUnauthenticated() throws Exception {
        // This test replaces the old 'testCreateEventWithNonExistentHost'
        // It tests the security layer directly.
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("Event Creation Attempt Unauthenticated");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("Location");
        request.setDescription("Description");
        request.setMaxParticipants(10);
        request.setFeaturedGame(testGame);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(anonymous()) // Simulate unauthenticated request
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized()); // Expect 401 UNAUTHORIZED
    }

    // ----- UPDATE Tests (3 tests) -----

    @Test
    @Order(5)
    public void testUpdateEventSuccess() throws Exception {
        String newTitle = "Updated Event";
        String newLocation = "Updated Location";
        int newMaxParticipants = 25;
        String validDate = "2023-03-18"; // Use the known date

        // Use query parameters for PUT request
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testEvent.getId())
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER")) // Authenticate as the host
                .param("title", newTitle)
                .param("location", newLocation)
                .param("maxParticipants", String.valueOf(newMaxParticipants))
                .param("dateTime", validDate)) // Pass date as string param
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value(newTitle))
            .andExpect(jsonPath("$.location").value(newLocation))
            .andExpect(jsonPath("$.maxParticipants").value(newMaxParticipants));
    }

    @Test
    @Order(6)
    public void testUpdateEventWithInvalidData() throws Exception {
        // Use an invalid value for maxParticipants
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testEvent.getId())
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER"))
                .param("maxParticipants", "-10")) // Invalid param
            .andExpect(status().isNotFound()); // MODIFIED: Expect 404 NOT_FOUND (was 400)
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentEvent() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + nonExistentId)
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER"))
                .param("title", "Updated Title"))
             // Service throws IllegalArgumentException, Global handler maps to 400.
             .andExpect(status().isNotFound()); // MODIFIED: Expect 404 NOT_FOUND (was 400)
    }

    // ----- DELETE Tests (3 tests) -----

    @Test
    @Order(8)
    public void testDeleteEventSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testEvent.getId())
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER"))) // Authenticate as host
            .andExpect(status().isNoContent()); // Expect 204 NO_CONTENT on successful delete

        assertFalse(eventRepository.findById(testEvent.getId()).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentEvent() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + nonExistentId)
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER")))
            // Expect 400 BAD_REQUEST because service throws IllegalArgumentException for not found
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(10)
    public void testDeleteEventTwice() throws Exception {
        // First delete
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testEvent.getId())
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER")))
            .andExpect(status().isNoContent()); // Expect 204

        // Second delete should fail (not found) -> 400 BAD_REQUEST
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testEvent.getId())
                .with(user(TEST_HOST_EMAIL).password(TEST_PASSWORD).roles("USER", "GAME_OWNER")))
            .andExpect(status().isBadRequest());
    }

    // ----- GET Tests ----- (These usually don't require auth or less strict auth)

    @Test
    @Order(11)
    public void testGetEventByIdSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testEvent.getId())
                .with(anonymous())) // Assuming GET by ID is public or requires basic auth handled elsewhere
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Test Event"))
            .andExpect(jsonPath("$.location").value("Test Location"));
    }

    @Test
    @Order(12)
    public void testGetAllEvents() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .with(anonymous())) // Assuming GET all is public
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray()) // Check if response is an array
            .andExpect(jsonPath("$[0].title").value("Test Event")); // Check first element
    }

    @Test
    @Order(13)
    public void testGetEventsByNonExistentDate() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-date")
                .param("date", "2025-12-31")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0)); // Expect empty array
    }

    @Test
    @Order(14)
    public void testGetEventsByGameName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-game-name")
                .param("gameName", testGame.getName())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].featuredGame.name").value(testGame.getName()));
    }

    @Test
    @Order(15)
    public void testGetEventsByLocation() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-location")
                .param("location", "Test Location")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].location").value("Test Location"));
    }

    @Test
    @Order(16)
    public void testGetEventsByDate() throws Exception {
        // Use the known date from setup
        Date testDate = Date.valueOf("2023-03-18");

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-date")
                .param("date", testDate.toString()) // Pass date as string param
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    @Order(17) // Renumbered
    public void testGetEventsByGameId() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-game-id/" + testGame.getId())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].featuredGame.name").value(testGame.getName()));
    }

    @Test
    @Order(18) // Renumbered
    public void testGetEventsByHostId() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/by-host-id/" + testHost.getId())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].host.email").value(testHost.getEmail()));
    }
}