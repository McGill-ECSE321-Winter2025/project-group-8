package ca.mcgill.ecse321.gameorganizer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times; // Add import for times()
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// Imports for Security Context Mocking
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository; // Import AccountRepository

import ca.mcgill.ecse321.gameorganizer.dto.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.ResourceNotFoundException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService;

@ExtendWith(MockitoExtension.class)
public class LendingRecordServiceTest {

    @Mock
    private LendingRecordRepository lendingRecordRepository;

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private AccountRepository accountRepository; // Mock AccountRepository

    @InjectMocks
    private LendingRecordService lendingRecordService;

    // Test constants
    private static final int VALID_RECORD_ID = 1;
    private static final int VALID_REQUEST_ID = 2;
    private static final int VALID_USER_ID = 3;

    private GameOwner owner;
    private Game game;
    private Account borrower;
    private BorrowRequest borrowRequest;
    private Date startDate;
    private Date endDate;
    private LendingRecord record;

    @BeforeEach
    public void setup() {
        // Setup test data
        owner = new GameOwner("Owner", "owner@test.com", "password");
        game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        borrower = new Account("Borrower", "borrower@test.com", "password");
        
        startDate = new Date();
        endDate = new Date(startDate.getTime() + 86400000); // Next day
        
        borrowRequest = new BorrowRequest();
        borrowRequest.setId(VALID_REQUEST_ID);
        borrowRequest.setRequestedGame(game);
        borrowRequest.setRequester(borrower);
        
        record = new LendingRecord(startDate, endDate, LendingStatus.ACTIVE, borrowRequest, owner);
        record.setId(VALID_RECORD_ID);
    }

    @Test
    public void testCreateLendingRecordSuccess() {
        // Setup
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(record);

        // Test
        ResponseEntity<String> response = lendingRecordService.createLendingRecord(startDate, endDate, borrowRequest, owner);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("successfully"));
        verify(lendingRecordRepository).save(any(LendingRecord.class));
    }

    @Test
    public void testCreateLendingRecordWithNullParameters() {
        // Test & Verify
        ResponseEntity<String> response = lendingRecordService.createLendingRecord(null, endDate, borrowRequest, owner);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = lendingRecordService.createLendingRecord(startDate, null, borrowRequest, owner);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = lendingRecordService.createLendingRecord(startDate, endDate, null, owner);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        response = lendingRecordService.createLendingRecord(startDate, endDate, borrowRequest, null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        verify(lendingRecordRepository, never()).save(any(LendingRecord.class));
    }

    @Test
    public void testCreateLendingRecordFromRequestIdSuccess() {
        // Setup
        when(borrowRequestRepository.findBorrowRequestById(VALID_REQUEST_ID)).thenReturn(Optional.of(borrowRequest));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(record);

        // Test
        ResponseEntity<String> response = lendingRecordService.createLendingRecordFromRequestId(startDate, endDate, VALID_REQUEST_ID, owner);

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("successfully"));
        verify(borrowRequestRepository).findBorrowRequestById(VALID_REQUEST_ID);
        verify(lendingRecordRepository).save(any(LendingRecord.class));
    }

    @Test
    public void testCreateLendingRecordFromRequestIdNotFound() {
        // Setup
        when(borrowRequestRepository.findBorrowRequestById(anyInt())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> 
            lendingRecordService.createLendingRecordFromRequestId(startDate, endDate, VALID_REQUEST_ID, owner));
        verify(lendingRecordRepository, never()).save(any(LendingRecord.class));
    }

    @Test
    public void testGetLendingRecordByIdSuccess() {
        // Setup
        when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

        // Test
        LendingRecord result = lendingRecordService.getLendingRecordById(VALID_RECORD_ID);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_RECORD_ID, result.getId());
        verify(lendingRecordRepository).findLendingRecordById(VALID_RECORD_ID);
    }

    @Test
    public void testGetLendingRecordByIdNotFound() {
        // Setup
        when(lendingRecordRepository.findLendingRecordById(anyInt())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(ResourceNotFoundException.class, () -> lendingRecordService.getLendingRecordById(VALID_RECORD_ID));
        verify(lendingRecordRepository).findLendingRecordById(VALID_RECORD_ID);
    }

    @Test
    public void testGetAllLendingRecords() {
        // Setup
        List<LendingRecord> records = new ArrayList<>();
        records.add(record);
        when(lendingRecordRepository.findAll()).thenReturn(records);

        // Test
        List<LendingRecord> result = lendingRecordService.getAllLendingRecords();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_RECORD_ID, result.get(0).getId());
        verify(lendingRecordRepository).findAll();
    }

    @Test
    public void testGetLendingRecordsByOwner() {
        // Setup
        List<LendingRecord> records = new ArrayList<>();
        records.add(record);
        when(lendingRecordRepository.findByRecordOwner(owner)).thenReturn(records);

        // Test
        List<LendingRecord> result = lendingRecordService.getLendingRecordsByOwner(owner);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_RECORD_ID, result.get(0).getId());
        verify(lendingRecordRepository).findByRecordOwner(owner);
    }

    @Test
    public void testGetLendingRecordsByOwnerNull() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> lendingRecordService.getLendingRecordsByOwner(null));
        verify(lendingRecordRepository, never()).findByRecordOwner(any());
    }

    @Test
    public void testFilterLendingRecordsSuccess() {
        // Setup
        List<LendingRecord> records = new ArrayList<>();
        records.add(record);
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto(
            startDate, endDate, "ACTIVE", game.getId(), borrower.getId());

        when(lendingRecordRepository.filterLendingRecords(
            startDate, endDate, LendingStatus.ACTIVE, borrower.getId(), game.getId()))
            .thenReturn(records);

        // Test
        List<LendingRecord> result = lendingRecordService.filterLendingRecords(filterDto);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_RECORD_ID, result.get(0).getId());
        verify(lendingRecordRepository).filterLendingRecords(
            startDate, endDate, LendingStatus.ACTIVE, borrower.getId(), game.getId());
    }

    @Test
    public void testFilterLendingRecordsPaginatedSuccess() {
        // Setup
        List<LendingRecord> records = new ArrayList<>();
        records.add(record);
        Page<LendingRecord> page = new PageImpl<>(records);
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto(
            startDate, endDate, "ACTIVE", game.getId(), borrower.getId());
        Pageable pageable = PageRequest.of(0, 10);

        when(lendingRecordRepository.filterLendingRecords(
            startDate, endDate, LendingStatus.ACTIVE, borrower.getId(), game.getId(), pageable))
            .thenReturn(page);

        // Test
        Page<LendingRecord> result = lendingRecordService.filterLendingRecordsPaginated(filterDto, pageable);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(VALID_RECORD_ID, result.getContent().get(0).getId());
        verify(lendingRecordRepository).filterLendingRecords(
            startDate, endDate, LendingStatus.ACTIVE, borrower.getId(), game.getId(), pageable);
    }

    @Test
    public void testUpdateStatusSuccess() {
        // Setup Security Context (Simulating owner making the change)
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner)); // Mock repo call needed by service
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));
            when(lendingRecordRepository.save(any(LendingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Test
            // Use owner.getId() as the userId performing the action
            ResponseEntity<String> response = lendingRecordService.updateStatus(
                VALID_RECORD_ID, LendingStatus.CLOSED, owner.getId(), "Test reason");

            // Verify
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("successfully"));
            verify(lendingRecordRepository, times(2)).save(any(LendingRecord.class)); // Expect 2 saves
            assertEquals(LendingStatus.CLOSED, record.getStatus()); // Verify status change
        } finally {
            SecurityContextHolder.clearContext(); // Clear context after test
        }
    }

    @Test
    public void testUpdateStatusInvalidTransition() {
        // Setup
        record.setStatus(LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

        // Test & Verify
        assertThrows(IllegalStateException.class, () -> 
            lendingRecordService.updateStatus(VALID_RECORD_ID, LendingStatus.ACTIVE, VALID_USER_ID, "Test reason"));
        verify(lendingRecordRepository, never()).save(any(LendingRecord.class));
    }

    @Test
    public void testCloseLendingRecordSuccess() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        // Setup Mocks
        when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner)); // Mock repo call
        when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Return saved entity

        try {
            // Test
            ResponseEntity<String> response = lendingRecordService.closeLendingRecord(
                VALID_RECORD_ID, owner.getId(), "Test reason"); // Use actual owner ID

            // Verify
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("successfully"));
            verify(lendingRecordRepository).save(any(LendingRecord.class));
            assertEquals(LendingStatus.CLOSED, record.getStatus()); // Verify status change
        } finally {
            SecurityContextHolder.clearContext(); // Clear context after test
        }
    }

    @Test
    public void testCloseLendingRecordAlreadyClosed() {
        // Setup Security Context (Owner is needed for the initial check)
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            record.setStatus(LendingStatus.CLOSED); // Set record to closed
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner)); // Mock repo call
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

            // Test & Verify
            // Expect IllegalStateException because the record is already closed
            assertThrows(IllegalStateException.class, () ->
                lendingRecordService.closeLendingRecord(VALID_RECORD_ID, owner.getId(), "Test reason"));
            verify(lendingRecordRepository, never()).save(any(LendingRecord.class)); // Ensure save is not called
        } finally {
            SecurityContextHolder.clearContext(); // Clear context after test
        }
    }

    @Test
    public void testCloseLendingRecordWithDamageAssessmentSuccess() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner)); // Mock repo call
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));
            when(lendingRecordRepository.save(any(LendingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Test
            ResponseEntity<String> response = lendingRecordService.closeLendingRecordWithDamageAssessment(
                VALID_RECORD_ID, true, "Minor scratch", 1, owner.getId(), "Test reason"); // Use actual owner ID

            // Verify
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("successfully"));
            verify(lendingRecordRepository, times(2)).save(any(LendingRecord.class)); // Expect 2 saves
            assertEquals(LendingStatus.CLOSED, record.getStatus());
            assertTrue(record.isDamaged());
            assertEquals("Minor scratch", record.getDamageNotes());
            assertEquals(1, record.getDamageSeverity());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testFindOverdueRecords() {
        // Setup
        List<LendingRecord> records = new ArrayList<>();
        records.add(record);
        when(lendingRecordRepository.findByEndDateBeforeAndStatus(any(Date.class), any(LendingStatus.class)))
            .thenReturn(records);

        // Test
        List<LendingRecord> result = lendingRecordService.findOverdueRecords();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_RECORD_ID, result.get(0).getId());
        verify(lendingRecordRepository).findByEndDateBeforeAndStatus(any(Date.class), any(LendingStatus.class));
    }

    @Test
    public void testUpdateEndDateSuccess() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            Date newEndDate = new Date(endDate.getTime() + 86400000); // One more day
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));
            when(lendingRecordRepository.save(any(LendingRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Test
            ResponseEntity<String> response = lendingRecordService.updateEndDate(VALID_RECORD_ID, newEndDate);

            // Verify
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("successfully"));
            verify(lendingRecordRepository, times(2)).save(any(LendingRecord.class)); // Expect 2 saves
            assertEquals(newEndDate, record.getEndDate()); // Verify date change
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testUpdateEndDateInvalidDate() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            Date invalidDate = new Date(startDate.getTime() - 86400000); // One day before start
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

            // Test & Verify
            assertThrows(IllegalArgumentException.class, () ->
                lendingRecordService.updateEndDate(VALID_RECORD_ID, invalidDate));
            verify(lendingRecordRepository, never()).save(any(LendingRecord.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testDeleteLendingRecordSuccess() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            record.setStatus(LendingStatus.CLOSED); // Can only delete non-active records
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

            // Test
            ResponseEntity<String> response = lendingRecordService.deleteLendingRecord(VALID_RECORD_ID);

            // Verify
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().contains("successfully"));
            verify(lendingRecordRepository).delete(record);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testDeleteActiveLendingRecord() {
        // Setup Security Context
        Authentication auth = new UsernamePasswordAuthenticationToken(owner.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_GAME_OWNER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks
            // record is ACTIVE by default from setup()
            when(accountRepository.findByEmail(owner.getEmail())).thenReturn(Optional.of(owner));
            when(lendingRecordRepository.findLendingRecordById(VALID_RECORD_ID)).thenReturn(Optional.of(record));

            // Test & Verify
            assertThrows(IllegalStateException.class, () -> lendingRecordService.deleteLendingRecord(VALID_RECORD_ID));
            verify(lendingRecordRepository, never()).delete(any(LendingRecord.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
