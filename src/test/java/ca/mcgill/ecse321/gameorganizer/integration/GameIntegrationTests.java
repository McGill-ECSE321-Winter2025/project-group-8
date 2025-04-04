package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dto.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dto.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewSubmissionDto;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    private GameOwner testOwner;
    private Game testGame;
    private static final String BASE_URL_ALL = "/api/v1/games"; // for getAllGames
    private static final String BASE_URL = "/games"; // for other endpoints
    
    private static final String VALID_EMAIL = "owner@example.com";
    private static final String VALID_USERNAME = "gameowner";
    private static final String VALID_PASSWORD = "password123";
    
    @BeforeEach
    public void setup() {
        // Clean repositories first
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        
        // Create test game owner as a GameOwner
        testOwner = new GameOwner(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
        testOwner = (GameOwner) accountRepository.save(testOwner);
        
        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date(System.currentTimeMillis()));
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);
    }
    
    @AfterEach
    public void cleanup() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    // Helper to build URL
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
    
    // ----- CREATE Tests (4 tests) -----
    
    @Test
    @Order(1)
    public void testCreateGameSuccess() {
        // Create game request
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);
        // Set a valid category (this is required by your service)
        request.setCategory("Board Game");

        ResponseEntity<GameResponseDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            GameResponseDto.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("New Game", response.getBody().getName());
        assertEquals(2, response.getBody().getMinPlayers());
        assertEquals(6, response.getBody().getMaxPlayers());
        assertEquals(VALID_EMAIL, response.getBody().getOwner().getEmail());
    }

    
    @Test
    @Order(2)
    public void testCreateGameWithInvalidOwner() {
        // Non-existent owner should trigger a BAD_REQUEST
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId("nonexistent@example.com");
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(3)
    public void testCreateGameWithMissingName() {
        // Missing game name should result in BAD_REQUEST
        GameCreationDto request = new GameCreationDto();
        // Name is missing
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(4)
    public void testCreateGameWithInvalidPlayerCount() {
        // Invalid player count: minPlayers greater than maxPlayers
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(7); // invalid
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ----- UPDATE Tests (3 tests) -----
    
    @Test
    @Order(5)
    public void testUpdateGameSuccess() {
        // Prepare update data: update name, player counts, and image
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);
        
        ResponseEntity<GameResponseDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            GameResponseDto.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Updated Game", response.getBody().getName());
        assertEquals(3, response.getBody().getMinPlayers());
        assertEquals(8, response.getBody().getMaxPlayers());
    }
    
    @Test
    @Order(6)
    public void testUpdateGameWithInvalidData() {
        // Provide invalid data: empty name and minPlayers greater than maxPlayers.
        GameCreationDto request = new GameCreationDto();
        request.setName("");  // invalid empty name
        request.setMinPlayers(3);
        request.setMaxPlayers(2);  // invalid range
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);
        
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(7)
    public void testUpdateNonExistentGame() {
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);
        
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ----- DELETE Tests (3 tests) -----
    
    @Test
    @Order(8)
    public void testDeleteGameSuccess() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(gameRepository.findById(testGame.getId()).isPresent());
    }
    
    @Test
    @Order(9)
    public void testDeleteNonExistentGame() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(10)
    public void testDeleteGameTwice() {
        ResponseEntity<String> response1 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        ResponseEntity<String> response2 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testGame.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode());
    }
    
    // ----- Additional Search Tests -----
    
    @Test
    public void testGetGameByIdSuccess() {
        ResponseEntity<GameResponseDto> response = restTemplate.getForEntity(
            createURLWithPort("/games/" + testGame.getId()),
            GameResponseDto.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testGame.getName(), response.getBody().getName());
    }
    
    @Test
    public void testGetGameByIdNotFound() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort("/games/999"),
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    public void testGetAllGames() {
        // Use the /api/v1/games endpoint to get all games
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL_ALL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        // We already have one test game in setup
        assertEquals(1, games.size());
    }
    
    @Test
    public void testGetGamesByOwner() {
        // Filter games by owner using ownerId query parameter on /api/v1/games endpoint
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL_ALL + "?ownerId=" + VALID_EMAIL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertEquals(1, games.size());
        assertEquals(VALID_EMAIL, games.get(0).getOwner().getEmail());
    }
    
    @Test
    public void testGetGamesByPlayerCount() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/players?players=3"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        // All games returned should have minPlayers <= 3 and maxPlayers >= 3.
        assertTrue(games.stream().allMatch(game -> game.getMinPlayers() <= 3 && game.getMaxPlayers() >= 3));
    }
    
    @Test
    public void testGetGamesByNameContaining() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL_ALL + "?namePart=Test"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertFalse(games.isEmpty());
        assertTrue(games.get(0).getName().contains("Test"));
    }
    
    // ----- Advanced Search Tests -----
    
    @Test
    @Order(11)
    public void testSearchGamesWithName() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/search?name=Test"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertFalse(games.isEmpty());
        assertTrue(games.get(0).getName().contains("Test"));
    }
    
    @Test
    @Order(12)
    public void testSearchGamesWithPlayerRange() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/search?minPlayers=2&maxPlayers=4"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertTrue(games.stream().allMatch(game -> 
            game.getMinPlayers() >= 2 && game.getMaxPlayers() <= 4));
    }
    
    @Test
    @Order(13)
    public void testSearchGamesWithCategory() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/search?category=Board Game"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertTrue(games.stream().allMatch(game -> 
            game.getCategory().equals("Board Game")));
    }
    
    @Test
    @Order(14)
    public void testSearchGamesWithSorting() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/search?sort=name&order=asc"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertFalse(games.isEmpty());
        
        // Only verify sorting if we have multiple games
        if (games.size() > 1) {
            for (int i = 1; i < games.size(); i++) {
                assertTrue(games.get(i-1).getName().compareTo(games.get(i).getName()) <= 0);
            }
        }
    }
    
    // ----- Get Games By Owner Tests -----
    
    @Test
    @Order(15)
    public void testGetGamesByOwnerSuccess() {
        ResponseEntity<List<GameResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/users/" + VALID_EMAIL + "/games"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<GameResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<GameResponseDto> games = response.getBody();
        assertNotNull(games);
        assertFalse(games.isEmpty());
        assertEquals(VALID_EMAIL, games.get(0).getOwner().getEmail());
    }
    
    @Test
    @Order(16)
    public void testGetGamesByNonExistentOwner() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort("/users/nonexistent@example.com/games"),
            HttpMethod.GET,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ----- Game Reviews Tests -----
    
    @Test
    @Order(17)
    public void testGetGameReviews() {
        ResponseEntity<List<ReviewResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/" + testGame.getId() + "/reviews"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ReviewResponseDto>>() {}
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ReviewResponseDto> reviews = response.getBody();
        assertNotNull(reviews);
    }
    
    @Test
    @Order(18)
    public void testSubmitGameReview() {
        ReviewSubmissionDto reviewDto = new ReviewSubmissionDto();
        reviewDto.setRating(5);
        reviewDto.setComment("Great game!");
        reviewDto.setReviewerId(VALID_EMAIL);
        
        ResponseEntity<ReviewResponseDto> response = restTemplate.postForEntity(
            createURLWithPort("/games/" + testGame.getId() + "/reviews"),
            reviewDto,
            ReviewResponseDto.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ReviewResponseDto review = response.getBody();
        assertNotNull(review);
        assertEquals(5, review.getRating());
        assertEquals("Great game!", review.getComment());
    }
    
    @Test
    @Order(19)
    public void testGetGameRating() {
        ResponseEntity<Double> response = restTemplate.getForEntity(
            createURLWithPort("/games/" + testGame.getId() + "/rating"),
            Double.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Double rating = response.getBody();
        assertNotNull(rating);
        assertTrue(rating >= 0 && rating <= 5);
    }
    
    @Test
    @Order(20)
    public void testGetRatingForNonExistentGame() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort("/games/999/rating"),
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
