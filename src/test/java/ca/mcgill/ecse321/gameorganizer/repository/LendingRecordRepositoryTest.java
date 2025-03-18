package ca.mcgill.ecse321.gameorganizer.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import ca.mcgill.ecse321.gameorganizer.models.*;
import ca.mcgill.ecse321.gameorganizer.repositories.*;

@SpringBootTest
public class LendingRecordRepositoryTest {

    @Autowired
    private LendingRecordRepository lendingRecordRepository;

    @Autowired
    private AccountRepository gameOwnerRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    private GameOwner owner;
    private Game game;
    private Account borrower;
    private BorrowRequest request;
    private LendingRecord record;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    public void setup() {
        // Create test data with future dates
        startDate = new Date(System.currentTimeMillis() + 86400000); // 1 day in future
        endDate = new Date(startDate.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days after start

        owner = new GameOwner("Test Owner", "owner@test.com", "password123");
        gameOwnerRepository.save(owner);

        game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        game.setOwner(owner);
        gameRepository.save(game);

        borrower = new Account("Test Borrower", "borrower@test.com", "password123");
        accountRepository.save(borrower);

        request = new BorrowRequest(startDate, endDate, BorrowRequestStatus.APPROVED, new Date(), game);
        request.setRequester(borrower);
        request.setResponder(owner);
        borrowRequestRepository.save(request);

        record = new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
        lendingRecordRepository.save(record);
    }

    @AfterEach
    public void cleanup() {
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        gameOwnerRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadLendingRecord() {
        // Test basic persistence
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(record.getId(), found.getId());
        assertEquals(record.getStartDate(), found.getStartDate());
        assertEquals(record.getEndDate(), found.getEndDate());
        assertEquals(record.getStatus(), found.getStatus());
    }

    @Test
    public void testFindByStatus() {
        List<LendingRecord> activeRecords = lendingRecordRepository.findByStatus(LendingRecord.LendingStatus.ACTIVE);
        assertFalse(activeRecords.isEmpty());
        assertEquals(LendingRecord.LendingStatus.ACTIVE, activeRecords.get(0).getStatus());

        List<LendingRecord> closedRecords = lendingRecordRepository.findByStatus(LendingRecord.LendingStatus.CLOSED);
        assertTrue(closedRecords.isEmpty());
    }

    @Test
    public void testFindByRecordOwner() {
        List<LendingRecord> ownerRecords = lendingRecordRepository.findByRecordOwner(owner);
        assertFalse(ownerRecords.isEmpty());
        assertEquals(owner.getId(), ownerRecords.get(0).getRecordOwner().getId());
    }

    @Test
    public void testFindByDateRange() {
        Date rangeStart = new Date(startDate.getTime() - 86400000); // 1 day before
        Date rangeEnd = new Date(endDate.getTime() + 86400000); // 1 day after

        List<LendingRecord> records = lendingRecordRepository.findByStartDateBetween(rangeStart, rangeEnd);
        assertFalse(records.isEmpty());
        assertTrue(records.get(0).getStartDate().after(rangeStart));
        assertTrue(records.get(0).getStartDate().before(rangeEnd));
    }

    @Test
    public void testFindOverdueRecords() {
        // Create an overdue record
        Date pastStart = new Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000); // 14 days ago
        Date pastEnd = new Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000); // 7 days ago

        // Create new borrow request for the overdue record
        BorrowRequest overdueRequest = new BorrowRequest(pastStart, pastEnd, BorrowRequestStatus.APPROVED, new Date(), game);
        overdueRequest.setRequester(borrower);
        overdueRequest.setResponder(owner);
        borrowRequestRepository.save(overdueRequest);

        LendingRecord overdueRecord = new LendingRecord(pastStart, pastEnd, LendingRecord.LendingStatus.ACTIVE, overdueRequest, owner);
        lendingRecordRepository.save(overdueRecord);

        List<LendingRecord> overdueRecords = lendingRecordRepository.findByEndDateBeforeAndStatus(
                new Date(), LendingRecord.LendingStatus.ACTIVE);

        assertFalse(overdueRecords.isEmpty());
        assertTrue(overdueRecords.stream()
                .anyMatch(r -> r.getEndDate().before(new Date()) && r.getStatus() == LendingRecord.LendingStatus.ACTIVE));
    }

    @Test
    public void testFindByBorrower() {
        List<LendingRecord> borrowerRecords = lendingRecordRepository.findByRequest_Requester(borrower);
        assertFalse(borrowerRecords.isEmpty());
        assertEquals(borrower.getId(), borrowerRecords.get(0).getRequest().getRequester().getId());
    }

    @Test
    public void testCascadeDelete() {
        // Test that deleting a lending record doesn't cascade to related entities
        lendingRecordRepository.delete(record);

        assertTrue(gameOwnerRepository.existsById(owner.getId()));
        assertTrue(gameRepository.existsById(game.getId()));
        assertTrue(accountRepository.existsById(borrower.getId()));
        assertTrue(borrowRequestRepository.existsById(request.getId()));
    }

    @Test
    public void testRelationshipMappings() {
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Test relationship with GameOwner
        assertNotNull(found.getRecordOwner());
        assertEquals(owner.getId(), found.getRecordOwner().getId());

        // Test relationship with BorrowRequest
        assertNotNull(found.getRequest());
        assertEquals(request.getId(), found.getRequest().getId());

        // Test nested relationships
        assertEquals(game.getId(), found.getRequest().getRequestedGame().getId());
        assertEquals(borrower.getId(), found.getRequest().getRequester().getId());
    }

    @Test
    public void testUpdateAttributes() {
        // Test updating various attributes
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Update dates
        Date newStartDate = new Date(startDate.getTime() + 86400000); // +1 day
        Date newEndDate = new Date(endDate.getTime() + 86400000); // +1 day
        found.setStartDate(newStartDate);
        found.setEndDate(newEndDate);

        // Update status
        found.setStatus(LendingRecord.LendingStatus.OVERDUE);

        // Save changes
        lendingRecordRepository.save(found);

        // Verify changes persisted
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(newStartDate, updated.getStartDate());
        assertEquals(newEndDate, updated.getEndDate());
        assertEquals(LendingRecord.LendingStatus.OVERDUE, updated.getStatus());
    }

    @Test
    public void testDamageAssessmentFields() {
        // Test damage assessment fields
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Initially, damage fields should have default values
        assertFalse(found.isDamaged());
        assertEquals(0, found.getDamageSeverity());
        assertNull(found.getDamageNotes());
        assertNull(found.getDamageAssessmentDate());

        // Update damage fields
        Date assessmentDate = new Date();
        found.setDamaged(true);
        found.setDamageSeverity(2);
        found.setDamageNotes("Moderate damage to game box");
        found.setDamageAssessmentDate(assessmentDate);

        lendingRecordRepository.save(found);

        // Verify persistence of damage fields
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.isDamaged());
        assertEquals(2, updated.getDamageSeverity());
        assertEquals("Moderate damage to game box", updated.getDamageNotes());
        assertEquals(assessmentDate, updated.getDamageAssessmentDate());
    }

    @Test
    public void testAuditFields() {
        // Test audit fields
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Initially, audit fields should be null
        assertNull(found.getLastModifiedBy());
        assertNull(found.getClosedBy());
        assertNull(found.getClosingReason());

        // Set audit fields
        Date modificationDate = new Date();
        found.setLastModifiedDate(modificationDate);
        found.setLastModifiedBy(123);
        found.setStatusChangeReason("Status changed due to late return");
        found.setClosedBy(456);
        found.setClosingReason("Game returned with damage");

        lendingRecordRepository.save(found);

        // Verify persistence of audit fields
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(modificationDate, updated.getLastModifiedDate());
        assertEquals(Integer.valueOf(123), updated.getLastModifiedBy());
        assertEquals("Status changed due to late return", updated.getStatusChangeReason());
        assertEquals(Integer.valueOf(456), updated.getClosedBy());
        assertEquals("Game returned with damage", updated.getClosingReason());
    }

    @Test
    public void testRecordDamageMethod() {
        // Test the recordDamage method
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Call the recordDamage method
        found.recordDamage(true, "Scratches on game disc", 3);
        lendingRecordRepository.save(found);

        // Verify the changes
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertTrue(updated.isDamaged());
        assertEquals("Scratches on game disc", updated.getDamageNotes());
        assertEquals(3, updated.getDamageSeverity());
        assertNotNull(updated.getDamageAssessmentDate());
    }

    @Test
    public void testRecordClosingMethod() {
        // Test the recordClosing method
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // Call the recordClosing method
        found.recordClosing(789, "Game returned in good condition");
        lendingRecordRepository.save(found);

        // Verify the changes
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(LendingRecord.LendingStatus.CLOSED, updated.getStatus());
        assertEquals(Integer.valueOf(789), updated.getClosedBy());
        assertEquals(Integer.valueOf(789), updated.getLastModifiedBy());
        assertEquals("Game returned in good condition", updated.getClosingReason());
        assertNotNull(updated.getLastModifiedDate());
        assertEquals("Record closed: Game returned in good condition", updated.getStatusChangeReason());
    }

    @Test
    public void testGetDurationInDays() {
        // Test the getDurationInDays method
        LendingRecord found = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(found);

        // The duration should be 7 days based on our setup
        assertEquals(7, found.getDurationInDays());

        // Change the end date to test different duration
        Date newEndDate = new Date(startDate.getTime() + 3 * 86400000); // 3 days after start
        found.setEndDate(newEndDate);
        lendingRecordRepository.save(found);

        // Verify the updated duration
        LendingRecord updated = lendingRecordRepository.findLendingRecordById(record.getId()).orElse(null);
        assertNotNull(updated);
        assertEquals(3, updated.getDurationInDays());
    }

    @Test
    public void testConstructorValidations() {
        // Test constructor validations

        // 1. End date before start date
        Date invalidEndDate = new Date(startDate.getTime() - 86400000); // 1 day before start
        Exception exception1 = assertThrows(IllegalArgumentException.class, () -> {
            new LendingRecord(startDate, invalidEndDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
        });
        assertEquals("End date cannot be before start date", exception1.getMessage());

        // 2. Null parameters
        Exception exception2 = assertThrows(IllegalArgumentException.class, () -> {
            new LendingRecord(null, endDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
        });
        assertEquals("Required fields cannot be null", exception2.getMessage());

        // 3. Mismatch between owner and game owner
        GameOwner differentOwner = new GameOwner("Different Owner", "different@test.com", "password123");
        gameOwnerRepository.save(differentOwner);

        Exception exception3 = assertThrows(IllegalArgumentException.class, () -> {
            new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.ACTIVE, request, differentOwner);
        });
        assertEquals("The record owner must be the owner of the game in the borrow request", exception3.getMessage());
    }
}