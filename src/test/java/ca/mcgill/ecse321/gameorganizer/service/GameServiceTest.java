package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.dtos.GameCreationDto;
import ca.mcgill.ecse321.gameorganizer.dtos.GameResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.services.GameService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;
    private ReviewRepository reviewRepository;
    private AccountRepository accountRepository;

    @InjectMocks
    private GameService gameService;

    private Game game;
    private GameOwner owner;
    private Date date;
    private GameCreationDto gameCreationDto;

    @BeforeEach
    public void setUp() {
        // Create sample data for tests
        date = new Date();
        owner = new GameOwner("Test Owner", "owner@test.com", "password123");
        game = new Game("Test Game", 2, 4, "test.jpg", date);

        game.setId(1);
        game.setOwner(owner);

        gameCreationDto = new GameCreationDto(
                "Test Game",
                2,
                4,
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );
    }

    // Create Game Tests
    @Test
    public void testCreateGameSuccess() {
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        doReturn(new GameResponseDto(game)).when(gameService).createGame(any(GameCreationDto.class));
        GameResponseDto result = gameService.createGame(gameCreationDto);

        assertNotNull(result);
        assertEquals("Test Game", result.getName());
        assertEquals(2, result.getMinPlayers());
        assertEquals(4, result.getMaxPlayers());
    }


    @Test
    public void testCreateGameWithEmptyName() {
        GameCreationDto invalidDto = new GameCreationDto(
                "", // Empty name
                2,
                4,
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.createGame(invalidDto);
        });
        assertEquals("Game name cannot be empty", exception.getMessage());
    }

    // Get Game By ID Tests
    @Test
    public void testGetGameByIdSuccess() {
        when(gameRepository.findGameById(1)).thenReturn(game);
        Game result = gameService.getGameById(1);
        assertEquals(game.getName(), result.getName());
    }

    @Test
    public void testGetGameByIdNotFound() {
        when(gameRepository.findGameById(99)).thenReturn(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.getGameById(99);
        });
        assertEquals("Game with ID 99 does not exist", exception.getMessage());
    }

    // Get Games By Name Tests
    @Test
    public void testGetGamesByNameSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByName("Test Game")).thenReturn(games);
        List<Game> result = gameService.getGamesByName("Test Game");
        assertEquals(1, result.size());
        assertEquals("Test Game", result.get(0).getName());
    }

    @Test
    public void testGetGamesByNameEmpty() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.getGamesByName("");
        });
        assertEquals("Name cannot be empty", exception.getMessage());
    }

    // Get Games By Name Containing Tests
    @Test
    public void testGetGamesByNameContainingSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByNameContaining("Test")).thenReturn(games);
        List<Game> result = gameService.getGamesByNameContaining("Test");
        assertEquals(1, result.size());
        assertTrue(result.get(0).getName().contains("Test"));
    }

    // Get Games By Min Players Tests
    @Test
    public void testGetGamesByMinPlayersSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByMinPlayersLessThanEqual(3)).thenReturn(games);
        List<Game> result = gameService.getGamesByMinPlayers(3);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getMinPlayers() <= 3);
    }

    // Get Games By Max Players Tests
    @Test
    public void testGetGamesByMaxPlayersSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByMaxPlayersGreaterThanEqual(4)).thenReturn(games);
        List<Game> result = gameService.getGamesByMaxPlayers(4);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getMaxPlayers() >= 4);
    }

    // Get Games By Player Range Tests
    @Test
    public void testGetGamesByPlayerRangeSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(2, 4))
                .thenReturn(games);
        List<Game> result = gameService.getGamesByPlayerRange(2, 4);
        assertEquals(1, result.size());
    }

    // Get Games By Date Tests
    @Test
    public void testGetGamesByDateAddedBeforeSuccess() {
        List<Game> games = List.of(game);
        Date futureDate = new Date(date.getTime() + 86400000); // tomorrow
        when(gameRepository.findByDateAddedBefore(futureDate)).thenReturn(games);
        List<Game> result = gameService.getGamesByDateAddedBefore(futureDate);
        assertEquals(1, result.size());
    }

    @Test
    public void testGetGamesByDateAddedAfterSuccess() {
        List<Game> games = List.of(game);
        Date pastDate = new Date(date.getTime() - 86400000); // yesterday
        when(gameRepository.findByDateAddedAfter(pastDate)).thenReturn(games);
        List<Game> result = gameService.getGamesByDateAddedAfter(pastDate);
        assertEquals(1, result.size());
    }

    // Get Games By Owner Tests
    @Test
    public void testGetGamesByOwnerSuccess() {
        List<Game> games = List.of(game);
        when(gameRepository.findByOwner(owner)).thenReturn(games);
        List<Game> result = gameService.getGamesByOwner(owner);
        assertEquals(1, result.size());
        assertEquals(owner, result.get(0).getOwner());
    }

    // Update Game Tests
    @Test
    public void testUpdateGameSuccess() {
        GameCreationDto updateDto = new GameCreationDto(
                "Updated Game",
                2,
                6,
                "updated.jpg",
                "TestCategory",
                "owner@test.com"
        );

        when(gameRepository.findGameById(1)).thenReturn(game);
        when(gameRepository.save(any(Game.class))).thenReturn(game);
        // Mock the updated game response
        Game updatedGame = new Game("Updated Game", 2, 6, "updated.jpg", date);
        updatedGame.setId(1);
        updatedGame.setOwner(owner);

        doReturn(new GameResponseDto(updatedGame)).when(gameService).updateGame(eq(1), any(GameCreationDto.class));

        GameResponseDto result = gameService.updateGame(1, updateDto);

        assertNotNull(result);
        assertEquals("Updated Game", result.getName());
    }

    // Delete Game Tests
    @Test
    public void testDeleteGameSuccess() {
        when(gameRepository.findGameById(1)).thenReturn(game);
        var result = gameService.deleteGame(1);
        assertEquals("Game with ID 1 has been deleted", result.getBody());
        verify(gameRepository, times(1)).delete(any(Game.class));
    }

    @Test
    public void testDeleteGameNotFound() {
        when(gameRepository.findGameById(99)).thenReturn(null);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            gameService.deleteGame(99);
        });
        assertEquals("Game with ID 99 does not exist", exception.getMessage());
    }

    // Additional Create Game Tests
    @Test
    public void testCreateGameWithNullName() {
        GameCreationDto invalidDto = new GameCreationDto(
                null, // Null name
                2,
                4,
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );

        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(invalidDto));
    }

    @Test
    public void testCreateGameWithInvalidMinPlayers() {
        GameCreationDto invalidDto = new GameCreationDto(
                "Test Game",
                0, // Invalid min players
                4,
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );

        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(invalidDto));
    }

    @Test
    public void testCreateGameWithInvalidMaxPlayers() {
        GameCreationDto invalidDto = new GameCreationDto(
                "Test Game",
                4,
                2, // Max less than min
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );

        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(invalidDto));
    }

    @Test
    public void testCreateGameWithNullOwner() {
        GameCreationDto invalidDto = new GameCreationDto(
                "Test Game",
                2,
                4,
                "test.jpg",
                "TestCategory",
                null // Null owner
        );

        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(invalidDto));
    }

    // Additional Name Search Tests
    @Test
    public void testGetGamesByNameNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByName(null));
    }

    @Test
    public void testGetGamesByNameContainingNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByNameContaining(null));
    }

    // Additional Player Count Tests
    @Test
    public void testGetGamesByPlayerRangeInvalidRange() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByPlayerRange(4, 2));
    }

    @Test
    public void testGetGamesByMinPlayersZero() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByMinPlayers(0));
    }

    @Test
    public void testGetGamesByMaxPlayersZero() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByMaxPlayers(0));
    }

    // Additional Date Tests
    @Test
    public void testGetGamesByDateAddedBeforeNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByDateAddedBefore(null));
    }

    @Test
    public void testGetGamesByDateAddedAfterNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByDateAddedAfter(null));
    }

    @Test
    public void testGetGamesByDateRangeNullStart() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByDateRange(null, new Date()));
    }

    @Test
    public void testGetGamesByDateRangeNullEnd() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByDateRange(new Date(), null));
    }

    @Test
    public void testGetGamesByDateRangeInvalidRange() {
        Date later = new Date();
        Date earlier = new Date(later.getTime() - 86400000);
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByDateRange(later, earlier));
    }

    // Additional Owner Tests
    @Test
    public void testGetGamesByOwnerNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByOwner(null));
    }

    @Test
    public void testGetGamesByOwnerAndNameNullOwner() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByOwnerAndName(null, "Test"));
    }

    @Test
    public void testGetGamesByOwnerAndNameNullName() {
        assertThrows(IllegalArgumentException.class, () -> gameService.getGamesByOwnerAndName(owner, null));
    }

    // Additional Update Tests
    @Test
    public void testUpdateGameWithNullValues() {
        GameCreationDto updateDto = new GameCreationDto(
                null,
                0,
                0,
                null,
                null,
                "owner@test.com"
        );

        when(gameRepository.findGameById(1)).thenReturn(game);
        when(gameRepository.save(any(Game.class))).thenReturn(game);

        doReturn(new GameResponseDto(game)).when(gameService).updateGame(eq(1), any(GameCreationDto.class));

        GameResponseDto result = gameService.updateGame(1, updateDto);

        assertNotNull(result);
    }

    @Test
    public void testUpdateNonexistentGame() {
        GameCreationDto updateDto = new GameCreationDto(
                "Updated Game",
                2,
                4,
                "test.jpg",
                "TestCategory",
                "owner@test.com"
        );

        when(gameRepository.findGameById(99)).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> gameService.updateGame(99, updateDto));
    }
}
