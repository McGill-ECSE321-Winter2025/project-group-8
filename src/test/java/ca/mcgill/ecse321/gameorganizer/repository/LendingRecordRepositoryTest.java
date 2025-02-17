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
        // Create test data
        startDate = new Date();
        endDate = new Date(startDate.getTime() + 7 * 24 * 60 * 60 * 1000); // 7 days later

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
        
        LendingRecord overdueRecord = new LendingRecord(pastStart, pastEnd, LendingRecord.LendingStatus.ACTIVE, request, owner);
        lendingRecordRepository.save(overdueRecord);

        List<LendingRecord> overdueRecords = lendingRecordRepository.findByEndDateBeforeAndStatus(
            new Date(), LendingRecord.LendingStatus.ACTIVE);
        
        assertFalse(overdueRecords.isEmpty());
        assertTrue(overdueRecords.stream()
            .anyMatch(r -> r.getEndDate().before(new Date()) && r.getStatus() == LendingRecord.LendingStatus.ACTIVE));
    }

    @Test
    public void testFindByBorrower() {
        List<LendingRecord> borrowerRecords = lendingRecordRepository.findByRequest_Borrower(borrower);
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
}
