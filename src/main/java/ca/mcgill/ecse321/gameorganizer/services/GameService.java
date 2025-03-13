package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service class that handles business logic for game management operations.
 * Provides methods for creating, retrieving, updating, and deleting games,
 * as well as various search functionalities.
 *
 * @author @PlazmaMamba
 */
@Service
public class GameService {

    private GameRepository gameRepository;

    @Autowired
    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    /**
     * Creates a new game in the system.
     *
     * @param aNewGame The game object to create
     * @return ResponseEntity with creation confirmation message
     * @throws IllegalArgumentException if game details are invalid
     */
    @Transactional
    public ResponseEntity<String> createGame(Game aNewGame) {
        if (aNewGame.getName() == null || aNewGame.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Game name cannot be empty");
        }
        if (aNewGame.getMinPlayers() < 1) {
            throw new IllegalArgumentException("Minimum players must be at least 1");
        }
        if (aNewGame.getMaxPlayers() < aNewGame.getMinPlayers()) {
            throw new IllegalArgumentException("Maximum players must be greater than or equal to minimum players");
        }
        if (aNewGame.getOwner() == null) {
            throw new IllegalArgumentException("Game must have an owner");
        }

        gameRepository.save(aNewGame);
        return ResponseEntity.ok("Game created");
    }

    /**
     * Retrieves a game by its ID.
     *
     * @param id The ID of the game to retrieve
     * @return The Game object
     * @throws IllegalArgumentException if no game is found with the given ID
     */
    @Transactional
    public Game getGameById(int id) {
        Game game = gameRepository.findGameById(id);
        if (game == null) {
            throw new IllegalArgumentException("Game with ID " + id + " does not exist");
        }
        return game;
    }

    /**
     * Retrieves games by their exact name.
     *
     * @param name The exact name to search for
     * @return List of games matching the name
     * @throws IllegalArgumentException if name is null or empty
     */
    @Transactional
    public List<Game> getGamesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        return gameRepository.findByName(name);
    }

    /**
     * Retrieves games containing the given text in their name.
     *
     * @param namePart The text to search for in game names
     * @return List of games with matching name parts
     * @throws IllegalArgumentException if search pattern is null or empty
     */
    @Transactional
    public List<Game> getGamesByNameContaining(String namePart) {
        if (namePart == null || namePart.trim().isEmpty()) {
            throw new IllegalArgumentException("Search pattern cannot be empty");
        }
        return gameRepository.findByNameContaining(namePart);
    }

    /**
     * Finds games that can be played with the specified number of players or fewer.
     *
     * @param players The maximum number of players to search for
     * @return List of games playable with the specified number of players or fewer
     * @throws IllegalArgumentException if player count is less than 1
     */
    @Transactional
    public List<Game> getGamesByMinPlayers(int players) {
        if (players < 1) {
            throw new IllegalArgumentException("Player count must be at least 1");
        }
        return gameRepository.findByMinPlayersLessThanEqual(players);
    }

    /**
     * Finds games that can be played with the specified number of players or more.
     *
     * @param players The minimum number of players to search for
     * @return List of games playable with the specified number of players or more
     * @throws IllegalArgumentException if player count is less than 1
     */
    @Transactional
    public List<Game> getGamesByMaxPlayers(int players) {
        if (players < 1) {
            throw new IllegalArgumentException("Player count must be at least 1");
        }
        return gameRepository.findByMaxPlayersGreaterThanEqual(players);
    }

    /**
     * Finds games that can be played with a number of players within the specified range.
     *
     * @param minPlayers The minimum number of players required
     * @param maxPlayers The maximum number of players allowed
     * @return List of games playable within the specified player range
     * @throws IllegalArgumentException if minPlayers is less than 1 or maxPlayers is less than minPlayers
     */
    @Transactional
    public List<Game> getGamesByPlayerRange(int minPlayers, int maxPlayers) {
        if (minPlayers < 1) {
            throw new IllegalArgumentException("Minimum players must be at least 1");
        }
        if (maxPlayers < minPlayers) {
            throw new IllegalArgumentException("Maximum players must be greater than or equal to minimum players");
        }
        return gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(minPlayers, maxPlayers);
    }

    @Transactional
    public List<Game> getGamesByDateAddedBefore(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return gameRepository.findByDateAddedBefore(date);
    }

    @Transactional
    public List<Game> getGamesByDateAddedAfter(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        return gameRepository.findByDateAddedAfter(date);
    }

    @Transactional
    public List<Game> getGamesByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates cannot be null");
        }
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        return gameRepository.findByDateAddedBetween(startDate, endDate);
    }

    @Transactional
    public List<Game> getGamesByOwner(GameOwner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        return gameRepository.findByOwner(owner);
    }

    @Transactional
    public List<Game> getGamesByOwnerAndName(GameOwner owner, String namePart) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        if (namePart == null || namePart.trim().isEmpty()) {
            throw new IllegalArgumentException("Search pattern cannot be empty");
        }
        return gameRepository.findByOwnerAndNameContaining(owner, namePart);
    }

    /**
     * Updates an existing game's information.
     *
     * @param id The ID of the game to update
     * @param updatedGame The game object containing updated information
     * @return ResponseEntity with update confirmation message
     * @throws IllegalArgumentException if no game is found with the given ID
     */
    @Transactional
    public ResponseEntity<String> updateGame(int id, Game updatedGame) {
        Game game = gameRepository.findGameById(id);
        if (game == null) {
            throw new IllegalArgumentException("Game with ID " + id + " does not exist");
        }

        game.setName(updatedGame.getName());
        game.setMinPlayers(updatedGame.getMinPlayers());
        game.setMaxPlayers(updatedGame.getMaxPlayers());
        game.setImage(updatedGame.getImage());

        gameRepository.save(game);
        return ResponseEntity.ok("Game updated successfully");
    }

    @Transactional
    public ResponseEntity<String> deleteGame(int id) {
        Game gameToDelete = gameRepository.findGameById(id);
        if (gameToDelete == null) {
            throw new IllegalArgumentException("Game with ID " + id + " does not exist");
        }
        gameRepository.delete(gameToDelete);
        return ResponseEntity.ok("Game with ID " + id + " has been deleted");
    }

    /**
     * Retrieves all games in the system.
     *
     * @return List of all Game objects
     */
    @Transactional
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    /**
     * Finds games based on their current availability status.
     *
     * @param isAvailable true to find available games, false for unavailable games
     * @return List of games matching the availability criteria
     */
    @Transactional
    public List<Game> getGamesByAvailability(boolean isAvailable) {
        Date currentDate = new Date();
        if (isAvailable) {
            return gameRepository.findAvailableGames(currentDate);
        } else {
            return gameRepository.findUnavailableGames(currentDate);
        }
    }

    /**
     * Finds games with an average rating at or above the specified minimum.
     *
     * @param minRating The minimum average rating to search for (0-5)
     * @return List of games meeting the rating criteria
     * @throws IllegalArgumentException if rating is not between 0 and 5
     */
    @Transactional
    public List<Game> getGamesByRating(double minRating) {
        if (minRating < 0 || minRating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        return gameRepository.findByAverageRatingGreaterThanEqual(minRating);
    }

    /**
     * Finds games belonging to a specific category.
     *
     * @param category The category to search for
     * @return List of games in the specified category
     * @throws IllegalArgumentException if category is null or empty
     */
    @Transactional
    public List<Game> getGamesByCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty");
        }
        return gameRepository.findByCategory(category);
    }

    //submit game review

    //remove game from collection
    /**
     * Removes the chosen game from the collection of it's owner
     *
     * @param aGame The game we are removing
     * @return ResponseEntity with removal confirmation message
     */
    @Transactional
    public ResponseEntity<String> removeGameFromCollection(Game aGame){
        //print statements

        aGame.setOwner(null);
        gameRepository.delete(aGame);
        return ResponseEntity.ok("Game removed from collection");
    }
    //add game to collection

    //borrow game

    //return game
}