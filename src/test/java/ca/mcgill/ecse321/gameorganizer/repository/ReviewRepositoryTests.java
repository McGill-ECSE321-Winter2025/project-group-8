package ca.mcgill.ecse321.gameorganizer.repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dao.GameRepository;
import ca.mcgill.ecse321.gameorganizer.dao.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Review;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewRepositoryTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GameRepository gameRepository;

    private Game testGame;

    @BeforeEach
    public void setup() {
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame = gameRepository.save(testGame);
    }

    @AfterEach
    public void clearDatabase() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadReview() {
        // Create a review instance
        Review review = new Review();
        review.setRating(4);
        review.setComment("Great strategy game!");
        
        // Initialize game's reviews list if null
        if (testGame.getReviews() == null) {
            testGame.setReviews(new ArrayList<>());
        }
        
        // Set up relationship
        testGame.getReviews().add(review);
        
        // Save the review
        review = reviewRepository.save(review);
        testGame = gameRepository.save(testGame);
        
        // Retrieve the review through game
        List<Review> retrievedReviews = reviewRepository.findByGame(testGame);
        
        // Verify the review
        assertNotNull(retrievedReviews);
        assertEquals(1, retrievedReviews.size());
        Review retrievedReview = retrievedReviews.get(0);
        assertEquals(4, retrievedReview.getRating());
        assertEquals("Great strategy game!", retrievedReview.getComment());
    }

    @Test
    public void testUpdateReview() {
        // Create and persist a review
        Review review = new Review();
        review.setRating(3);
        review.setComment("Good game");
        
        // Initialize game's reviews list if null
        if (testGame.getReviews() == null) {
            testGame.setReviews(new ArrayList<>());
        }
        
        // Set up relationship
        testGame.getReviews().add(review);
        
        // Save entities
        review = reviewRepository.save(review);
        testGame = gameRepository.save(testGame);
        
        // Update review
        review.setRating(4);
        review.setComment("Very good game after several plays");
        reviewRepository.save(review);
        
        // Retrieve and verify updates
        List<Review> retrievedReviews = reviewRepository.findByGame(testGame);
        assertNotNull(retrievedReviews);
        assertEquals(1, retrievedReviews.size());
        Review retrievedReview = retrievedReviews.get(0);
        assertEquals(4, retrievedReview.getRating());
        assertEquals("Very good game after several plays", retrievedReview.getComment());
    }

    @Test
    public void testMultipleReviewsForGame() {
        // Create first review
        Review review1 = new Review();
        review1.setRating(5);
        review1.setComment("Excellent game!");
        
        // Create second review
        Review review2 = new Review();
        review2.setRating(4);
        review2.setComment("Good game, but could be better");
        
        // Initialize game's reviews list if null
        if (testGame.getReviews() == null) {
            testGame.setReviews(new ArrayList<>());
        }
        
        // Set up relationships
        testGame.getReviews().add(review1);
        testGame.getReviews().add(review2);
        
        // Save all entities
        review1 = reviewRepository.save(review1);
        review2 = reviewRepository.save(review2);
        testGame = gameRepository.save(testGame);
        
        // Retrieve and verify
        List<Review> retrievedReviews = reviewRepository.findByGame(testGame);
        assertNotNull(retrievedReviews);
        assertEquals(2, retrievedReviews.size());
    }
}
