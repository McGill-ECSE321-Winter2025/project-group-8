package ca.mcgill.ecse321.gameorganizer.repository;

import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Review;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")

public class ReviewRepositoryTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void clearDatabase() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // Read Tests

    @Test
    public void testReadReviewById() {
        Review review = new Review(5, "Test review", new Date());
        reviewRepository.save(review);

        assertTrue(reviewRepository.findReviewById(review.getId()).isPresent());
        assertEquals("Test review", reviewRepository.findReviewById(review.getId()).get().getComment());
    }

    @Test
    public void testReadReviewAttributes() {
        Date currentDate = new Date();
        Review review = new Review(4, "Read attributes test", currentDate);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertEquals(4, found.getRating());
        assertEquals("Read attributes test", found.getComment());
        assertEquals(currentDate, found.getDateSubmitted());
    }

    @Test
    public void testReadReviewGameReference() {
        // Create game with owner
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);

        // Create and link review
        Review review = new Review(5, "Game reference test", new Date());
        review.setGameReviewed(game);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertNotNull(found.getGameReviewed());
        assertEquals("Test Game", found.getGameReviewed().getName());
    }

    @Test
    public void testReadReviewReviewerReference() {
        Account reviewer = new Account();
        reviewer.setName("reviewer");
        reviewer.setEmail("reviewer@test.com");
        reviewer.setPassword("password");
        reviewer = accountRepository.save(reviewer);

        Review review = new Review(5, "Reviewer reference test", new Date());
        review.setReviewer(reviewer);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertNotNull(found.getReviewer());
        assertEquals("reviewer", found.getReviewer().getName());
    }

    // Write Tests

    @Test
    public void testWriteReviewAttributes() {
        Review review = new Review(3, "Initial comment", new Date());
        reviewRepository.save(review);

        // Modify attributes
        review.setRating(5);
        review.setComment("Updated comment");
        Date newDate = new Date();
        review.setDateSubmitted(newDate);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertEquals(5, found.getRating());
        assertEquals("Updated comment", found.getComment());
        assertEquals(newDate, found.getDateSubmitted());
    }

    @Test
    public void testWriteReviewGameReference() {
        // Create initial game
        GameOwner owner1 = new GameOwner("owner1", "owner1@test.com", "password");
        owner1 = (GameOwner) accountRepository.save(owner1);
        Game game1 = new Game("Game 1", 2, 4, "game1.jpg", new Date());
        game1.setOwner(owner1);
        game1 = gameRepository.save(game1);

        // Create second game
        GameOwner owner2 = new GameOwner("owner2", "owner2@test.com", "password");
        owner2 = (GameOwner) accountRepository.save(owner2);
        Game game2 = new Game("Game 2", 3, 6, "game2.jpg", new Date());
        game2.setOwner(owner2);
        game2 = gameRepository.save(game2);

        // Create review with first game
        Review review = new Review(4, "Game reference write test", new Date());
        review.setGameReviewed(game1);
        reviewRepository.save(review);

        // Update to second game
        review.setGameReviewed(game2);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertEquals("Game 2", found.getGameReviewed().getName());
    }

    @Test
    public void testWriteReviewReviewerReference() {
        // Create initial reviewer
        Account reviewer1 = new Account();
        reviewer1.setName("reviewer1");
        reviewer1.setEmail("reviewer1@test.com");
        reviewer1.setPassword("password");
        reviewer1 = accountRepository.save(reviewer1);

        // Create second reviewer
        Account reviewer2 = new Account();
        reviewer2.setName("reviewer2");
        reviewer2.setEmail("reviewer2@test.com");
        reviewer2.setPassword("password");
        reviewer2 = accountRepository.save(reviewer2);

        // Create review with first reviewer
        Review review = new Review(4, "Reviewer reference write test", new Date());
        review.setReviewer(reviewer1);
        reviewRepository.save(review);

        // Update to second reviewer
        review.setReviewer(reviewer2);
        reviewRepository.save(review);

        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertEquals("reviewer2", found.getReviewer().getName());
    }

    // Object Persistence Tests

    @Test
    public void testPersistAndLoadCompleteReview() {
        // Create all required objects
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);

        Game game = new Game("Complete Game", 2, 6, "complete.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);

        Account reviewer = new Account();
        reviewer.setName("reviewer");
        reviewer.setEmail("reviewer@test.com");
        reviewer.setPassword("password");
        reviewer = accountRepository.save(reviewer);

        // Create complete review
        Date currentDate = new Date();
        Review review = new Review(5, "Complete persistence test", currentDate);
        review.setGameReviewed(game);
        review.setReviewer(reviewer);
        reviewRepository.save(review);

        // Flush persistence context
        reviewRepository.flush();

        // Verify complete object persistence
        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertEquals(5, found.getRating());
        assertEquals("Complete persistence test", found.getComment());
        assertEquals(currentDate, found.getDateSubmitted());
        assertEquals("Complete Game", found.getGameReviewed().getName());
        assertEquals("reviewer", found.getReviewer().getName());
    }

    @Test
    public void testCascadingDelete() {
        // Create review with game and reviewer
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);

        Game game = new Game("Delete Test Game", 2, 4, "delete.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);

        Account reviewer = new Account();
        reviewer.setName("reviewer");
        reviewer.setEmail("reviewer@test.com");
        reviewer.setPassword("password");
        reviewer = accountRepository.save(reviewer);

        Review review = new Review(4, "Cascade delete test", new Date());
        review.setGameReviewed(game);
        review.setReviewer(reviewer);
        reviewRepository.save(review);

        // Delete game
        gameRepository.delete(game);

        // Verify review still exists but game reference is null
        Review found = reviewRepository.findReviewById(review.getId()).get();
        assertNotNull(found);
        assertNull(found.getGameReviewed());
        assertNotNull(found.getReviewer());
    }
}