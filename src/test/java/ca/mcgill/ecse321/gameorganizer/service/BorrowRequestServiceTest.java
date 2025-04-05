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
import org.mockito.Spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity; // Import ResponseEntity
// Imports for Security Context Mocking
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import static org.mockito.Mockito.times; // Add times import
import org.springframework.security.core.userdetails.UserDetails;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

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
import org.springframework.test.context.ContextConfiguration;
import ca.mcgill.ecse321.gameorganizer.TestJwtConfig;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(initializers = TestJwtConfig.Initializer.class)
public class BorrowRequestServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock // Add mock for LendingRecordService
    private LendingRecordService lendingRecordService;

    @Spy
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

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(requester.getEmail());
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            when(accountRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
            when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(game));
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
            verify(accountRepository).findByEmail(requester.getEmail());
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
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

        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(requester.getEmail());
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            when(accountRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
            when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.empty());

            // Test & Verify
            assertThrows(IllegalArgumentException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
            verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
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

        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(requester.getEmail());
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            when(accountRepository.findByEmail(requester.getEmail())).thenReturn(Optional.empty());
            // Use lenient() to avoid UnnecessaryStubbingException
            lenient().when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(new Game()));

            // Test & Verify - updated to expect UnauthedException
            assertThrows(UnauthedException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
            verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
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
        requester.setId(VALID_REQUESTER_ID);

        // Mock authentication
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(requester.getEmail());
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            when(accountRepository.findByEmail(requester.getEmail())).thenReturn(Optional.of(requester));
            when(gameRepository.findById(VALID_GAME_ID)).thenReturn(Optional.of(game));

            // Test & Verify
            assertThrows(IllegalArgumentException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
            verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
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
        // Setup Owner and Security Context (Owner approves/rejects)
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        owner.setId(99);
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        // Make all authentication mocking lenient to avoid UnnecessaryStubbingException
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(owner.getEmail());
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks and other test data
            BorrowRequest request = new BorrowRequest();
            request.setId(VALID_REQUEST_ID);
            request.setStatus(BorrowRequestStatus.PENDING);
            Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
            game.setId(VALID_GAME_ID);
            game.setOwner(owner); // Set owner on game
            Account requester = new Account("Requester", "requester@test.com", "password");
            requester.setId(VALID_REQUESTER_ID);
            request.setRequestedGame(game);
            request.setRequester(requester);
            request.setStartDate(new Date());
            request.setEndDate(new Date(System.currentTimeMillis() + 86400000));
            request.setRequestDate(new Date());

            // Use doReturn for the spy method and make it lenient
            lenient().doReturn(true).when(borrowRequestService).isGameOwnerOfRequest(VALID_REQUEST_ID, owner.getEmail());
            
            when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));
            when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(lendingRecordService.createLendingRecord(any(Date.class), any(Date.class), any(BorrowRequest.class), any(GameOwner.class)))
                .thenReturn(ResponseEntity.ok("Lending record created successfully"));

            // Test
            BorrowRequestDto result = borrowRequestService.updateBorrowRequestStatus(VALID_REQUEST_ID, BorrowRequestStatus.APPROVED);

            // Verify
            assertNotNull(result);
            assertEquals("APPROVED", result.getStatus());
            verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
            verify(borrowRequestRepository).save(any(BorrowRequest.class));
            verify(lendingRecordService).createLendingRecord(any(Date.class), any(Date.class), any(BorrowRequest.class), any(GameOwner.class));
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
    }

    @Test
    public void testUpdateBorrowRequestStatusInvalidStatus() {
        // Setup Owner and Security Context (Owner approves/rejects)
        GameOwner owner = new GameOwner("Owner", "owner@test.com", "password");
        owner.setId(99);
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        // Make all authentication mocking lenient to avoid UnnecessaryStubbingException
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(owner.getEmail());
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks and other test data
            BorrowRequest request = new BorrowRequest();
            request.setId(VALID_REQUEST_ID);
            request.setStatus(BorrowRequestStatus.PENDING);
            Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
            game.setOwner(owner); // Set owner on game
            request.setRequestedGame(game);
            Account requester = new Account("Requester", "requester@test.com", "password");
            request.setRequester(requester);
            
            // Use doReturn for the spy method and make it lenient
            lenient().doReturn(true).when(borrowRequestService).isGameOwnerOfRequest(VALID_REQUEST_ID, owner.getEmail());
            
            // Use lenient() to avoid UnnecessaryStubbingException
            lenient().when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));
            
            // Test & Verify - PENDING is not a valid target status, it's already set to PENDING
            assertThrows(IllegalArgumentException.class, () -> 
                borrowRequestService.updateBorrowRequestStatus(VALID_REQUEST_ID, BorrowRequestStatus.PENDING));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testDeleteBorrowRequestSuccess() {
        // Setup Requester and Security Context (Requester can delete their own request)
        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        // Make all authentication mocking lenient to avoid UnnecessaryStubbingException
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(requester.getEmail());
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            BorrowRequest request = new BorrowRequest();
            request.setId(VALID_REQUEST_ID);
            request.setRequester(requester);
            
            // Use doReturn for the spy method and make it lenient
            lenient().doReturn(true).when(borrowRequestService).isOwnerOrRequesterOfRequest(VALID_REQUEST_ID, requester.getEmail());
            
            when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(request));
            
            // Test
            borrowRequestService.deleteBorrowRequest(VALID_REQUEST_ID);

            // Verify
            verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
            verify(borrowRequestRepository).delete(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testDeleteBorrowRequestNotFound() {
        // Setup Requester and Security Context (Requester tries to delete a non-existent request)
        Account requester = new Account("Requester", "requester@test.com", "password");
        requester.setId(VALID_REQUESTER_ID);
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        
        // Make all authentication mocking lenient to avoid UnnecessaryStubbingException
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        lenient().when(auth.getName()).thenReturn(requester.getEmail());
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Use doReturn for the spy method and make it lenient
            lenient().doReturn(true).when(borrowRequestService).isOwnerOrRequesterOfRequest(VALID_REQUEST_ID, requester.getEmail());
            
            // Request doesn't exist
            when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.empty());

            // Test & Verify
            assertThrows(IllegalArgumentException.class, () -> borrowRequestService.deleteBorrowRequest(VALID_REQUEST_ID));
            verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
            verify(borrowRequestRepository, never()).delete(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testCreateBorrowRequestAuthenticatedUserNotFound() {
        // Setup
        Date startDate = new Date();
        Date endDate = new Date(startDate.getTime() + 86400000);
        CreateBorrowRequestDto requestDto = new CreateBorrowRequestDto(
            VALID_REQUESTER_ID,
            VALID_GAME_ID,
            startDate,
            endDate
        );

        // Mock authentication with a user that doesn't exist in the database
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("nonexistent@email.com");
        when(authentication.isAuthenticated()).thenReturn(true);
        SecurityContextHolder.setContext(securityContext);

        try {
            // User doesn't exist in database
            when(accountRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());

            // Test & Verify - expect UnauthedException instead of IllegalArgumentException
            assertThrows(UnauthedException.class, () -> borrowRequestService.createBorrowRequest(requestDto));
            verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        } finally {
            SecurityContextHolder.clearContext(); // Clean up security context
        }
    }
}
