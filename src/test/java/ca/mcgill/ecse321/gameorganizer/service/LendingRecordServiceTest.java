package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.models.*;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LendingRecordServiceTest {

    @Mock
    private LendingRecordRepository lendingRecordRepository;

    @InjectMocks
    private LendingRecordService lendingRecordService;

    private LendingRecord testRecord;
    private GameOwner owner;
    private Account borrower;
    private Game game;
    private BorrowRequest request;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    public void setUp() {
        // Set up test dates
        startDate = new Date();
        endDate = new Date(startDate.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days later

        // Create test objects
        owner = new GameOwner("Test Owner", "owner@test.com", "password123");
        borrower = new Account("Test Borrower", "borrower@test.com", "password123");
        game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);

        request = new BorrowRequest(startDate, endDate, BorrowRequestStatus.APPROVED, new Date(), game);
        request.setRequester(borrower);
        request.setResponder(owner);

        testRecord = new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
        testRecord.setId(1);
    }

    @Test
    public void testCreateLendingRecordSuccess() {
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        ResponseEntity<String> response = lendingRecordService.createLendingRecord(startDate, endDate, request, owner);

        assertEquals("Lending record created successfully", response.getBody());
        verify(lendingRecordRepository).save(any(LendingRecord.class));
    }

    @Test
    public void testCreateLendingRecordWithNullStartDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.createLendingRecord(null, endDate, request, owner);
        });
        assertEquals("Required parameters cannot be null", exception.getMessage());
    }

    @Test
    public void testCreateLendingRecordWithEndDateBeforeStartDate() {
        Date invalidEndDate = new Date(startDate.getTime() - 86400000); // 1 day before start
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.createLendingRecord(startDate, invalidEndDate, request, owner);
        });
        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    @Test
    public void testGetLendingRecordByIdSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        LendingRecord found = lendingRecordService.getLendingRecordById(1);

        assertNotNull(found);
        assertEquals(testRecord.getId(), found.getId());
    }

    @Test
    public void testGetLendingRecordByIdNotFound() {
        when(lendingRecordRepository.findLendingRecordById(99)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.getLendingRecordById(99);
        });
        assertEquals("Lending record not found with id: 99", exception.getMessage());
    }

    @Test
    public void testGetLendingRecordsByOwner() {
        when(lendingRecordRepository.findByRecordOwner(owner)).thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> records = lendingRecordService.getLendingRecordsByOwner(owner);

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
        assertEquals(testRecord, records.get(0));
    }

    @Test
    public void testGetLendingRecordsByBorrower() {
        when(lendingRecordRepository.findByRequest_Borrower(borrower)).thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> records = lendingRecordService.getLendingRecordsByBorrower(borrower);

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
        assertEquals(testRecord, records.get(0));
    }

    @Test
    public void testUpdateStatusSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        ResponseEntity<String> response = lendingRecordService.updateStatus(1, LendingRecord.LendingStatus.OVERDUE);

        assertEquals("Lending record status updated successfully", response.getBody());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, testRecord.getStatus());
    }

    @Test
    public void testUpdateStatusClosedRecord() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.updateStatus(1, LendingRecord.LendingStatus.ACTIVE);
        });
        assertEquals("Cannot update status of a closed lending record", exception.getMessage());
    }

    @Test
    public void testFindOverdueRecords() {
        when(lendingRecordRepository.findByEndDateBeforeAndStatus(any(Date.class), 
            eq(LendingRecord.LendingStatus.ACTIVE))).thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> overdueRecords = lendingRecordService.findOverdueRecords();

        assertFalse(overdueRecords.isEmpty());
        assertEquals(1, overdueRecords.size());
    }

    @Test
    public void testUpdateEndDateSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        Date newEndDate = new Date(endDate.getTime() + 86400000); // 1 day later

        ResponseEntity<String> response = lendingRecordService.updateEndDate(1, newEndDate);

        assertEquals("Lending record end date updated successfully", response.getBody());
        assertEquals(newEndDate, testRecord.getEndDate());
    }

    @Test
    public void testDeleteLendingRecordSuccess() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        ResponseEntity<String> response = lendingRecordService.deleteLendingRecord(1);

        assertEquals("Lending record deleted successfully", response.getBody());
        verify(lendingRecordRepository).delete(testRecord);
    }

    @Test
    public void testDeleteActiveLendingRecord() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.deleteLendingRecord(1);
        });
        assertEquals("Cannot delete an active lending record", exception.getMessage());
    }

    @Test
    public void testCloseLendingRecordSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        ResponseEntity<String> response = lendingRecordService.closeLendingRecord(1);

        assertEquals("Lending record closed successfully", response.getBody());
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
    }

    @Test
    public void testGetLendingRecordsByDateRange() {
        when(lendingRecordRepository.findByStartDateBetween(any(Date.class), any(Date.class)))
            .thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> records = lendingRecordService.getLendingRecordsByDateRange(startDate, endDate);

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
    }

    @Test
    public void testUpdateMultipleAttributesSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        // New dates for update
        Date newStartDate = new Date(startDate.getTime() + 86400000); // +1 day
        Date newEndDate = new Date(endDate.getTime() + 172800000); // +2 days

        testRecord.setStartDate(newStartDate);
        testRecord.setEndDate(newEndDate);
        testRecord.setStatus(LendingRecord.LendingStatus.OVERDUE);

        LendingRecord updated = lendingRecordService.getLendingRecordById(1);
        
        assertEquals(newStartDate, updated.getStartDate());
        assertEquals(newEndDate, updated.getEndDate());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, updated.getStatus());
    }

    @Test
    public void testCreateLendingRecordWithInvalidGameOwner() {
        GameOwner differentOwner = new GameOwner("Different Owner", "different@test.com", "password123");
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.createLendingRecord(startDate, endDate, request, differentOwner);
        });
        assertEquals("The record owner must be the owner of the game in the borrow request", exception.getMessage());
    }

    @Test
    public void testCreateLendingRecordWithPastStartDate() {
        Date pastDate = new Date(System.currentTimeMillis() - 86400000); // 1 day ago
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.createLendingRecord(pastDate, endDate, request, owner);
        });
        assertEquals("Start date cannot be in the past", exception.getMessage());
    }

    @Test
    public void testComplexLendingRecordLifecycle() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        // Step 1: Create and verify initial state
        ResponseEntity<String> createResponse = lendingRecordService.createLendingRecord(startDate, endDate, request, owner);
        assertEquals("Lending record created successfully", createResponse.getBody());

        // Step 2: Update to OVERDUE status
        ResponseEntity<String> updateResponse = lendingRecordService.updateStatus(1, LendingRecord.LendingStatus.OVERDUE);
        assertEquals("Lending record status updated successfully", updateResponse.getBody());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, testRecord.getStatus());

        // Step 3: Extend end date
        Date newEndDate = new Date(endDate.getTime() + 86400000);
        ResponseEntity<String> extendResponse = lendingRecordService.updateEndDate(1, newEndDate);
        assertEquals("Lending record end date updated successfully", extendResponse.getBody());
        assertEquals(newEndDate, testRecord.getEndDate());

        // Step 4: Close the record
        ResponseEntity<String> closeResponse = lendingRecordService.closeLendingRecord(1);
        assertEquals("Lending record closed successfully", closeResponse.getBody());
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());

        // Step 5: Verify record can be deleted after closing
        ResponseEntity<String> deleteResponse = lendingRecordService.deleteLendingRecord(1);
        assertEquals("Lending record deleted successfully", deleteResponse.getBody());

        verify(lendingRecordRepository, times(5)).save(any(LendingRecord.class));
        verify(lendingRecordRepository, times(1)).delete(any(LendingRecord.class));
    }
}
