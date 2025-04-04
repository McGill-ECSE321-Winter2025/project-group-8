package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer; 
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Add this
import org.springframework.boot.test.context.SpringBootTest;
// TestRestTemplate removed
// LocalServerPort removed
// Import removed
import org.springframework.test.web.servlet.MockMvc; // Add MockMvc
import com.fasterxml.jackson.databind.ObjectMapper; // Add ObjectMapper
import org.springframework.security.test.context.support.WithMockUser; // Add WithMockUser
import org.springframework.http.MediaType; // Add MediaType
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Add MockMvc builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Add MockMvc matchers
// ParameterizedTypeReference removed
// HttpEntity, HttpMethod, HttpStatus, ResponseEntity removed
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dto.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewSubmissionDto;
import ca.mcgill.ecse321.gameorganizer.GameorganizerApplication; // Add main app import
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
// Remove SecurityConfig import
import ca.mcgill.ecse321.gameorganizer.config.TestSecurityConfig; // Add TestSecurityConfig import
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

// Apply standard configuration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {GameorganizerApplication.class, TestConfig.class, TestSecurityConfig.class}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this
// Remove @Import
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewIntegrationTests {

        // LocalServerPort removed
        // private int port;
    
        // TestRestTemplate removed
        // @Autowired
        // private TestRestTemplate restTemplate;
    
        @Autowired
        private MockMvc mockMvc; // Inject MockMvc
    
        @Autowired
        private ObjectMapper objectMapper; // Inject ObjectMapper

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    private Account testReviewer;
    private Game testGame;
    private Review testReview;
    private static final String BASE_URL = "/api/v1/reviews";

        // createURLWithPort removed

    @BeforeEach
    public void setup() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test reviewer
        testReviewer = new Account("reviewer", "reviewer@example.com", "password123");
        testReviewer = accountRepository.save(testReviewer);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame = gameRepository.save(testGame);

        // Create test review
        testReview = new Review(4, "Great game!", new Date());
        testReview.setReviewer(testReviewer);
        testReview.setGameReviewed(testGame);
        testReview = reviewRepository.save(testReview);
    }

    @AfterEach
    public void cleanup() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // ============================================================
    // CREATE Tests (4 tests)
    // ============================================================

    // 1. Successful submission of a review
    @Test
        @Order(1)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testSubmitReviewSuccess() throws Exception { // Add throws
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.comment").value("Excellent game!"))
                    .andExpect(jsonPath("$.gameId").value(testGame.getId()))
                    .andExpect(jsonPath("$.reviewer.email").value(testReviewer.getEmail()));
    }

    // 2. Submission with invalid game id (non-existent game)
    @Test
        @Order(2)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testSubmitReviewWithInvalidGame() throws Exception { // Add throws
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            9999,  // non-existent game id
            testReviewer.getEmail()
        );
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
    }

    // 3. Submission with missing required field (simulate missing rating by using 0)
    @Test
        @Order(3)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testSubmitReviewMissingRating() throws Exception { // Add throws
        // Assume that a rating of 0 is invalid (valid ratings: 1-5)
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            0,
            "Missing rating should be invalid",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
    }

    // 4. Submission with invalid reviewer email
    @Test
        @Order(4)
        // No @WithMockUser needed? The DTO contains the email, controller should handle it.
        // Let's assume the controller uses the DTO email, not authenticated user for this specific check.
        public void testSubmitReviewWithInvalidReviewer() throws Exception { // Add throws
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            4,
            "Invalid reviewer email",
            testGame.getId(),
            "nonexistent@example.com"
        );
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
    }

    // ============================================================
    // UPDATE Tests (3 tests)
    // ============================================================

    // 1. Successful update of an existing review
    @Test
        @Order(5)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testUpdateReviewSuccess() throws Exception { // Add throws
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
                mockMvc.perform(put(BASE_URL + "/" + testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(3))
                    .andExpect(jsonPath("$.comment").value("Updated opinion"));
    }

    // 2. Update non-existent review should return NOT_FOUND
    @Test
        @Order(6)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testUpdateNonExistentReview() throws Exception { // Add throws
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
                mockMvc.perform(put(BASE_URL + "/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
    }

    // 3. Update with invalid data (e.g., invalid rating such as 6)
    @Test
        @Order(7)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testUpdateReviewWithInvalidRating() throws Exception { // Add throws
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            6, // invalid rating (assuming valid ratings are 1-5)
            "Rating out of range",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
                mockMvc.perform(put(BASE_URL + "/" + testReview.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isBadRequest());
    }

    // ============================================================
    // DELETE Tests (3 tests)
    // ============================================================

    // 1. Successful deletion of an existing review
    @Test
        @Order(8)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testDeleteReviewSuccess() throws Exception { // Add throws
                mockMvc.perform(delete(BASE_URL + "/" + testReview.getId()))
                    .andExpect(status().isOk());
                assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    // 2. Deletion of a non-existent review
    @Test
        @Order(9)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testDeleteNonExistentReview() throws Exception { // Add throws
                mockMvc.perform(delete(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());
    }

    // 3. Deletion attempted twice should return error on second attempt
    @Test
        @Order(10)
        @WithMockUser(username="reviewer@example.com", roles={"USER"}) // Need reviewer auth
        public void testDeleteReviewTwice() throws Exception { // Add throws
        // First deletion
                // First deletion
                mockMvc.perform(delete(BASE_URL + "/" + testReview.getId()))
                    .andExpect(status().isOk());
        
                // Second deletion should return NOT_FOUND
                mockMvc.perform(delete(BASE_URL + "/" + testReview.getId()))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(11)
        @WithMockUser // Basic auth
        public void testGetReviewByIdSuccess() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/" + testReview.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(testReview.getRating()))
                .andExpect(jsonPath("$.comment").value(testReview.getComment()));
    }

    @Test
        @Order(12)
        @WithMockUser // Basic auth
        public void testGetReviewsByGameId() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/games/" + testGame.getId() + "/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].comment").value(testReview.getComment()));
    }

    @Test
        @Order(13)
        @WithMockUser // Basic auth
        public void testGetReviewsByNonExistentGameName() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/game")
                    .param("gameName", "NonExistentGame"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
