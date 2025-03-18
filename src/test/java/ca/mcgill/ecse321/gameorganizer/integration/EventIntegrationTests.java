package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.dto.EventResponse;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    private Account testHost;
    private Game testGame;
    private Event testEvent;
    private static final String BASE_URL = "/events";

    @BeforeEach
    public void setup() {
        // Create test host
        testHost = new Account("host", "host@example.com", "password123");
        testHost = accountRepository.save(testHost);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame = gameRepository.save(testGame);

        // Create test event
        testEvent = new Event(
            "Test Event",
            new java.util.Date(),
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

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testCreateEventSuccess() {
        // Create request
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("New Event");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setLocation("New Location");
        request.setDescription("New Description");
        request.setMaxParticipants(20);
        request.setFeaturedGame(testGame);
        request.setHost(testHost);

        // Send request
        ResponseEntity<EventResponse> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            EventResponse.class
        );

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Event", response.getBody().getTitle());
        assertEquals("New Location", response.getBody().getLocation());
        assertEquals(20, response.getBody().getMaxParticipants());
    }

    @Test
    public void testGetEventByIdSuccess() {
        // Send request
        ResponseEntity<EventResponse> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            EventResponse.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEvent.getTitle(), response.getBody().getTitle());
        assertEquals(testEvent.getLocation(), response.getBody().getLocation());
        assertEquals(testEvent.getMaxParticipants(), response.getBody().getMaxParticipants());
    }

    @Test
    public void testGetEventByIdNotFound() {
        // Send request for non-existent event
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + UUID.randomUUID()),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllEvents() {
        // Create another event
        Event event2 = new Event(
            "Another Event",
            new java.util.Date(),
            "Another Location",
            "Another Description",
            15,
            testGame,
            testHost
        );
        eventRepository.save(event2);

        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testUpdateEventSuccess() {
        // Send update request
        ResponseEntity<EventResponse> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId() +
                "?title=Updated Event" +
                "&location=Updated Location" +
                "&description=Updated Description" +
                "&maxParticipants=25"),
            HttpMethod.PUT,
            null,
            EventResponse.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Event", response.getBody().getTitle());
        assertEquals("Updated Location", response.getBody().getLocation());
        assertEquals("Updated Description", response.getBody().getDescription());
        assertEquals(25, response.getBody().getMaxParticipants());
    }

    @Test
    public void testUpdateNonExistentEvent() {
        // Send update request for non-existent event
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + UUID.randomUUID() +
                "?title=Updated Event"),
            HttpMethod.PUT,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteEventSuccess() {
        // Send delete request
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testEvent.getId()),
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Verify
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertFalse(eventRepository.findById(testEvent.getId()).isPresent());
    }

    @Test
    public void testGetEventsByDate() {
        // Send request
        Date testDate = new Date(testEvent.getDateTime().getTime());
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-date?date=" + testDate),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    public void testGetEventsByGameId() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-game-id/" + testGame.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertEquals(testGame.getId(), response.getBody().get(0).getFeaturedGame().getId());
    }

    @Test
    public void testGetEventsByGameName() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-game-name?gameName=" + testGame.getName()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertEquals(testGame.getName(), response.getBody().get(0).getFeaturedGame().getName());
    }

    @Test
    public void testGetEventsByHostId() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-host-id/" + testHost.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertEquals(testHost.getId(), response.getBody().get(0).getHost().getId());
    }

    @Test
    public void testGetEventsByHostName() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-host-name?hostUsername=" + testHost.getName()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertEquals(testHost.getName(), response.getBody().get(0).getHost().getName());
    }

    @Test
    public void testGetEventsByGameMinPlayers() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-game-min-players/" + testGame.getMinPlayers()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertTrue(response.getBody().get(0).getFeaturedGame().getMinPlayers() <= testGame.getMinPlayers());
    }

    @Test
    public void testGetEventsByLocation() {
        // Send request
        ResponseEntity<List<EventResponse>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/by-location?location=Test"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<EventResponse>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        assertTrue(response.getBody().get(0).getLocation().contains("Test"));
    }
}
