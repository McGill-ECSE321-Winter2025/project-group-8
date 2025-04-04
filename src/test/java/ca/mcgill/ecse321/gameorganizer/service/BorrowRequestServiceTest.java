package ca.mcgill.ecse321.gameorganizer.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
// Removed duplicate mock import, already covered by MockitoExtension implicitly? Let's keep it explicit for clarity if needed.
// import static org.mockito.Mockito.mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity; // Import ResponseEntity

import ca.mcgill.ecse321.gameorganizer.dto.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService; // Import LendingRecordService
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;

@ExtendWith(MockitoExtension.class)
public class BorrowRequestServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock // Add mock for LendingRecordService
    private LendingRecordService lendingRecordService;

    @InjectMocks
    private BorrowRequestService borrowRequestService;

    // Test constants
    private static final int VALID_GAME_ID = 1;
    private static final int VALID_REQUESTER_ID = 2;
    private static final int VALID_REQUEST_ID = 3;

    @Test
    public void testCreateBorrowRequestSuccess() {
        // Setup
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000); // Next day

        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(
            VALID_REQUESTER_ID,
            VALID_GAME_ID,
            startDate,
            endDate
        );

        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setId(VALID_GAME_ID);
        game.setOwner(owner);

        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);

        BorrowRequest savedRequest = new BorrowRequest();
        savedRequest.setId(VALID_REQUEST_ID);
        savedRequest.setRequestedGame(game);
        savedRequest.setRequester(requester);
        savedRequest.setStartDate(startDate);
        savedRequest.setEndDate(endDate);
        savedRequest.setStatus(BorrowRequestStatus.PENDING);
        savedRequest.setRequestDate(new Date()); // Corrected: Pass the date object

        when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(game));
        when(accountRepository.findById(VALID_REQUESTER_ID)).thenReturn(Optional.of(requester));
        when(borrowRequestRepository.findOverlappingApprovedRequests(VALID_GAME_ID, startDate, endDate))
                .thenReturn(new ArrayList<>());
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(savedRequest);

        // Test
        BorrowRequestDto result = borrowRequestService.createBorrowRequest(requestDto);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_REQUEST_ID, result.getId());
        assertEquals(VALID_GAME_ID, result.getRequestedGameId());
        assertEquals(VALID_REQUESTER_ID, result.getRequesterId());
        assertEquals("PENDING", result.getStatus());
        verify(borrowRequestRepository).save(any(BorrowRequest.class));
    }

    @Test
    public void testCreateBorrowRequestGameNotFound() {
        // Setup
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000);
        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(
            VALID_REQUESTER_ID,
            VALID_GAME_ID,
            startDate,
            endDate
        );

        when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
    }

    @Test
    public void testCreateBorrowRequestRequesterNotFound() {
        // Setup
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000);
        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(
            VALID_REQUESTER_ID,
            VALID_GAME_ID,
            startDate,
            endDate
        );

        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        // Need to set owner for the game object used here as well if service logic requires it
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        game.setOwner(owner);
        when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(game));
        when(accountRepository.findById(VALID_REQUESTER_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
    }

    @Test
    public void testCreateBorrowRequestInvalidDates() {
        // Setup
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() - 86400000); // Previous day
        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(
            VALID_REQUESTER_ID,
            VALID_GAME_ID,
            startDate,
            endDate
        );

        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        // Need to set owner for the game object used here as well if service logic requires it
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        game.setOwner(owner);
        Account requester = new Account("Requester", "requester@test.com", "password");

        when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(game));
        when(accountRepository.findById(VALID_REQUESTER_ID)).thenReturn(Optional.of(requester));

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
    }

    @Test
    public void testGetBorrowRequestByIdSuccess() {
        // Setup
        BorrowRequest request = new BorrowRequest();
        request.setId(VALID_REQUEST_ID);
        // Need to set game and requester properly here too for consistency
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        Account requester = new Account("Requester", "requester@test.com", "password");
        request.setRequestedGame(game);
        request.setRequester(requester);
        request.setStatus(BorrowRequestStatus.PENDING);
        request.setStartDate(new Date()); // Add dates
        request.setEndDate(new Date(System.currentTimeMillis() + 86400000));
        request.setRequestDate(new Date()); // Add request date

        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));

        // Test
        BorrowRequestDto result = borrowRequestService.getBorrowRequestById(VALID_REQUEST_ID);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_REQUEST_ID, result.getId());
        assertEquals("PENDING", result.getStatus());
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
    }

    @Test
    public void testGetBorrowRequestByIdNotFound() {
        // Setup
        when(borrowRequestRepository.findBorrowRequestById(anyInt())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> borrowRequestService.getBorrowRequestById(VALID_REQUEST_ID));
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
    }

    @Test
    public void testGetAllBorrowRequests() {
        // Setup
        List<BorrowRequest> requests = new ArrayList<>();
        BorrowRequest request = new BorrowRequest();
        request.setId(VALID_REQUEST_ID);
        // Need to set game and requester properly here too for consistency
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        Account requester = new Account("Requester", "requester@test.com", "password");
        request.setRequestedGame(game);
        request.setRequester(requester);
        request.setStatus(BorrowRequestStatus.PENDING);
        request.setStartDate(new Date()); // Add dates
        request.setEndDate(new Date(System.currentTimeMillis() + 86400000));
        request.setRequestDate(new Date()); // Add request date
        requests.add(request);

        when(borrowRequestRepository.findAll()).thenReturn(requests);

        // Test
        List<BorrowRequestDto> result = borrowRequestService.getAllBorrowRequests();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_REQUEST_ID, result.get(0).getId());
        verify(borrowRequestRepository).findAll();
    }

    @Test
    public void testUpdateBorrowRequestStatusSuccess() {
        // Setup
        BorrowRequest request = new BorrowRequest();
        request.setId(VALID_REQUEST_ID);
        request.setStatus(BorrowRequestStatus.PENDING);

        // Create necessary related objects
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        owner.setId(99); // Assign an ID to the owner
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setId(VALID_GAME_ID);
        game.setOwner(owner); // Set owner on game
        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);

        // Set related objects on the request
        request.setRequestedGame(game);
        request.setRequester(requester);
        request.setStartDate(new Date());
        request.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // Set valid dates
        request.setRequestDate(new Date()); // Set request date

        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(request);
        // Mock the lendingRecordService call
        when(lendingRecordService.createLendingRecord(any(Date.class), any(Date.class), any(BorrowRequest.class), any(GameOwner.class)))
            .thenReturn(ResponseEntity.ok("Lending record created successfully"));

        // Test
        BorrowRequestDto result = borrowRequestService.updateBorrowRequestStatus(VALID_REQUEST_ID, BorrowRequestStatus.APPROVED);

        // Verify
        assertNotNull(result);
        assertEquals("APPROVED", result.getStatus());
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
        verify(borrowRequestRepository).save(any(BorrowRequest.class));
        verify(lendingRecordService).createLendingRecord(any(Date.class), any(Date.class), any(BorrowRequest.class), any(GameOwner.class)); // Verify service call
    }

    @Test
    public void testUpdateBorrowRequestStatusInvalidStatus() {
        // Setup
        BorrowRequest request = new BorrowRequest();
        request.setId(VALID_REQUEST_ID);
        request.setStatus(BorrowRequestStatus.PENDING);
        // Need game/requester/dates if service logic checks them before status validation
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        Account requester = new Account("Requester", "requester@test.com", "password");
        request.setRequestedGame(game);
        request.setRequester(requester);
        request.setStartDate(new Date());
        request.setEndDate(new Date(System.currentTimeMillis() + 86400000));
        request.setRequestDate(new Date());

        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));

        // Test & Verify
        // Using DECLINED as the invalid target status based on service logic (must be APPROVED or DECLINED)
        // If the intention is to test that PENDING cannot be set again, the service logic might need adjustment or the test case is different.
        // Assuming the service logic is correct (lines 181-183 in BorrowRequestService), PENDING is not a valid *target* status.
        // Let's test trying to set it to PENDING again, which should fail based on the service logic.
        assertThrows(IllegalArgumentException.class,
            () -> borrowRequestService.updateBorrowRequestStatus(VALID_REQUEST_ID, BorrowRequestStatus.PENDING));
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        verify(lendingRecordService, never()).createLendingRecord(any(), any(), any(), any()); // Ensure lending service not called
    }

    @Test
    public void testDeleteBorrowRequestSuccess() {
        // Setup
        BorrowRequest request = new BorrowRequest();
        request.setId(VALID_REQUEST_ID);
        // Add minimal required fields if deletion logic depends on them (usually not)
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        Account requester = new Account("Requester", "requester@test.com", "password");
        request.setRequestedGame(game);
        request.setRequester(requester);


        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));

        // Test
        borrowRequestService.deleteBorrowRequest(VALID_REQUEST_ID);

        // Verify
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
        verify(borrowRequestRepository).delete(request);
    }

    @Test
    public void testDeleteBorrowRequestNotFound() {
        // Setup
        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> borrowRequestService.deleteBorrowRequest(VALID_REQUEST_ID));
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
        verify(borrowRequestRepository, never()).delete(any(BorrowRequest.class));
    }
}
