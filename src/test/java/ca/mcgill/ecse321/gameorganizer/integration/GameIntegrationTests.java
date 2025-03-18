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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dto.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dto.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GameIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository accountRepository;

    private GameOwner testOwner;
    private Game testGame;
    private static final String BASE_URL = "/games";
    private static final String VALID_EMAIL = "owner@example.com";
    private static final String VALID_USERNAME = "gameowner";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        // Create test game owner
        testOwner = new GameOwner(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
        testOwner = accountRepository.save(testOwner);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);
    }

    @AfterEach
    public void cleanup() {
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testCreateGameSuccess() {
        // Create game request
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);

        // Send request
        ResponseEntity<GameResponseDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            GameResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Game", response.getBody().getName());
        assertEquals(2, response.getBody().getMinPlayers());
        assertEquals(6, response.getBody().getMaxPlayers());
        assertEquals(VALID_EMAIL, response.getBody().getOwner().getEmail());
    }

    @Test
    public void testCreateGameWithInvalidOwner() {
        // Create game request with non-existent owner
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId("nonexistent@example.com");

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testUpdateGameSuccess() {
        // Create update request
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);

        // Send request
        ResponseEntity<GameResponseDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            GameResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Game", response.getBody().getName());
        assertEquals(3, response.getBody().getMinPlayers());
        assertEquals(8, response.getBody().getMaxPlayers());
    }

    @Test
    public void testUpdateNonExistentGame() {
        // Create update request for non-existent game
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);

        // Send request
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteGameSuccess() {
        // Send delete request
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(gameRepository.findById(testGame.getId()).isPresent());
    }

    @Test
    public void testDeleteNonExistentGame() {
        // Send delete request for non-existent game
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetGameByIdSuccess() {
        // Send get request
        ResponseEntity<GameResponseDto> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            GameResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testGame.getName(), response.getBody().getName());
        assertEquals(testGame.getMinPlayers(), response.getBody().getMinPlayers());
        assertEquals(testGame.getMaxPlayers(), response.getBody().getMaxPlayers());
    }

    @Test
    public void testGetGameByIdNotFound() {
        // Send get request for non-existent game
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/999"),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllGames() {
        // Create another test game
        Game game2 = new Game("Another Game", 3, 6, "another.jpg", new Date());
        game2.setOwner(testOwner);
        gameRepository.save(game2);

        // Send get request
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testGetGamesByOwner() {
        // Send get request with owner filter
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "?ownerId=" + VALID_EMAIL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(VALID_EMAIL, response.getBody().get(0).getOwner().getEmail());
    }

    @Test
    public void testGetGamesByPlayerCount() {
        // Send get request for games with specific player count
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/players?players=3"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().stream()
            .allMatch(game -> game.getMinPlayers() <= 3 && game.getMaxPlayers() >= 3));
    }

    @Test
    public void testGetGamesByNameContaining() {
        // Send get request with name filter
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "?namePart=Test"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().get(0).getName().contains("Test"));
    }
}
