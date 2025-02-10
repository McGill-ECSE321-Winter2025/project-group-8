package ca.mcgill.ecse321.gameorganizer.repository;

import java.util.ArrayList;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
public class GameRepositoryTests {

    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private ReviewRepository reviewRepository;

    @AfterEach
    public void clearDatabase() {
        reviewRepository.deleteAll();
        gameRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadGame() {
        // Create a game instance
        String name = "Monopoly";
        int minPlayers = 2;
        int maxPlayers = 8;
        String image = "monopoly.jpg";
        Date dateAdded = new Date();
        
        Game game = new Game(name, minPlayers, maxPlayers, image, dateAdded);
        
        // Save the game
        gameRepository.save(game);
        
        // Retrieve the game
        Game retrievedGame = gameRepository.findGameByName(name);
        
        // Verify the game attributes
        assertNotNull(retrievedGame);
        assertEquals(name, retrievedGame.getName());
        assertEquals(minPlayers, retrievedGame.getMinPlayers());
        assertEquals(maxPlayers, retrievedGame.getMaxPlayers());
        assertEquals(image, retrievedGame.getImage());
        assertEquals(dateAdded, retrievedGame.getDateAdded());
    }

    @Test
    public void testGameReviewRelationship() {
        // Create and persist a game
        Game game = new Game("Chess", 2, 2, "chess.jpg", new Date());
        game = gameRepository.save(game);
        
        // Create a review
        Review review = new Review();
        review.setRating(5);
        review.setComment("Great classic game!");
        
        // Initialize reviews list if null
        if (game.getReviews() == null) {
            game.setReviews(new ArrayList<>());
        }
        
        // Add review to game and persist review
        game.getReviews().add(review);
        review = reviewRepository.save(review);
        
        // Update game with review
        game = gameRepository.save(game);
        
        // Retrieve game and verify relationship
        Game retrievedGame = gameRepository.findGameByName("Chess");
        assertNotNull(retrievedGame);
        assertNotNull(retrievedGame.getReviews());
        assertEquals(1, retrievedGame.getReviews().size());
        
        Review retrievedReview = retrievedGame.getReviews().get(0);
        assertEquals(5, retrievedReview.getRating());
        assertEquals("Great classic game!", retrievedReview.getComment());
    }

    @Test
    public void testUpdateGame() {
        // Create and persist a game
        Game game = new Game("Risk", 2, 6, "risk.jpg", new Date());
        gameRepository.save(game);
        
        // Update game attributes
        game.setName("Risk Legacy");
        game.setMaxPlayers(5);
        gameRepository.save(game);
        
        // Retrieve and verify updates
        Game retrievedGame = gameRepository.findGameByName("Risk Legacy");
        assertNotNull(retrievedGame);
        assertEquals("Risk Legacy", retrievedGame.getName());
        assertEquals(5, retrievedGame.getMaxPlayers());
    }
}
