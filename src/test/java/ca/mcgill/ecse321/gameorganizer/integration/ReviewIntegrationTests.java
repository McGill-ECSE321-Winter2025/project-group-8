package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Import matchers
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*; // Import security post processors

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.http.MediaType; // Added for ContentType
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc; // Import MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; // Import builders
import com.fasterxml.jackson.databind.ObjectMapper; // Import ObjectMapper
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Import AutoConfigureMockMvc
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

// Removed TestRestTemplate, @LocalServerPort, @Import, HttpEntity, HttpHeaders imports

import ca.mcgill.ecse321.gameorganizer.dto.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewSubmissionDto;
// Removed TestConfig and SecurityConfig imports
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner; // Import GameOwner
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Use MOCK environment
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this annotation
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewIntegrationTests {

    // @LocalServerPort // Not needed with MockMvc
    // private int port;

    @Autowired
    private MockMvc mockMvc; // Inject MockMvc

    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper

    // @Autowired // Not needed with MockMvc
    // private TestRestTemplate restTemplate;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account testReviewer;
    private Game testGame;
    private Review testReview;
    private static final String BASE_URL = "/reviews";
    private static final String TEST_REVIEWER_EMAIL = "reviewer@example.com"; // Added constant
    private static final String TEST_PASSWORD = "password123"; // Added constant

    @BeforeEach
    public void setup() {
        // Clean repositories first - order might matter depending on constraints
        reviewRepository.deleteAll();
        gameRepository.deleteAll(); // Delete games before owners due to FK constraint
        accountRepository.deleteAll();

        // Create test reviewer
        testReviewer = new Account("reviewer", TEST_REVIEWER_EMAIL, passwordEncoder.encode(TEST_PASSWORD));
        testReviewer = accountRepository.save(testReviewer);

        // Create a GameOwner for the test game with a unique email per execution
        String uniqueDummyEmail = "dummy-" + System.currentTimeMillis() + "@owner.com";
        GameOwner gameOwner = new GameOwner("dummyOwner", uniqueDummyEmail, passwordEncoder.encode("dummyPwd"));
        gameOwner = accountRepository.save(gameOwner); // Save the owner

        // Create test game and assign the GameOwner
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame.setOwner(gameOwner); // Assign the saved GameOwner
        testGame = gameRepository.save(testGame);

        // Create test review
        testReview = new Review(4, "Great game!", new Date());
        testReview.setReviewer(testReviewer);
        testReview.setGameReviewed(testGame);
        testReview = reviewRepository.save(testReview);

        // No need to login and store token with MockMvc
    }

    @AfterEach
    public void cleanupAndClearToken() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
        // No token to clear
    }

    // ============================================================
    // CREATE Tests (4 tests)
    // ============================================================

    @Test
    @Order(1)
    public void testSubmitReviewSuccess() throws Exception {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            testGame.getId(),
            TEST_REVIEWER_EMAIL // Use constant
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER")) // Authenticate as reviewer
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.rating").value(5))
            .andExpect(jsonPath("$.comment").value("Excellent game!"))
            .andExpect(jsonPath("$.gameId").value(testGame.getId()))
            .andExpect(jsonPath("$.reviewer.email").value(TEST_REVIEWER_EMAIL));
    }

    @Test
    @Order(2)
    public void testSubmitReviewWithInvalidGame() throws Exception {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            9999,  // non-existent game id
            TEST_REVIEWER_EMAIL
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    @Test
    @Order(3)
    public void testSubmitReviewMissingRating() throws Exception {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            0, // Invalid rating
            "Missing rating should be invalid",
            testGame.getId(),
            TEST_REVIEWER_EMAIL
        );

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void testSubmitReviewWithInvalidReviewer() throws Exception {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            4,
            "Invalid reviewer email",
            testGame.getId(),
            "nonexistent@example.com" // Non-existent email
        );

        // Authenticate as the valid user, but the DTO contains an invalid reviewer email
        // The service logic should catch this based on the DTO content.
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    // ============================================================
    // UPDATE Tests (3 tests)
    // ============================================================

    @Test
    @Order(5)
    public void testUpdateReviewSuccess() throws Exception {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            TEST_REVIEWER_EMAIL // Reviewer ID might be needed by DTO, but service uses auth
        );

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testReview.getId())
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER")) // Authenticate as the reviewer
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(3))
            .andExpect(jsonPath("$.comment").value("Updated opinion"));
    }

    @Test
    @Order(6)
    public void testUpdateNonExistentReview() throws Exception {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            TEST_REVIEWER_EMAIL
        );

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/999") // Non-existent ID
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isNotFound()); // Service throws ResourceNotFoundException -> 404
    }

    @Test
    @Order(7)
    public void testUpdateReviewWithInvalidRating() throws Exception {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            6, // invalid rating
            "Rating out of range",
            testGame.getId(),
            TEST_REVIEWER_EMAIL
        );

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testReview.getId())
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isBadRequest()); // Service throws IllegalArgumentException -> 400
    }

    // ============================================================
    // DELETE Tests (3 tests)
    // ============================================================

    @Test
    @Order(8)
    public void testDeleteReviewSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testReview.getId())
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER"))) // Authenticate as reviewer
            .andExpect(status().isOk()); // Expect 200 OK

        assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentReview() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/999") // Non-existent ID
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER")))
            .andExpect(status().isNotFound()); // Service throws ResourceNotFoundException -> 404
    }

    @Test
    @Order(10)
    public void testDeleteReviewTwice() throws Exception {
        // First deletion
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testReview.getId())
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER")))
            .andExpect(status().isOk()); // Expect 200 OK

        // Second deletion should fail (not found) -> 404 NOT_FOUND
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testReview.getId())
                .with(user(TEST_REVIEWER_EMAIL).password(TEST_PASSWORD).roles("USER")))
            .andExpect(status().isNotFound());
    }

    // ============================================================
    // GET Tests
    // ============================================================

    @Test
    @Order(11)
    public void testGetReviewByIdSuccess() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testReview.getId())
                .with(anonymous())) // Assuming GET is public
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rating").value(testReview.getRating()))
            .andExpect(jsonPath("$.comment").value(testReview.getComment()));
    }

    @Test
    @Order(12)
    public void testGetReviewsByGameId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/games/" + testGame.getId() + "/reviews") // Correct endpoint
                .with(anonymous())) // Assuming public access
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].comment").value(testReview.getComment()));
    }

    @Test
    @Order(13)
    public void testGetReviewsByNonExistentGameName() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/reviews/game") // Correct endpoint
                .param("gameName", "NonExistentGame")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0)); // Expect empty array;
    }
}
