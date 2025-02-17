package ca.mcgill.ecse321.gameorganizer.repository;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
public class GameRepositoryTests {
    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository gameOwnerRepository;

    @AfterEach
    public void clearDatabase(){
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadGame(){
        String name = "Dune Imperium";
        int minPlayers = 1;
        int maxPlayers = 6;
        String image = "dune.png";
        Date dateAdded = new Date();

        Game game = new Game(name, minPlayers, maxPlayers, image, dateAdded);

        game = gameRepository.save(game);
        int id = game.getId();

        Game gameFromDb = gameRepository.findGameById(id);

        assertNotNull(gameFromDb);
        assertEquals(name, gameFromDb.getName());
        assertEquals(minPlayers, gameFromDb.getMinPlayers());
        assertEquals(maxPlayers, gameFromDb.getMaxPlayers());
        assertEquals(image, gameFromDb.getImage());
        assertEquals(dateAdded, gameFromDb.getDateAdded());
    }

    @Test
    public void testFindByName() {
        // Create multiple games with same name
        Game game1 = new Game("Monopoly", 2, 6, "monopoly1.jpg", new Date());
        Game game2 = new Game("Monopoly", 2, 8, "monopoly2.jpg", new Date());
        gameRepository.save(game1);
        gameRepository.save(game2);

        // Find games by name
        List<Game> games = gameRepository.findByName("Monopoly");

        // Assert
        assertEquals(2, ((java.util.List<?>) games).size());
        assertTrue(games.stream().allMatch(g -> g.getName().equals("Monopoly")));
    }

    @Test
    public void testFindByNameContaining() {
        Game game1 = new Game("Monopoly", 2, 6, "m1.jpg", new Date());
        Game game2 = new Game("Monopoly Junior", 2, 4, "m2.jpg", new Date());
        Game game3 = new Game("Chess", 2, 2, "c.jpg", new Date());
        gameRepository.saveAll(List.of(game1, game2, game3));

        List<Game> gamesWithMono = gameRepository.findByNameContaining("Mono");

        assertEquals(2, gamesWithMono.size());
        assertTrue(gamesWithMono.stream().allMatch(g -> g.getName().contains("Mono")));
    }

    @Test
    public void testFindByMinPlayers() {
        Game game1 = new Game("Game1", 2, 4, "g1.jpg", new Date());
        Game game2 = new Game("Game2", 3, 6, "g2.jpg", new Date());
        Game game3 = new Game("Game3", 4, 8, "g3.jpg", new Date());
        gameRepository.saveAll(List.of(game1, game2, game3));

        List<Game> gamesFor3Players = gameRepository.findByMinPlayersLessThanEqual(3);

        assertEquals(2, gamesFor3Players.size());
        assertTrue(gamesFor3Players.stream().allMatch(g -> g.getMinPlayers() <= 3));
    }

    @Test
    public void testFindByMaxPlayers() {
        Game game1 = new Game("Game1", 2, 4, "g1.jpg", new Date());
        Game game2 = new Game("Game2", 3, 6, "g2.jpg", new Date());
        Game game3 = new Game("Game3", 4, 8, "g3.jpg", new Date());
        gameRepository.saveAll(List.of(game1, game2, game3));

        List<Game> gamesFor6Players = gameRepository.findByMaxPlayersGreaterThanEqual(6);

        assertEquals(2, gamesFor6Players.size());
        assertTrue(gamesFor6Players.stream().allMatch(g -> g.getMaxPlayers() >= 6));
    }

    @Test
    public void testFindByPlayerRange() {
        Game game1 = new Game("Game1", 2, 4, "g1.jpg", new Date());
        Game game2 = new Game("Game2", 3, 6, "g2.jpg", new Date());
        Game game3 = new Game("Game3", 4, 8, "g3.jpg", new Date());
        gameRepository.saveAll(List.of(game1, game2, game3));

        List<Game> gamesFor4Players = gameRepository.findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(4, 4);

        assertEquals(3, gamesFor4Players.size());
        assertTrue(gamesFor4Players.stream()
                .allMatch(g -> g.getMinPlayers() <= 4 && g.getMaxPlayers() >= 4));
    }

    @Test
    public void testFindByDateAddedBefore() {
        Date cutoffDate = new Date();
        try {
            Thread.sleep(100); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Game oldGame = new Game("OldGame", 2, 4, "old.jpg", new Date(cutoffDate.getTime() - 86400000));
        Game newGame = new Game("NewGame", 2, 4, "new.jpg", new Date());
        gameRepository.saveAll(List.of(oldGame, newGame));

        List<Game> oldGames = gameRepository.findByDateAddedBefore(cutoffDate);

        assertEquals(1, oldGames.size());
        assertEquals("OldGame", oldGames.get(0).getName());
    }

    @Test
    public void testFindByDateAddedAfter() {
        Date cutoffDate = new Date();
        try {
            Thread.sleep(100); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Game oldGame = new Game("OldGame", 2, 4, "old.jpg", new Date(cutoffDate.getTime() - 86400000));
        Game newGame = new Game("NewGame", 2, 4, "new.jpg", new Date());
        gameRepository.saveAll(List.of(oldGame, newGame));

        List<Game> newGames = gameRepository.findByDateAddedAfter(cutoffDate);

        assertEquals(1, newGames.size());
        assertEquals("NewGame", newGames.get(0).getName());
    }

    @Test
    public void testFindByDateAddedBetween() {
        Date startDate = new Date(System.currentTimeMillis() - 86400000); // Yesterday
        Date endDate = new Date(System.currentTimeMillis() + 86400000);   // Tomorrow
        Date outsideDate = new Date(System.currentTimeMillis() - 172800000); // 2 days ago

        Game game1 = new Game("Game1", 2, 4, "g1.jpg", new Date());
        Game game2 = new Game("Game2", 2, 4, "g2.jpg", outsideDate);
        gameRepository.saveAll(List.of(game1, game2));

        List<Game> gamesInRange = gameRepository.findByDateAddedBetween(startDate, endDate);

        assertEquals(1, gamesInRange.size());
        assertEquals("Game1", gamesInRange.get(0).getName());
    }

    @Test
    public void testFindByOwner() {
        // Create owners with required fields
        GameOwner owner1 = new GameOwner("Owner1", "owner1@test.com", "password1");
        GameOwner owner2 = new GameOwner("Owner2", "owner2@test.com", "password2");
        owner1 = gameOwnerRepository.save(owner1);
        owner2 = gameOwnerRepository.save(owner2);

        Game game1 = new Game("Game1", 2, 4, "g1.jpg", new Date());
        Game game2 = new Game("Game2", 3, 6, "g2.jpg", new Date());
        Game game3 = new Game("Game3", 4, 8, "g3.jpg", new Date());

        game1.setOwner(owner1);
        game2.setOwner(owner1);
        game3.setOwner(owner2);

        // Save games first
        game1 = gameRepository.save(game1);
        game2 = gameRepository.save(game2);
        game3 = gameRepository.save(game3);

        List<Game> owner1Games = gameRepository.findByOwner(owner1);

        assertEquals(2, owner1Games.size());
        // Check by comparing IDs instead of objects
        List<Integer> gameIds = owner1Games.stream()
                .map(Game::getId)
                .collect(Collectors.toList());
        assertTrue(gameIds.contains(game1.getId()));
        assertTrue(gameIds.contains(game2.getId()));
    }

    @Test
    public void testFindByOwnerAndNameContaining() {
        // Create owner with required fields
        GameOwner owner = new GameOwner("TestOwner", "test@owner.com", "password");
        owner = gameOwnerRepository.save(owner);

        Game game1 = new Game("Monopoly", 2, 6, "m1.jpg", new Date());
        Game game2 = new Game("Monopoly Junior", 2, 4, "m2.jpg", new Date());
        Game game3 = new Game("Chess", 2, 2, "c.jpg", new Date());

        game1.setOwner(owner);
        game2.setOwner(owner);
        game3.setOwner(owner);

        // Save games after setting owner
        game1 = gameRepository.save(game1);
        game2 = gameRepository.save(game2);
        game3 = gameRepository.save(game3);

        List<Game> ownerMonopolyGames = gameRepository.findByOwnerAndNameContaining(owner, "Mono");

        assertEquals(2, ownerMonopolyGames.size());

        // Check using IDs and names
        List<Integer> gameIds = ownerMonopolyGames.stream()
                .map(Game::getId)
                .collect(Collectors.toList());

        GameOwner finalOwner = owner;
        assertTrue(ownerMonopolyGames.stream()
                .allMatch(g -> g.getName().contains("Mono") &&
                        g.getOwner().getId() == finalOwner.getId()));  // Using == for primitive int comparison
        assertTrue(gameIds.contains(game1.getId()));
        assertTrue(gameIds.contains(game2.getId()));
    }



}
