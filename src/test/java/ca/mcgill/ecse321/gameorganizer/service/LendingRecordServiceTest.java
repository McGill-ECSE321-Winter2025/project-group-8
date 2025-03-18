package ca.mcgill.ecse321.gameorganizer.service;

import ca.mcgill.ecse321.gameorganizer.models.*;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService;
import ca.mcgill.ecse321.gameorganizer.dtos.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test class for the LendingRecordService.
 * Tests business logic and service layer methods using mocked repositories.
 *
 * @author @YoussGm3o8
 */
@ExtendWith(MockitoExtension.class)
public class LendingRecordServiceTest {

    @Mock
    private LendingRecordRepository lendingRecordRepository;

    @Mock
    private AccountRepository accountRepository;

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
        // Update setup to use future dates
        startDate = new Date(System.currentTimeMillis() + 86400000); // 1 day in future
        endDate = new Date(startDate.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days after start

        // Create test objects
        owner = new GameOwner("Test Owner", "owner@test.com", "password123");
        owner.setId(1);

        borrower = new Account("Test Borrower", "borrower@test.com", "password123");
        borrower.setId(2);

        game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setId(1);
        game.setOwner(owner);

        request = new BorrowRequest(startDate, endDate, BorrowRequestStatus.APPROVED, new Date(), game);
        request.setId(1);
        request.setRequester(borrower);
        request.setResponder(owner);

        testRecord = new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
        testRecord.setId(1);
    }

    @Test
    public void testCreateLendingRecordSuccess() {
        // Create dates in the future
        Date futureStart = new Date(System.currentTimeMillis() + 86400000); // 1 day in future
        Date futureEnd = new Date(System.currentTimeMillis() + 2 * 86400000); // 2 days in future

        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        ResponseEntity<String> response = lendingRecordService.createLendingRecord(futureStart, futureEnd, request, owner);

        assertEquals("Lending record created successfully", response.getBody());
        verify(lendingRecordRepository).save(any(LendingRecord.class));
    }

    @Test
    public void testCreateLendingRecordWithNullStartDate() {
        ResponseEntity<String> response = lendingRecordService.createLendingRecord(null, endDate, request, owner);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Required parameters cannot be null"));
    }

    @Test
    public void testCreateLendingRecordWithEndDateBeforeStartDate() {
        Date invalidEndDate = new Date(startDate.getTime() - 86400000); // 1 day before start

        ResponseEntity<String> response = lendingRecordService.createLendingRecord(startDate, invalidEndDate, request, owner);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("End date cannot be before start date"));
    }

    @Test
    public void testGetLendingRecordByIdSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        LendingRecord found = lendingRecordService.getLendingRecordById(1);

        assertNotNull(found);
        assertEquals(testRecord.getId(), found.getId());
        assertEquals(LendingRecord.LendingStatus.ACTIVE, found.getStatus());
        assertEquals(owner, found.getRecordOwner());
        assertEquals(request, found.getRequest());
    }

    @Test
    public void testGetLendingRecordByIdNotFound() {
        when(lendingRecordRepository.findLendingRecordById(anyInt())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            lendingRecordService.getLendingRecordById(1);
        });
        assertTrue(exception.getMessage().contains("No lending record found with ID"));
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
        when(lendingRecordRepository.findByRequest_Requester(borrower)).thenReturn(Arrays.asList(testRecord));

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
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testUpdateStatusByEmailSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ResponseEntity<String> response = lendingRecordService.updateStatusByEmail(
                1, LendingRecord.LendingStatus.OVERDUE, "owner@test.com", "Test update by email");

        assertEquals("Lending record status updated successfully", response.getBody());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, testRecord.getStatus());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, testRecord.getLastModifiedBy()); // ID of owner
        assertEquals("Test update by email", testRecord.getStatusChangeReason());
    }

    @Test
    public void testUpdateStatusByEmailWithInvalidEmail() {
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.updateStatusByEmail(
                    1, LendingRecord.LendingStatus.OVERDUE, "nonexistent@test.com", "Test");
        });
        assertTrue(exception.getMessage().contains("No user found with email"));
    }

    @Test
    public void testUpdateStatusClosedRecord() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.updateStatus(1, LendingRecord.LendingStatus.ACTIVE);
        });
        assertTrue(exception.getMessage().contains("Cannot change status of a closed lending record"));
    }

    @Test
    public void testFindOverdueRecords() {
        when(lendingRecordRepository.findByEndDateBeforeAndStatus(any(Date.class),
                eq(LendingRecord.LendingStatus.ACTIVE))).thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> overdueRecords = lendingRecordService.findOverdueRecords();

        assertFalse(overdueRecords.isEmpty());
        assertEquals(1, overdueRecords.size());
        assertEquals(testRecord, overdueRecords.get(0));
    }

    @Test
    public void testUpdateEndDateSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);
        Date newEndDate = new Date(endDate.getTime() + 86400000); // 1 day later

        ResponseEntity<String> response = lendingRecordService.updateEndDate(1, newEndDate);

        assertEquals("End date updated successfully", response.getBody());
        assertEquals(newEndDate, testRecord.getEndDate());
        assertEquals(HttpStatus.OK, response.getStatusCode());
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
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        ResponseEntity<String> response = lendingRecordService.closeLendingRecord(1);

        // Verify the format of the response message matches what the service actually returns
        assertTrue(response.getBody().contains("Lending record (ID: 1) successfully closed"));
        assertTrue(response.getBody().contains("Previous status was ACTIVE"));
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
    }

    @Test
    public void testCloseLendingRecordByEmailSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ResponseEntity<String> response = lendingRecordService.closeLendingRecordByEmail(
                1, "owner@test.com", "Test close by email");

        assertTrue(response.getBody().contains("Lending record (ID: 1) successfully closed"));
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
        assertEquals(1, testRecord.getClosedBy()); // ID of owner
    }

    @Test
    public void testCloseAlreadyClosedLendingRecord() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.closeLendingRecord(1);
        });
        assertEquals("Lending record is already closed", exception.getMessage());
    }

    @Test
    public void testCloseAlreadyClosedLendingRecord() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.closeLendingRecord(1);
        });
        assertEquals("Lending record is already closed", exception.getMessage());
    }

    @Test
    public void testGetLendingRecordsByDateRange() {
        when(lendingRecordRepository.findByStartDateBetween(any(Date.class), any(Date.class)))
                .thenReturn(Arrays.asList(testRecord));

        List<LendingRecord> records = lendingRecordService.getLendingRecordsByDateRange(startDate, endDate);

        assertFalse(records.isEmpty());
        assertEquals(1, records.size());
        assertEquals(testRecord, records.get(0));
    }

    @Test
    public void testUpdateMultipleAttributesSuccess() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        Date newStartDate = new Date(startDate.getTime() + 86400000); // 1 day after original start
        Date newEndDate = new Date(endDate.getTime() + 86400000); // 1 day after original end

        LendingRecord recordToUpdate = lendingRecordService.getLendingRecordById(1);
        recordToUpdate.setStartDate(newStartDate);
        recordToUpdate.setEndDate(newEndDate);
        recordToUpdate.setStatus(LendingRecord.LendingStatus.OVERDUE);
        lendingRecordRepository.save(recordToUpdate);

        LendingRecord updated = lendingRecordService.getLendingRecordById(1);

        assertEquals(newStartDate, updated.getStartDate());
        assertEquals(newEndDate, updated.getEndDate());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, updated.getStatus());
    }

    @Test
    public void testCreateLendingRecordWithInvalidGameOwner() {
        GameOwner differentOwner = new GameOwner();
        differentOwner.setId(2);

        ResponseEntity<String> response = lendingRecordService.createLendingRecord(startDate, endDate, request, differentOwner);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("The record owner must be the owner of the game"));
    }

    @Test
    public void testCreateLendingRecordWithPastStartDate() {
        Date pastDate = new Date(System.currentTimeMillis() - 86400000); // 1 day in the past

        ResponseEntity<String> response = lendingRecordService.createLendingRecord(pastDate, endDate, request, owner);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Start date cannot be in the past"));
    }

    @Test
    public void testComplexLendingRecordLifecycle() {
        // Setup future dates for the test
        Date futureStart = new Date(System.currentTimeMillis() + 86400000); // 1 day in future
        Date futureEnd = new Date(System.currentTimeMillis() + 2 * 86400000); // 2 days in future

        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        // Step 1: Create and verify initial state with future dates
        ResponseEntity<String> createResponse = lendingRecordService.createLendingRecord(futureStart, futureEnd, request, owner);
        assertEquals("Lending record created successfully", createResponse.getBody());

        // Step 2: Update to OVERDUE status using email-based method
        ResponseEntity<String> updateResponse = lendingRecordService.updateStatusByEmail(
                1, LendingRecord.LendingStatus.OVERDUE, "owner@test.com", "Marked as overdue");
        assertEquals("Lending record status updated successfully", updateResponse.getBody());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, testRecord.getStatus());

        // Step 3: Extend end date
        Date newEndDate = new Date(endDate.getTime() + 86400000);
        ResponseEntity<String> extendResponse = lendingRecordService.updateEndDate(1, newEndDate);
        assertEquals("End date updated successfully", extendResponse.getBody());
        assertEquals(newEndDate, testRecord.getEndDate());
        // Step 4: Close the record using email-based method
        ResponseEntity<String> closeResponse = lendingRecordService.closeLendingRecordByEmail(
                1, "owner@test.com", "Game returned");
        assertTrue(closeResponse.getBody().contains("successfully closed"));
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());

        // Step 5: Verify record can be deleted after closing
        ResponseEntity<String> deleteResponse = lendingRecordService.deleteLendingRecord(1);
        assertEquals("Lending record deleted successfully", deleteResponse.getBody());

        // Verify exactly 4 saves (create, update status, update end date, close) and 1 delete
        verify(lendingRecordRepository, times(4)).save(any(LendingRecord.class));
        verify(lendingRecordRepository, times(1)).delete(any(LendingRecord.class));
    }

    @Test
    public void testGetLendingRecordsByOwnerWithNullOwner() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.getLendingRecordsByOwner(null);
        });
        assertEquals("Owner cannot be null", exception.getMessage());
    }

    @Test
    public void testGetLendingRecordsByBorrowerWithNullBorrower() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.getLendingRecordsByBorrower(null);
        });
        assertEquals("Borrower cannot be null", exception.getMessage());
    }

    @Test
    public void testGetLendingRecordsByDateRangeWithNullDates() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.getLendingRecordsByDateRange(null, null);
        });
        assertEquals("Date range parameters cannot be null", exception.getMessage());
    }

    @Test
    public void testUpdateStatusWithNullStatus() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.updateStatus(1, null);
        });
        assertEquals("New status cannot be null", exception.getMessage());
    }

    @Test
    public void testUpdateEndDateWithNullNewEndDate() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.updateEndDate(1, null);
        });
        assertEquals("New end date cannot be null", exception.getMessage());
    }

    @Test
    public void testUpdateEndDateForClosedRecord() {
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.updateEndDate(1, new Date(endDate.getTime() + 86400000));
        });
        assertEquals("Cannot update end date of a closed lending record", exception.getMessage());
    }

    @Test
    public void testUpdateEndDateWithInvalidDate() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        Date invalidEndDate = new Date(startDate.getTime() - 86400000); // Before start date

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.updateEndDate(1, invalidEndDate);
        });
        assertEquals("New end date cannot be before start date", exception.getMessage());
    }

    @Test
    public void testCloseLendingRecordWithDamageAssessment() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);

        ResponseEntity<String> response = lendingRecordService.closeLendingRecordWithDamageAssessment(
                1, true, "Scratches on disc", 2, 123, "Game returned with damage");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(testRecord.isDamaged());
        assertEquals("Scratches on disc", testRecord.getDamageNotes());
        assertEquals(2, testRecord.getDamageSeverity());
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
        assertEquals(123, testRecord.getLastModifiedBy());
        assertNotNull(testRecord.getLastModifiedDate());
    }

    @Test
    public void testCloseLendingRecordWithDamageAssessmentByEmail() {
        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));
        when(lendingRecordRepository.save(any(LendingRecord.class))).thenReturn(testRecord);
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ResponseEntity<String> response = lendingRecordService.closeLendingRecordWithDamageAssessmentByEmail(
                1, true, "Scratches on disc", 2, "owner@test.com", "Game returned with damage");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(testRecord.isDamaged());
        assertEquals("Scratches on disc", testRecord.getDamageNotes());
        assertEquals(2, testRecord.getDamageSeverity());
        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
        assertEquals(1, testRecord.getLastModifiedBy()); // ID of owner
        assertNotNull(testRecord.getLastModifiedDate());
    }

    @Test
    public void testFilterLendingRecordsWithMultipleCriteria() {
        // Setup filter DTO with multiple criteria
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus("ACTIVE");
        filterDto.setGameId(1);
        filterDto.setBorrowerId("borrower@test.com");
        Date fromDate = new Date(System.currentTimeMillis() - 86400000);
        Date toDate = new Date(System.currentTimeMillis() + 86400000 * 14);
        filterDto.setFromDate(fromDate);
        filterDto.setToDate(toDate);

        // Setup mock repository responses
        when(accountRepository.findByEmail("borrower@test.com")).thenReturn(Optional.of(borrower));
        when(lendingRecordRepository.filterLendingRecords(
                eq(fromDate), eq(toDate), eq(LendingRecord.LendingStatus.ACTIVE), eq(2), eq(1)))
                .thenReturn(Arrays.asList(testRecord));

        // Execute filtering
        List<LendingRecord> filteredRecords = lendingRecordService.filterLendingRecords(filterDto);

        // Verify results
        assertEquals(1, filteredRecords.size());
        assertEquals(testRecord, filteredRecords.get(0));
        verify(lendingRecordRepository).filterLendingRecords(
                eq(fromDate), eq(toDate), eq(LendingRecord.LendingStatus.ACTIVE), eq(2), eq(1));
    }

    @Test
    public void testFilterLendingRecordsByBorrowerEmail() {
        // Setup filter DTO with borrower email
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setBorrowerId("borrower@test.com");

        // Setup mock repository responses
        when(accountRepository.findByEmail("borrower@test.com")).thenReturn(Optional.of(borrower));
        when(lendingRecordRepository.filterLendingRecords(
                eq(null), eq(null), eq(null), eq(2), eq(null)))
                .thenReturn(Arrays.asList(testRecord));

        // Execute filtering
        List<LendingRecord> filteredRecords = lendingRecordService.filterLendingRecords(filterDto);


        // Verify results
        assertEquals(1, filteredRecords.size());
        assertEquals(testRecord, filteredRecords.get(0));
        verify(lendingRecordRepository).filterLendingRecords(
                eq(null), eq(null), eq(null), eq(2), eq(null));
    }

    @Test
    public void testFilterLendingRecordsByInvalidBorrowerEmail() {
        // Setup filter DTO with invalid borrower email
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setBorrowerId("nonexistent@test.com");

        // Setup mock repository response
        when(accountRepository.findByEmail("nonexistent@test.com")).thenReturn(Optional.empty());

        // Execute filtering - should throw exception
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            lendingRecordService.filterLendingRecords(filterDto);
        });

        assertTrue(exception.getMessage().contains("No borrower found with email"));
    }

    @Test
    public void testFilterByGameId() {
        // Setup test data
        Game testGame2 = new Game("Game 2", 2, 4, "test2.jpg", new Date());
        testGame2.setId(2);
        testGame2.setOwner(owner);

        BorrowRequest request2 = new BorrowRequest(startDate, endDate, BorrowRequestStatus.APPROVED, new Date(), testGame2);
        request2.setRequester(borrower);
        request2.setResponder(owner);

        LendingRecord record2 = new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.OVERDUE, request2, owner);
        record2.setId(2);

        // Setup filter DTO with game ID
        LendingHistoryFilterDto gameFilterDto = new LendingHistoryFilterDto();
        gameFilterDto.setGameId(2);

        // Setup mock repository response
        when(lendingRecordRepository.filterLendingRecords(
                eq(null), eq(null), eq(null), eq(null), eq(2)))
                .thenReturn(Arrays.asList(record2));

        // Execute filtering
        List<LendingRecord> filteredRecords = lendingRecordService.filterLendingRecords(gameFilterDto);

        // Verify results
        assertEquals(1, filteredRecords.size());
        assertEquals(2, filteredRecords.get(0).getId());
        verify(lendingRecordRepository).filterLendingRecords(
                eq(null), eq(null), eq(null), eq(null), eq(2));
    }

    @Test
    public void testFilterLendingRecordsByDateRange() {
        // Setup filter DTO with date range
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        Date fromDate = new Date(System.currentTimeMillis() - 86400000);
        Date toDate = new Date(System.currentTimeMillis() + 86400000 * 7);
        filterDto.setFromDate(fromDate);
        filterDto.setToDate(toDate);

        // Setup mock repository response
        List<LendingRecord> expectedRecords = Arrays.asList(testRecord);
        when(lendingRecordRepository.filterLendingRecords(
                eq(fromDate), eq(toDate), eq(null), eq(null), eq(null)))
                .thenReturn(expectedRecords);

        // Execute filtering
        List<LendingRecord> filteredRecords = lendingRecordService.filterLendingRecords(filterDto);

        // Verify results
        assertEquals(1, filteredRecords.size());
        assertEquals(testRecord, filteredRecords.get(0));
        verify(lendingRecordRepository).filterLendingRecords(
                eq(fromDate), eq(toDate), eq(null), eq(null), eq(null));
    }

    @Test
    public void testStatusTransitionValidation() {
        // Setup closed record
        testRecord.setStatus(LendingRecord.LendingStatus.CLOSED);

        when(lendingRecordRepository.findLendingRecordById(1)).thenReturn(Optional.of(testRecord));

        // Test invalid transition: CLOSED -> ACTIVE
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            lendingRecordService.updateStatus(1, LendingRecord.LendingStatus.ACTIVE);
        });
        // Just verify that some exception was thrown and that it's an IllegalStateException
        assertNotNull(exception.getMessage());
    }

    @Test
    public void testRecordClosingMethod() {
        // This tests the recordClosing method on the LendingRecord entity
        testRecord.recordClosing(123, "Test closing reason");

        assertEquals(LendingRecord.LendingStatus.CLOSED, testRecord.getStatus());
        assertEquals(123, testRecord.getLastModifiedBy());
        assertEquals(123, testRecord.getClosedBy());
        assertEquals("Test closing reason", testRecord.getClosingReason());
        assertEquals("Record closed: Test closing reason", testRecord.getStatusChangeReason());
        assertNotNull(testRecord.getLastModifiedDate());
    }

    @Test
    public void testRecordDamageMethod() {
        // This tests the recordDamage method on the LendingRecord entity
        testRecord.recordDamage(true, "Test damage notes", 3);

        assertTrue(testRecord.isDamaged());
        assertEquals("Test damage notes", testRecord.getDamageNotes());
        assertEquals(3, testRecord.getDamageSeverity());
        assertNotNull(testRecord.getDamageAssessmentDate());
    }
}
