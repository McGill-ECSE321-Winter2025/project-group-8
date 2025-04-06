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

import ca.mcgill.ecse321.gameorganizer.dto.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewSubmissionDto;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ReviewIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    private Account testReviewer;
    private Game testGame;
    private Review testReview;
    private static final String BASE_URL = "/reviews";

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

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
    public void testSubmitReviewSuccess() {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
        ResponseEntity<ReviewResponseDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            ReviewResponseDto.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        ReviewResponseDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(5, dto.getRating());
        assertEquals("Excellent game!", dto.getComment());
        assertEquals(testGame.getId(), dto.getGameId());
        assertEquals(testReviewer.getEmail(), dto.getReviewer().getEmail());
    }

    // 2. Submission with invalid game id (non-existent game)
    @Test
    @Order(2)
    public void testSubmitReviewWithInvalidGame() {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            9999,  // non-existent game id
            testReviewer.getEmail()
        );
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 3. Submission with missing required field (simulate missing rating by using 0)
    @Test
    @Order(3)
    public void testSubmitReviewMissingRating() {
        // Assume that a rating of 0 is invalid (valid ratings: 1-5)
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            0,
            "Missing rating should be invalid",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // 4. Submission with invalid reviewer email
    @Test
    @Order(4)
    public void testSubmitReviewWithInvalidReviewer() {
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            4,
            "Invalid reviewer email",
            testGame.getId(),
            "nonexistent@example.com"
        );
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ============================================================
    // UPDATE Tests (3 tests)
    // ============================================================

    // 1. Successful update of an existing review
    @Test
    @Order(5)
    public void testUpdateReviewSuccess() {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
        ResponseEntity<ReviewResponseDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            ReviewResponseDto.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponseDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(3, dto.getRating());
        assertEquals("Updated opinion", dto.getComment());
    }

    // 2. Update non-existent review should return NOT_FOUND
    @Test
    @Order(6)
    public void testUpdateNonExistentReview() {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            String.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // 3. Update with invalid data (e.g., invalid rating such as 6)
    @Test
    @Order(7)
    public void testUpdateReviewWithInvalidRating() {
        ReviewSubmissionDto updateRequest = new ReviewSubmissionDto(
            6, // invalid rating (assuming valid ratings are 1-5)
            "Rating out of range",
            testGame.getId(),
            testReviewer.getEmail()
        );
        
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(updateRequest),
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ============================================================
    // DELETE Tests (3 tests)
    // ============================================================

    // 1. Successful deletion of an existing review
    @Test
    @Order(8)
    public void testDeleteReviewSuccess() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    // 2. Deletion of a non-existent review
    @Test
    @Order(9)
    public void testDeleteNonExistentReview() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // 3. Deletion attempted twice should return error on second attempt
    @Test
    @Order(10)
    public void testDeleteReviewTwice() {
        // First deletion
        ResponseEntity<String> response1 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        
        // Second deletion should return NOT_FOUND
        ResponseEntity<String> response2 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    }

    @Test
    @Order(11)
    public void testGetReviewByIdSuccess() {
        ResponseEntity<ReviewResponseDto> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            ReviewResponseDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ReviewResponseDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(testReview.getRating(), dto.getRating());
        assertEquals(testReview.getComment(), dto.getComment());
    }

    @Test
    @Order(12)
    public void testGetReviewsByGameId() {
        ResponseEntity<List<ReviewResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/games/" + testGame.getId() + "/reviews"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ReviewResponseDto>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<ReviewResponseDto> reviews = response.getBody();
        assertNotNull(reviews);
        assertFalse(reviews.isEmpty());
        assertEquals(testReview.getComment(), reviews.get(0).getComment());
    }

    @Test
    @Order(13)
    public void testGetReviewsByNonExistentGameName() {
        ResponseEntity<List<ReviewResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/game?gameName=NonExistentGame"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ReviewResponseDto>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
