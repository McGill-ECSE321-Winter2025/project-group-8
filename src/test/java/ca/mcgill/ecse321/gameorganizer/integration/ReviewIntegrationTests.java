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

import ca.mcgill.ecse321.gameorganizer.dto.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.ReviewSubmissionDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    @BeforeEach
    public void setup() {
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

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testSubmitReviewSuccess() {
        // Create review request
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            testGame.getId(),
            testReviewer.getEmail()
        );

        // Send request
        ResponseEntity<ReviewResponseDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            ReviewResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getRating());
        assertEquals("Excellent game!", response.getBody().getComment());
        assertEquals(testGame.getId(), response.getBody().getGameId());
        assertEquals(testReviewer.getEmail(), response.getBody().getReviewerId());
    }

    @Test
    public void testSubmitReviewWithInvalidGame() {
        // Create review request with non-existent game
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            5,
            "Excellent game!",
            999,
            testReviewer.getEmail()
        );

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
    public void testGetReviewByIdSuccess() {
        // Send request
        ResponseEntity<ReviewResponseDto> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            ReviewResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testReview.getRating(), response.getBody().getRating());
        assertEquals(testReview.getComment(), response.getBody().getComment());
        assertEquals(testGame.getId(), response.getBody().getGameId());
        assertEquals(testReviewer.getEmail(), response.getBody().getReviewerId());
    }

    @Test
    public void testGetReviewByIdNotFound() {
        // Send request for non-existent review
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/999"),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetReviewsByGameId() {
        // Send request
        ResponseEntity<List<ReviewResponseDto>> response = restTemplate.exchange(
            createURLWithPort("/games/" + testGame.getId() + "/reviews"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ReviewResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testGame.getId(), response.getBody().get(0).getGameId());
    }

    @Test
    public void testGetReviewsByGameName() {
        // Send request
        ResponseEntity<List<ReviewResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/game?gameName=" + testGame.getName()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<ReviewResponseDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(testGame.getName(), response.getBody().get(0).getGameTitle());
    }

    @Test
    public void testUpdateReviewSuccess() {
        // Create update request
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );

        // Send request
        ResponseEntity<ReviewResponseDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            ReviewResponseDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getRating());
        assertEquals("Updated opinion", response.getBody().getComment());
    }

    @Test
    public void testUpdateNonExistentReview() {
        // Create update request
        ReviewSubmissionDto request = new ReviewSubmissionDto(
            3,
            "Updated opinion",
            testGame.getId(),
            testReviewer.getEmail()
        );

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
    public void testDeleteReviewSuccess() {
        // Send delete request
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testReview.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(reviewRepository.findById(testReview.getId()).isPresent());
    }

    @Test
    public void testDeleteNonExistentReview() {
        // Send delete request for non-existent review
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
