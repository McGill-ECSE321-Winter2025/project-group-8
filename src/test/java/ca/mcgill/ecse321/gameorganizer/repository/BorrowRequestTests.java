package ca.mcgill.ecse321.gameorganizer.repository;

import ca.mcgill.ecse321.gameorganizer.dtos.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dtos.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

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
        Account requester = new Account("requester", "requester@test.com", "password");
        requester = accountRepository.save(requester);

        // Set valid start and end dates
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000); // 1 hour later
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);   // 2 hours later

        // Create DTO for the request
        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(requester.getId(), game.getId(), startDate, endDate);
        
        // Create request via service
        BorrowRequestDto createdRequest = borrowRequestService.createBorrowRequest(requestDto);
        assertNotNull(createdRequest, "The borrow request should be successfully created.");
        
        // Check persisted attributes
        assertEquals(BorrowRequestStatus.PENDING.name(), createdRequest.getStatus());
        assertEquals(startDate, createdRequest.getStartDate());
        assertEquals(endDate, createdRequest.getEndDate());

        // Check object references
        assertEquals(game.getId(), createdRequest.getRequestedGameId(), "The game ID should match.");
        assertEquals(requester.getId(), createdRequest.getRequesterId(), "The requester ID should match.");
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
        Account requester = new Account("requester", "requester@test.com", "password");
        requester = accountRepository.save(requester);

        // Set invalid date order
        Date startDate = new Date(System.currentTimeMillis() + 7200 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 3600 * 1000);

        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(requester.getId(), game.getId(), startDate, endDate);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowRequestService.createBorrowRequest(requestDto);
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

        // Set valid dates
        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);

        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(owner.getId(), game.getId(), startDate, endDate);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            borrowRequestService.createBorrowRequest(requestDto);
        });
        assertTrue(exception.getMessage().contains("Owners cannot request their own game."));
    }

    /**
     * Test updating borrow request status.
     */
    @Test
    public void testUpdateBorrowRequestStatus() {
        // Setup entities
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);

        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);

        Account requester = new Account("requester", "requester@test.com", "password");
        requester = accountRepository.save(requester);

        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);

        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(requester.getId(), game.getId(), startDate, endDate);
        BorrowRequestDto createdRequest = borrowRequestService.createBorrowRequest(requestDto);

        // Update status to APPROVED
        BorrowRequestDto updatedRequest = borrowRequestService.updateBorrowRequestStatus(createdRequest.getId(), "APPROVED");
        assertEquals("APPROVED", updatedRequest.getStatus());
    }

    /**
     * Test deletion of borrow request.
     */
    @Test
    public void testDeleteBorrowRequest() {
        // Setup entities
        GameOwner owner = new GameOwner("owner", "owner@test.com", "password");
        owner = (GameOwner) accountRepository.save(owner);

        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        game = gameRepository.save(game);

        Account requester = new Account("requester", "requester@test.com", "password");
        requester = accountRepository.save(requester);

        Date startDate = new Date(System.currentTimeMillis() + 3600 * 1000);
        Date endDate = new Date(System.currentTimeMillis() + 7200 * 1000);

        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(requester.getId(), game.getId(), startDate, endDate);
        BorrowRequestDto createdRequest = borrowRequestService.createBorrowRequest(requestDto);

        borrowRequestService.deleteBorrowRequest(createdRequest.getId());
        assertFalse(borrowRequestRepository.findBorrowRequestById(createdRequest.getId()).isPresent());
    }
}
