package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.dto.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dto.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ca.mcgill.ecse321.gameorganizer.models.Game;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller that handles API endpoints for game operations.
 * Provides endpoints for creating, retrieving, updating, and deleting games,
 * as well as various filtering options.
 *
 * @author Alexander
 */
@RestController
public class GameController {
    @Autowired
    private GameService service;

    @Autowired
    private AccountService accountService;

    /**
     * Retrieves all games in the system, with optional filtering.
     *
     * @param ownerId Optional parameter to filter games by owner's email
     * @param category Optional parameter to filter games by category
     * @param namePart Optional parameter to filter games by name containing text
     * @return List of games matching the filter criteria
     */
    @GetMapping("/games")
    public ResponseEntity<List<GameResponseDto>> getAllGames(
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String namePart) {

        List<Game> games;

        if (ownerId != null && !ownerId.isEmpty()) {
            // If owner email is provided, get games by owner
            Account account = accountService.getAccountByEmail(ownerId);
            if (account instanceof GameOwner) {
                games = service.getGamesByOwner((GameOwner) account);
            } else {
                throw new IllegalArgumentException("Account is not a game owner");
            }
        } else if (category != null && !category.isEmpty()) {
            // If category is provided, filter by category
            games = service.getGamesByCategory(category);
        } else if (namePart != null && !namePart.isEmpty()) {
            // If name part is provided, search by name containing
            games = service.getGamesByNameContaining(namePart);
        } else {
            // Otherwise, get all games
            games = service.getAllGames();
        }

        List<GameResponseDto> gameResponseDtos = games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(gameResponseDtos);
    }

    /**
     * Retrieves a specific game by ID.
     *
     * @param id ID of the game to retrieve
     * @return The requested game
     */
    @GetMapping("/games/{id}")
    public ResponseEntity<GameResponseDto> findGameById(@PathVariable int id) {
        Game game = service.getGameById(id);
        return ResponseEntity.ok(new GameResponseDto(game));
    }

    /**
     * Creates a new game.
     *
     * @param gameCreationDto Data for the new game
     * @return The created game
     */
    @PostMapping("/games")
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameCreationDto gameCreationDto) {
        GameResponseDto createdGame = service.createGame(gameCreationDto);
        return new ResponseEntity<>(createdGame, HttpStatus.CREATED);
    }

    /**
     * Updates an existing game.
     *
     * @param id ID of the game to update
     * @param gameDto Updated game data
     * @return The updated game
     */
    @PutMapping("/games/{id}")
    public ResponseEntity<GameResponseDto> updateGame(@PathVariable int id, @RequestBody GameCreationDto gameDto) {
        GameResponseDto updatedGame = service.updateGame(id, gameDto);
        return ResponseEntity.ok(updatedGame);
    }

    /**
     * Deletes a game by ID.
     *
     * @param id ID of the game to delete
     * @return Confirmation message
     */
    @DeleteMapping("/games/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable int id) {
        return service.deleteGame(id);
    }

    /**
     * Retrieves games that can be played with the specified number of players.
     *
     * @param players Number of players
     * @return List of games compatible with the player count
     */
    @GetMapping("/games/players")
    public ResponseEntity<List<GameResponseDto>> getGamesByPlayerCount(@RequestParam int players) {
        List<Game> games = service.getGamesByPlayerRange(players, players);
        List<GameResponseDto> gameResponseDtos = games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameResponseDtos);
    }
}