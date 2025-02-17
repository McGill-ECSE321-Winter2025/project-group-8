package ca.mcgill.ecse321.gameorganizer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;

/**
 * Service class that handles business logic for lending record operations.
 * Provides methods for creating, updating, and managing lending records.
 */
@Service
public class LendingRecordService {
    
    @Autowired
    private LendingRecordRepository lendingRecordRepository;

    /**
     * Creates a new lending record for a game loan.
     *
     * @param startDate the start date of the lending period
     * @param endDate the end date of the lending period
     * @param request the associated borrow request
     * @param owner the game owner
     * @return ResponseEntity with success message
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if creation fails
     */
    @Transactional
    public ResponseEntity<String> createLendingRecord(Date startDate, Date endDate, BorrowRequest request, GameOwner owner) {
        // Validate input parameters
        if (startDate == null || endDate == null || request == null || owner == null) {
            throw new IllegalArgumentException("Required parameters cannot be null");
        }

        // Validate date range
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        
        // Validate that start date is not in the past
        if (startDate.before(new Date())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        // Validate owner matches the game owner
        if (!request.getRequestedGame().getOwner().equals(owner)) {
            throw new IllegalArgumentException("The record owner must be the owner of the game in the borrow request");
        }

        try {
            LendingRecord record = new LendingRecord(startDate, endDate, LendingStatus.ACTIVE, request, owner);
            lendingRecordRepository.save(record);
            return ResponseEntity.ok("Lending record created successfully");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create lending record: " + e.getMessage());
        }
    }

    /**
     * Retrieves a lending record by its ID.
     *
     * @param id the ID of the lending record
     * @return the lending record
     * @throws IllegalArgumentException if the lending record is not found
     */
    @Transactional
    public LendingRecord getLendingRecordById(int id) {
        return lendingRecordRepository.findLendingRecordById(id)
            .orElseThrow(() -> new IllegalArgumentException("Lending record not found with id: " + id));
    }

    /**
     * Retrieves lending records by the game owner.
     *
     * @param owner the game owner
     * @return list of lending records
     * @throws IllegalArgumentException if the owner is null
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByOwner(GameOwner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        return lendingRecordRepository.findByRecordOwner(owner);
    }

    /**
     * Retrieves lending records by the borrower.
     *
     * @param borrower the borrower account
     * @return list of lending records
     * @throws IllegalArgumentException if the borrower is null
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByBorrower(Account borrower) {
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null");
        }
        return lendingRecordRepository.findByRequest_Borrower(borrower);
    }

    /**
     * Retrieves lending records within a specific date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return list of lending records
     * @throws IllegalArgumentException if date range parameters are invalid
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range parameters cannot be null");
        }
        if (endDate.before(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        return lendingRecordRepository.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Updates the status of a lending record.
     *
     * @param id the ID of the lending record
     * @param newStatus the new status to set
     * @return ResponseEntity with success message
     * @throws IllegalArgumentException if the new status is null
     * @throws IllegalStateException if the status transition is invalid
     */
    @Transactional
    public ResponseEntity<String> updateStatus(int id, LendingStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }

        LendingRecord record = getLendingRecordById(id);
        
        // Validate status transition
        if (record.getStatus() == LendingStatus.CLOSED) {
            throw new IllegalStateException("Cannot update status of a closed lending record");
        }

        record.setStatus(newStatus);
        lendingRecordRepository.save(record);
        return ResponseEntity.ok("Lending record status updated successfully");
    }

    /**
     * Closes a lending record.
     *
     * @param id the ID of the lending record
     * @return ResponseEntity with success message
     * @throws IllegalStateException if the lending record is already closed
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecord(int id) {
        LendingRecord record = getLendingRecordById(id);
        
        if (record.getStatus() == LendingStatus.CLOSED) {
            throw new IllegalStateException("Lending record is already closed");
        }

        record.setStatus(LendingStatus.CLOSED);
        lendingRecordRepository.save(record);
        return ResponseEntity.ok("Lending record closed successfully");
    }

    /**
     * Finds overdue lending records.
     *
     * @return list of overdue lending records
     */
    @Transactional
    public List<LendingRecord> findOverdueRecords() {
        return lendingRecordRepository.findByEndDateBeforeAndStatus(new Date(), LendingStatus.ACTIVE);
    }

    /**
     * Updates the end date of a lending record.
     *
     * @param id the ID of the lending record
     * @param newEndDate the new end date to set
     * @return ResponseEntity with success message
     * @throws IllegalArgumentException if the new end date is null or invalid
     * @throws IllegalStateException if the lending record is closed
     */
    @Transactional
    public ResponseEntity<String> updateEndDate(int id, Date newEndDate) {
        if (newEndDate == null) {
            throw new IllegalArgumentException("New end date cannot be null");
        }

        LendingRecord record = getLendingRecordById(id);
        
        if (record.getStatus() == LendingStatus.CLOSED) {
            throw new IllegalStateException("Cannot update end date of a closed lending record");
        }

        if (newEndDate.before(record.getStartDate())) {
            throw new IllegalArgumentException("New end date cannot be before start date");
        }

        record.setEndDate(newEndDate);
        lendingRecordRepository.save(record);
        return ResponseEntity.ok("Lending record end date updated successfully");
    }

    /**
     * Deletes a lending record.
     *
     * @param id the ID of the lending record
     * @return ResponseEntity with success message
     * @throws IllegalStateException if the lending record is active
     */
    @Transactional
    public ResponseEntity<String> deleteLendingRecord(int id) {
        LendingRecord record = getLendingRecordById(id);
        
        if (record.getStatus() == LendingStatus.ACTIVE) {
            throw new IllegalStateException("Cannot delete an active lending record");
        }

        lendingRecordRepository.delete(record);
        return ResponseEntity.ok("Lending record deleted successfully");
    }
}
