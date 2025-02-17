package ca.mcgill.ecse321.gameorganizer.repository;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations="classpath:application-test.properties")
public class BorrowRequestTests {

    @Autowired
    private BorrowRequestService borrowRequestService;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void clearDatabase() {
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    /**
     * Test valid borrow request creation.
     */
    @Test
    public void testCreateBorrowRequestSuccess() {
        // Create owner and game
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        // Create requester
        Account requester = new Account();
        requester.setName("requester");
        requester.setEmail("requester@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        // Set valid start and end dates
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour later
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);   // 2 hours later
        
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        // Create request via service
        ResponseEntity<String> createResponse = borrowRequestService.createBorrowRequest(br);
        assertEquals("Borrow request created successfully.", createResponse.getBody());
        
        // Check persisted attributes
        BorrowRequest savedBR = borrowRequestRepository.findBorrowRequestById(br.getId()).orElse(null);
        assertNotNull(savedBR, "The borrow request should be persisted in the database.");
        assertEquals(BorrowRequestStatus.PENDING, savedBR.getStatus());
        assertNotNull(savedBR.getRequestDate());
        assertEquals(startDate, savedBR.getStartDate());
        assertEquals(endDate, savedBR.getEndDate());
        
        // Check object references
        assertNotNull(savedBR.getRequestedGame(), "The requested game reference should be persisted.");
        assertEquals(game.getId(), savedBR.getRequestedGame().getId(), "The game ID should match.");
        assertEquals(game.getName(), savedBR.getRequestedGame().getName(), "The game title should match.");
        
        assertNotNull(savedBR.getRequester(), "The requester reference should be persisted.");
        assertEquals(requester.getId(), savedBR.getRequester().getId(), "The requester ID should match.");
        assertEquals(requester.getName(), savedBR.getRequester().getName(), "The requester name should match.");
    }

    /**
     * Test rejection for end date before start.
     */
    @Test
    public void testCreateBorrowRequestEndDateBeforeStart() {
        // Create owner and game
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        // Create requester
        Account requester = new Account();
        requester.setName("requester");
        requester.setEmail("requester@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        // Set invalid date order
        Date startDate = new Date(System.currentTimeMillis() + 7200 * 1000); // 2 hours later
        Date endDate = new Date(System.currentTimeMillis() + 3600 * 1000);   // 1 hour later
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowRequestService.createBorrowRequest(br);
        });
        assertTrue(exception.getMessage().contains("End date must be after start date."));
    }

    /**
     * Test owner can't borrow own game.
     */
    @Test
    public void testCreateBorrowRequestSelfBorrow() {
        // Owner acts as requester
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        // Create owner's game
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        // Request by owner
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(owner);
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowRequestService.createBorrowRequest(br);
        });
        assertTrue(exception.getMessage().contains("Owners cannot request their own game."));
    }

    /**
     * Test rejection for overlapping request.
     */
    @Test
    public void testCreateBorrowRequestOverlapping() {
        // Create owner and game
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        // Create first approved request
        Account requester1 = new Account();
        requester1.setName("requester1");
        requester1.setEmail("requester1@test.com");
        requester1.setPassword("password");
        requester1 = accountRepository.save(requester1);
        
        Date startDate1 = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate1 = new Date(System.currentTimeMillis() + 7200 * 1000);
        BorrowRequest br1 = new BorrowRequest();
        br1.setRequestedGame(game);
        br1.setRequester(requester1);
        br1.setStartDate(startDate1);
        br1.setEndDate(endDate1);
        br1.setStatus(BorrowRequestStatus.APPROVED);
        br1.setRequestDate(new Date());
        borrowRequestRepository.save(br1);
        
        // Create second overlapping request
        Account requester2 = new Account();
        requester2.setName("requester2");
        requester2.setEmail("requester2@test.com");
        requester2.setPassword("password");
        requester2 = accountRepository.save(requester2);
        
        // Dates overlap first request
        Date startDate2 = new Date(System.currentTimeMillis() + 4000 * 1000);
        Date endDate2 = new Date(System.currentTimeMillis() + 8000 * 1000);
        BorrowRequest br2 = new BorrowRequest();
        br2.setRequestedGame(game);
        br2.setRequester(requester2);
        br2.setStartDate(startDate2);
        br2.setEndDate(endDate2);
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowRequestService.createBorrowRequest(br2);
        });
        assertTrue(exception.getMessage().contains("Game is unavailable for the requested period."));
    }

    /**
     * Test retrieval of persisted request by ID.
     */
    @Test
    public void testGetBorrowRequestById() {
        // Setup owner, game, and requester
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        Account requester = new Account();
        requester.setName("requester");
        requester.setEmail("requester@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        borrowRequestService.createBorrowRequest(br);
        BorrowRequest fetchedBR = borrowRequestService.getBorrowRequestById(br.getId());
        assertNotNull(fetchedBR);
        assertEquals(startDate, fetchedBR.getStartDate());
        assertEquals(endDate, fetchedBR.getEndDate());
        
        // Check fetched references
        assertNotNull(fetchedBR.getRequestedGame());
        assertEquals(game.getId(), fetchedBR.getRequestedGame().getId());
        assertNotNull(fetchedBR.getRequester());
        assertEquals(requester.getId(), fetchedBR.getRequester().getId());
    }

    /**
     * Test updating request status.
     */
    @Test
    public void testUpdateBorrowRequestStatus() {
        // Setup test entities
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        Account requester = new Account();
        requester.setName("requester");
        requester.setEmail("requester@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        borrowRequestService.createBorrowRequest(br);
        
        // Set status to APPROVED
        ResponseEntity<String> updateResponse = borrowRequestService.updateBorrowRequestStatus(br.getId(), "APPROVED");
        assertEquals("Borrow request status updated successfully.", updateResponse.getBody());
        
        BorrowRequest updatedBR = borrowRequestRepository.findBorrowRequestById(br.getId()).orElse(null);
        assertNotNull(updatedBR);
        assertEquals(BorrowRequestStatus.APPROVED, updatedBR.getStatus());
    }

    /**
     * Test request deletion of borrow request
     */
    @Test
    public void testDeleteBorrowRequest() {
        // Setup entities
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        Account requester = new Account();
        requester.setName("requester");
        requester.setEmail("requester@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        
        borrowRequestService.createBorrowRequest(br);
        ResponseEntity<String> deleteResponse = borrowRequestService.deleteBorrowRequest(br.getId());
        assertEquals("Borrow request with id " + br.getId() + " has been deleted.", deleteResponse.getBody());
        
        assertFalse(borrowRequestRepository.findBorrowRequestById(br.getId()).isPresent());
    }

    /**
     * Test persistence of borrow request.
     */
    @Test
    public void testBorrowRequestPersistence() {
        // Setup owner, game, and requester
        GameOwner owner = new GameOwner("ownerPersist", "ownerpersist@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);
        
        Game game = new Game("Persistent Game", 2, 4, "persist.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);
        
        Account requester = new Account();
        requester.setName("requesterPersist");
        requester.setEmail("requesterpersist@test.com");
        requester.setPassword("password");
        requester = accountRepository.save(requester);
        
        // Build request with attributes
        BorrowRequest br = new BorrowRequest();
        br.setRequestedGame(game);
        br.setRequester(requester);
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        br.setStartDate(startDate);
        br.setEndDate(endDate);
        br.setStatus(BorrowRequestStatus.PENDING);
        Date requestDate = new Date();
        br.setRequestDate(requestDate);
        
        // Save request to repository
        br = borrowRequestRepository.save(br);
        
        // Retrieve request from DB
        BorrowRequest persistedBR = borrowRequestRepository.findBorrowRequestById(br.getId()).orElse(null);
        assertNotNull(persistedBR, "The borrow request should have been persisted.");
        
        // Check attribute values
        assertEquals(BorrowRequestStatus.PENDING, persistedBR.getStatus(), "The status should be persisted correctly.");
        assertEquals(startDate, persistedBR.getStartDate(), "The start date should be persisted correctly.");
        assertEquals(endDate, persistedBR.getEndDate(), "The end date should be persisted correctly.");
        assertEquals(requestDate, persistedBR.getRequestDate(), "The request date should be persisted correctly.");
        
        // Check object references
        assertNotNull(persistedBR.getRequestedGame(), "The requested game reference should be persisted.");
        assertEquals(game.getId(), persistedBR.getRequestedGame().getId(), "The game ID should match.");
        assertEquals(game.getName(), persistedBR.getRequestedGame().getName(), "The game title should match.");
        
        assertNotNull(persistedBR.getRequester(), "The requester reference should be persisted.");
        assertEquals(requester.getId(), persistedBR.getRequester().getId(), "The requester ID should match.");
        assertEquals(requester.getName(), persistedBR.getRequester().getName(), "The requester name should match.");
    }
}
