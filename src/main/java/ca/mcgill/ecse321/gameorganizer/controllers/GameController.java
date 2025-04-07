package ca.mcgill.ecse321.gameorganizer.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.GameService;
import ca.mcgill.ecse321.gameorganizer.dto.request.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dto.request.GameSearchCriteria;
import ca.mcgill.ecse321.gameorganizer.dto.request.ReviewSubmissionDto;
import ca.mcgill.ecse321.gameorganizer.dto.response.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.response.ReviewResponseDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.ForbiddenException; // Import
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException; // Import
import ca.mcgill.ecse321.gameorganizer.exceptions.ResourceNotFoundException; // Import
import org.springframework.web.server.ResponseStatusException; // Import

/**
 * Controller that handles API endpoints for game operations.
 * Provides endpoints for creating, retrieving, updating, and deleting games,
 * as well as various filtering options.
 *
 * @author Alexander
 */

@RestController
@RequestMapping("/api/games")

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
    @GetMapping
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
    @GetMapping("/{id}")
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
    @PostMapping
    public ResponseEntity<GameResponseDto> createGame(@RequestBody GameCreationDto gameCreationDto) {
        try {
            // Service now uses authenticated principal for owner
            GameResponseDto createdGame = service.createGame(gameCreationDto);
            return new ResponseEntity<>(createdGame, HttpStatus.CREATED);
        } catch (UnauthedException e) {
            // When owner doesn't exist, return 400 Bad Request
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (ForbiddenException e) {
            // When permission denied, return 403 Forbidden
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Updates an existing game.
     *
     * @param id ID of the game to update
     * @param gameDto Updated game data
     * @return The updated game
     */
    @PutMapping("/{id}")
    public ResponseEntity<GameResponseDto> updateGame(@PathVariable int id, @RequestBody GameCreationDto gameDto) {
        try {
            GameResponseDto updatedGame = service.updateGame(id, gameDto);
            return ResponseEntity.ok(updatedGame);
        } catch (ForbiddenException | UnauthedException e) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()); // Or UNAUTHORIZED
        } catch (ResourceNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Deletes a game by ID.
     *
     * @param id ID of the game to delete
     * @return Confirmation message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteGame(@PathVariable int id) {
        try {
            return service.deleteGame(id);
        } catch (ForbiddenException | UnauthedException e) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()); // Or UNAUTHORIZED
        } catch (ResourceNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Retrieves games that can be played with the specified number of players.
     *
     * @param players Number of players
     * @return List of games compatible with the player count
     */
    @GetMapping("/players")
    public ResponseEntity<List<GameResponseDto>> getGamesByPlayerCount(@RequestParam int players) {
        List<Game> games = service.getGamesByPlayerRange(players, players);
        List<GameResponseDto> gameResponseDtos = games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameResponseDtos);
    }

    /**
     * Advanced search endpoint for games with multiple criteria
     */
    @GetMapping("/search")
    public ResponseEntity<List<GameResponseDto>> searchGames(GameSearchCriteria criteria) {
        List<Game> games = service.searchGames(criteria);
        List<GameResponseDto> gameResponseDtos = games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameResponseDtos);
    }

    /**
     * Get all games owned by a specific user
     */
    @GetMapping("/users/{ownerId}/games")
    public ResponseEntity<List<GameResponseDto>> getGamesByOwner(@PathVariable String ownerId) {
        Account account = accountService.getAccountByEmail(ownerId);
        if (!(account instanceof GameOwner)) {
            throw new IllegalArgumentException("Account is not a game owner");
        }
        List<Game> games = service.getGamesByOwner((GameOwner) account);
        List<GameResponseDto> gameResponseDtos = games.stream()
                .map(GameResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(gameResponseDtos);
    }

    /**
     * Get all reviews for a specific game
     */
    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getGameReviews(@PathVariable int id) {
        List<ReviewResponseDto> reviews = service.getReviewsByGameId(id);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Submit a new review for a game
     */
    @PostMapping("/{id}/reviews")
    public ResponseEntity<ReviewResponseDto> submitGameReview(
            @PathVariable int id,
            @RequestBody ReviewSubmissionDto reviewDto) {
        try {
            reviewDto.setGameId(id);
            // Service now uses authenticated principal for reviewer
            ReviewResponseDto review = service.submitReview(reviewDto);
            return new ResponseEntity<>(review, HttpStatus.CREATED);
        } catch (ForbiddenException | UnauthedException e) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage()); // Or UNAUTHORIZED
        } catch (ResourceNotFoundException e) {
             throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Get average rating for a game
     */
    @GetMapping("/{id}/rating")
    public ResponseEntity<Double> getGameRating(@PathVariable int id) {
        double rating = service.getAverageRatingForGame(id);
        return ResponseEntity.ok(rating);
    }


    /**
     * Get all instances for a specific game
     */
    @GetMapping("/{id}/instances")
    public ResponseEntity<List</* TODO: Replace with actual GameInstanceResponseDto */Object>> getGameInstances(@PathVariable int id) {
        // TODO: Implement service call to fetch game instances
        // List<GameInstanceResponseDto> instances = service.getInstancesByGameId(id);
        // return ResponseEntity.ok(instances);
        // Placeholder response until service/DTO are ready:
        return ResponseEntity.ok(List.of()); // Return empty list for now
    }
}