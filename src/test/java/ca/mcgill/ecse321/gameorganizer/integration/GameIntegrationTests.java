package ca.mcgill.ecse321.gameorganizer.integration;

import org.springframework.http.MediaType;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import java.sql.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.test.web.servlet.MockMvc; // Import MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; // Import builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Import matchers
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*; // Import security post processors
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Import AutoConfigureMockMvc
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
import ca.mcgill.ecse321.gameorganizer.models.Review;

import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
// Removed TestConfig and SecurityConfig imports as they are auto-detected with @SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Use MOCK environment
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this annotation
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameIntegrationTests {

    @Autowired
    private MockMvc mockMvc; // Inject MockMvc

    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper

    @Autowired
    private LendingRecordRepository lendingRecordRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private EventRepository eventRepository;

    // @LocalServerPort // Not needed with MockMvc
    // private int port;

    // @Autowired // Not needed with MockMvc
    // private TestRestTemplate restTemplate;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private GameOwner testOwner;
    private Game testGame;
    private static final String BASE_URL = "/games"; // Base URL for game endpoints
    private static final String VALID_EMAIL = "owner@example.com";
    private static final String VALID_USERNAME = "gameowner";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        // Clean repositories first
        reviewRepository.deleteAll();
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test game owner as a GameOwner
        testOwner = new GameOwner(VALID_USERNAME, VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD));
        testOwner = (GameOwner) accountRepository.save(testOwner);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date(System.currentTimeMillis()));
        testGame.setCategory("Board Game");
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);

        // No need to login and store token with MockMvc
    }

    @AfterEach
    public void cleanupAndClearToken() {
        reviewRepository.deleteAll();
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        // No token to clear
    }

    // Removed createURLWithPort and createAuthHeaders methods

    // ----- CREATE Tests (4 tests) -----

    @Test
    @Order(1)
    public void testCreateGameSuccess() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL); // Owner ID is required by DTO
        request.setCategory("Strategy"); // Category is required

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")) // Authenticate as owner
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("New Game"))
            .andExpect(jsonPath("$.minPlayers").value(2))
            .andExpect(jsonPath("$.maxPlayers").value(6))
            .andExpect(jsonPath("$.owner.email").value(VALID_EMAIL));
    }

    @Test
    @Order(2)
    public void testCreateGameWithInvalidOwner() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId("nonexistent@example.com"); // Invalid owner ID
        request.setCategory("Strategy");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")) // Authenticate as a valid owner
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Expect 400 because service throws IllegalArgumentException
    }

    @Test
    @Order(3)
    public void testCreateGameWithMissingName() throws Exception {
        GameCreationDto request = new GameCreationDto();
        // Name is missing
        request.setMinPlayers(2);
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);
        request.setCategory("Strategy");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void testCreateGameWithInvalidPlayerCount() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("New Game");
        request.setMinPlayers(7); // invalid
        request.setMaxPlayers(6);
        request.setImage("newgame.jpg");
        request.setOwnerId(VALID_EMAIL);
        request.setCategory("Strategy");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // ----- UPDATE Tests (3 tests) -----

    @Test
    @Order(5)
    public void testUpdateGameSuccess() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL); // DTO might require owner, though service uses auth
        request.setCategory("Updated Category"); // Include category if needed

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testGame.getId())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")) // Authenticate as owner
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Game"))
            .andExpect(jsonPath("$.minPlayers").value(3))
            .andExpect(jsonPath("$.maxPlayers").value(8));
    }

    @Test
    @Order(6)
    public void testUpdateGameWithInvalidData() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("");  // invalid empty name
        request.setMinPlayers(3);
        request.setMaxPlayers(2);  // invalid range
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);
        request.setCategory("Invalid Data");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testGame.getId())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentGame() throws Exception {
        GameCreationDto request = new GameCreationDto();
        request.setName("Updated Game");
        request.setMinPlayers(3);
        request.setMaxPlayers(8);
        request.setImage("updated.jpg");
        request.setOwnerId(VALID_EMAIL);
        request.setCategory("Non-existent");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/999") // Non-existent ID
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    // ----- DELETE Tests (3 tests) -----

    @Test
    @Order(8)
    public void testDeleteGameSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testGame.getId())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER"))) // Authenticate as owner
            .andExpect(status().isOk()); // Expect 200 OK

        assertFalse(gameRepository.findById(testGame.getId()).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentGame() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/999") // Non-existent ID
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    @Test
    @Order(10)
    public void testDeleteGameTwice() throws Exception {
        // First delete
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testGame.getId())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")))
            .andExpect(status().isOk()); // Expect 200 OK

        // Second delete should fail (not found) -> 400 BAD_REQUEST
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testGame.getId())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")))
            .andExpect(status().isBadRequest());
    }

    // ----- Additional Search Tests -----

    @Test
    @Order(11) // Renumbered
    public void testGetGameByIdSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testGame.getId())
                .with(anonymous())) // Assuming GET by ID is public
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value(testGame.getName()));
    }

    @Test
    @Order(12) // Renumbered
    public void testGetGameByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/999")
                .with(anonymous()))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    @Test
    @Order(13) // Renumbered
    public void testGetAllGames() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .with(anonymous())) // Assuming GET all is public
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1)); // Only the test game exists
    }

    @Test
    @Order(14) // Renumbered
    public void testGetGamesByOwner() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("ownerId", VALID_EMAIL)
                .with(anonymous())) // Assuming public access with filter
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].owner.email").value(VALID_EMAIL));
    }

    @Test
    @Order(15) // Renumbered
    public void testGetGamesByPlayerCount() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/games/players") // Specific endpoint
                .param("players", "3")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1)); // Test game fits 2-4 players
    }

    @Test
    @Order(16) // Renumbered
    public void testGetGamesByNameContaining() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("namePart", "Test")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Test Game"));
    }

    // ----- Advanced Search Tests -----

    @Test
    @Order(17) // Renumbered
    public void testSearchGamesWithName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/games/search")
                .param("name", "Test")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(18) // Renumbered
    public void testSearchGamesWithPlayerRange() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/games/search")
                .param("minPlayers", "2")
                .param("maxPlayers", "4")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(19) // Renumbered
    public void testSearchGamesWithCategory() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/games/search")
                .param("category", "Board Game")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @Order(20) // Renumbered
    public void testSearchGamesWithSorting() throws Exception {
        // Add another game for sorting test
        Game game2 = new Game("Another Game", 1, 2, "ag.jpg", new java.util.Date());
        game2.setOwner(testOwner);
        game2.setCategory("Card Game");
        gameRepository.save(game2);

        mockMvc.perform(MockMvcRequestBuilders.get("/games/search")
                .param("sort", "name")
                .param("order", "asc")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("Another Game")) // Sorted alphabetically
            .andExpect(jsonPath("$[1].name").value("Test Game"));
    }

    // ----- Get Games By Owner Tests -----

    @Test
    @Order(21) // Renumbered
    public void testGetGamesByOwnerSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/" + VALID_EMAIL + "/games")
                .with(anonymous())) // Assuming public access
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].owner.email").value(VALID_EMAIL));
    }

    @Test
    @Order(22) // Renumbered
    public void testGetGamesByNonExistentOwner() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/users/nonexistent@example.com/games")
                .with(anonymous()))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    // ----- Game Reviews Tests -----

    @Test
    @Order(23) // Renumbered
    public void testGetGameReviews() throws Exception {
        // Add a review first
        Review review = new Review(5, "Great!", new java.util.Date());
        review.setGameReviewed(testGame);
        review.setReviewer(testOwner); // Owner reviewing own game for simplicity here
        reviewRepository.save(review);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testGame.getId() + "/reviews")
                .with(anonymous())) // Assuming public access
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].comment").value("Great!"));
    }

    @Test
    @Order(24) // Renumbered
    public void testSubmitGameReview() throws Exception {
        ReviewSubmissionDto reviewDto = new ReviewSubmissionDto();
        reviewDto.setRating(5);
        reviewDto.setComment("Great game!");
        reviewDto.setReviewerId(VALID_EMAIL); // Reviewer is the owner in this test

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testGame.getId() + "/reviews")
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER", "GAME_OWNER")) // Authenticate as reviewer
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.comment").value("Great game!"));
    }

    @Test
    @Order(25) // Renumbered
    public void testGetGameRating() throws Exception {
         // Add a review first
        Review review = new Review(4, "Good", new java.util.Date());
        review.setGameReviewed(testGame);
        review.setReviewer(testOwner);
        reviewRepository.save(review);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testGame.getId() + "/rating")
                .with(anonymous())) // Assuming public access
            .andExpect(status().isOk())
            .andExpect(content().string("4.0")); // Expecting the average rating
    }

    @Test
    @Order(26) // Renumbered
    public void testGetRatingForNonExistentGame() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/999/rating")
                .with(anonymous()))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }
}
