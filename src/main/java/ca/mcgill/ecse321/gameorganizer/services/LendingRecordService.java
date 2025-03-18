package ca.mcgill.ecse321.gameorganizer.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.dto.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.ResourceNotFoundException;

/**
 * Service class that handles business logic for lending record operations.
 * Provides methods for managing the lending of games between owners and borrowers.
 * 
 * @author @YoussGm3o8
 */
@Service
public class LendingRecordService {
    
    @Autowired
    private LendingRecordRepository lendingRecordRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    /**
     * Helper method to create a standardized error response.
     * 
     * @param status The HTTP status code
     * @param message The error message
     * @return ResponseEntity with error details
     */
    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(message);
    }
    
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
        try {
            // Validate parameters
            if (startDate == null || endDate == null || request == null || owner == null) {
                throw new IllegalArgumentException("Required parameters cannot be null");
            }

            // Validate owner
            if (!request.getRequestedGame().getOwner().equals(owner)) {
                throw new IllegalArgumentException("The record owner must be the owner of the game in the borrow request");
            }

            // Validate dates
            Date now = new Date();
            if (endDate.before(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
            if (startDate.before(now)) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }

            // Create and save new lending record
            LendingRecord record = new LendingRecord(startDate, endDate, LendingRecord.LendingStatus.ACTIVE, request, owner);
            lendingRecordRepository.save(record);

            return ResponseEntity.ok("Lending record created successfully");
        } catch (IllegalArgumentException e) {
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create lending record: " + e.getMessage());
        }
    }
    
    /**
     * Creates a new lending record for a game loan using borrowRequestId.
     * This method provides a convenient way to create lending records from API requests.
     *
     * @param startDate the start date of the lending period
     * @param endDate the end date of the lending period
     * @param requestId the ID of the associated borrow request
     * @param owner the game owner
     * @return ResponseEntity with success message and the created lending record ID
     * @throws IllegalArgumentException if parameters are invalid
     * @throws IllegalStateException if creation fails
     */
    @Transactional
    public ResponseEntity<String> createLendingRecordFromRequestId(Date startDate, Date endDate, int requestId, GameOwner owner) {
        // Retrieve the borrow request entity
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + requestId));
                
        // Now use the existing method to create the lending record
        return createLendingRecord(startDate, endDate, request, owner);
    }

    /**
     * Retrieves a lending record by its ID.
     *
     * @param id The ID of the lending record to retrieve
     * @return The LendingRecord object
     * @throws ResourceNotFoundException if no record is found with the given ID
     */
    @Transactional
    public LendingRecord getLendingRecordById(int id) {
        return lendingRecordRepository.findLendingRecordById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No lending record found with ID " + id));
    }

    /**
     * Retrieves all lending records.
     *
     * @return List of all lending records
     */
    @Transactional
    public List<LendingRecord> getAllLendingRecords() {
        return lendingRecordRepository.findAll();
    }

    /**
     * Retrieves all lending records associated with a specific game owner.
     *
     * @param owner The GameOwner whose records to retrieve
     * @return List of lending records for the owner
     * @throws IllegalArgumentException if owner is null
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByOwner(GameOwner owner) {
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
        return lendingRecordRepository.findByRecordOwner(owner);
    }

    /**
     * Retrieves lending records within a specific date range.
     *
     * @param startDate The start date of the range (inclusive)
     * @param endDate The end date of the range (inclusive)
     * @return List of lending records within the date range
     * @throws IllegalArgumentException if either date is null
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Date range parameters cannot be null");
        }
        return lendingRecordRepository.findByStartDateBetween(startDate, endDate);
    }

    /**
     * Retrieves all lending records associated with a specific borrower.
     *
     * @param borrower The Account of the borrower
     * @return List of lending records for the borrower
     * @throws IllegalArgumentException if borrower is null
     */
    @Transactional
    public List<LendingRecord> getLendingRecordsByBorrower(Account borrower) {
        if (borrower == null) {
            throw new IllegalArgumentException("Borrower cannot be null");
        }
        return lendingRecordRepository.findByRequest_Requester(borrower);
    }
    
    /**
     * Applies multiple filters to lending records.
     * 
     * @param filterDto The DTO containing filter criteria
     * @return Filtered list of lending records
     */
    @Transactional
    public List<LendingRecord> filterLendingRecords(LendingHistoryFilterDto filterDto) {
        // Convert string status to enum if provided
        LendingStatus status = null;
        if (filterDto.getStatus() != null && !filterDto.getStatus().isEmpty()) {
            try {
                status = LendingStatus.valueOf(filterDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status - will be treated as null (no filter)
            }
        }
        
        // Use the new repository method for database-level filtering
        return lendingRecordRepository.filterLendingRecords(
            filterDto.getFromDate(),
            filterDto.getToDate(),
            status,
            filterDto.getBorrowerId(),
            filterDto.getGameId()
        );
    }
    
    /**
     * Paginated version of filterLendingRecords.
     * 
     * @param filterDto The DTO containing filter criteria
     * @param pageable The pagination information
     * @return Page of filtered lending records
     */
    @Transactional
    public Page<LendingRecord> filterLendingRecordsPaginated(LendingHistoryFilterDto filterDto, Pageable pageable) {
        // Convert string status to enum if provided
        LendingStatus status = null;
        if (filterDto.getStatus() != null && !filterDto.getStatus().isEmpty()) {
            try {
                status = LendingStatus.valueOf(filterDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status - will be treated as null (no filter)
            }
        }
        
        // Use the new repository method for database-level filtering with pagination
        return lendingRecordRepository.filterLendingRecords(
            filterDto.getFromDate(),
            filterDto.getToDate(),
            status,
            filterDto.getBorrowerId(),
            filterDto.getGameId(),
            pageable
        );
    }

    /**
     * Updates the status of a lending record with comprehensive validation of state transitions.
     * Prevents invalid transitions and automatically handles overdue detection.
     *
     * @param id The ID of the record to update
     * @param newStatus The new status to set
     * @param userId The ID of the user making the change
     * @param reason The reason for the status change
     * @return ResponseEntity with the result of the operation including the updated record ID
     * @throws IllegalArgumentException if no record is found with the given ID
     * @throws IllegalStateException if the status transition is not allowed
     */
    @Transactional
    public ResponseEntity<String> updateStatus(int id, LendingStatus newStatus, Integer userId, String reason) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        LendingRecord record = getLendingRecordById(id);
        LendingStatus currentStatus = record.getStatus();
        
        // Don't update if status is not changing
        if (currentStatus == newStatus) {
            return ResponseEntity.ok("Status already set to " + newStatus.name());
        }
        
        // Validate state transitions based on business rules
        validateStatusTransition(record, newStatus);
        
        // Perform automatic status updates if needed
        if (isRecordOverdue(record) && newStatus == LendingStatus.ACTIVE) {
            // If a record is actually overdue but someone tries to set it to ACTIVE,
            // automatically set it to OVERDUE instead
            record.setStatus(LendingStatus.OVERDUE);
            
            // Record audit information
            record.setLastModifiedDate(new Date());
            record.setLastModifiedBy(userId);
            record.setStatusChangeReason("System automated change: Record is overdue");
            
            lendingRecordRepository.save(record);
            return ResponseEntity.ok("Record is overdue - status automatically set to OVERDUE instead of ACTIVE");
        }
        
        // Update the status
        record.setStatus(newStatus);
        
        // Record audit information
        record.setLastModifiedDate(new Date());
        record.setLastModifiedBy(userId);
        record.setStatusChangeReason(reason != null ? reason : "Status updated by user");
        
        lendingRecordRepository.save(record);
        
        return ResponseEntity.ok("Lending record status updated successfully");
    }
    
    /**
     * Updates the status of a lending record without audit information.
     * This is a backward-compatible method for existing code.
     *
     * @param id The ID of the record to update
     * @param newStatus The new status to set
     * @return ResponseEntity with the result of the operation
     */
    @Transactional
    public ResponseEntity<String> updateStatus(int id, LendingStatus newStatus) {
        // Call the full method with null audit values
        return updateStatus(id, newStatus, null, "Status updated via API");
    }
    
    /**
     * Validates if a status transition is allowed based on business rules.
     * 
     * @param record The lending record
     * @param newStatus The new status to validate
     * @throws IllegalStateException if the status transition is not allowed
     */
    private void validateStatusTransition(LendingRecord record, LendingStatus newStatus) {
        LendingStatus currentStatus = record.getStatus();
        
        // Rule 1: Cannot change status of a closed record
        if (currentStatus == LendingStatus.CLOSED && newStatus != LendingStatus.CLOSED) {
            throw new IllegalStateException(
                String.format("Cannot change status of a closed lending record (ID: %d)", record.getId()));
        }
        
        // Rule 2: Cannot set to ACTIVE if the end date is in the past
        if (newStatus == LendingStatus.ACTIVE && isRecordOverdue(record)) {
            throw new IllegalStateException(
                String.format("Cannot set record (ID: %d) to ACTIVE as it is overdue", record.getId()));
        }
        
        // Additional rules could be added here, for example:
        // - Only certain user roles can make certain transitions
        // - Transitions might require additional data (like return verification)
    }
    
    /**
     * Checks if a lending record is overdue based on the current date and the record's end date.
     * Made protected for testing purposes.
     * 
     * @param record The lending record to check
     * @return true if the record is overdue, false otherwise
     */
    protected boolean isRecordOverdue(LendingRecord record) {
        Date now = new Date();
        return record.getEndDate().before(now);
    }

    /**
     * Closes a lending record by setting its status to CLOSED.
     * Performs additional validation before closing the record.
     *
     * @param id The ID of the record to close
     * @param userId ID of the user closing the record
     * @param reason Reason for closing the record
     * @return ResponseEntity with the result of the operation
     * @throws IllegalArgumentException if no record is found with the given ID
     * @throws IllegalStateException if the record is already closed or cannot be closed
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecord(int id, Integer userId, String reason) {
        LendingRecord record = getLendingRecordById(id);
        LendingStatus currentStatus = record.getStatus();
        
        // Check if already closed
        if (currentStatus == LendingStatus.CLOSED) {
            throw new IllegalStateException("Lending record is already closed");
        }
        
        // Record closing with audit information
        record.recordClosing(userId, reason != null ? reason : "Game returned in good condition");
        
        lendingRecordRepository.save(record);
        
        return ResponseEntity.ok(String.format(
            "Lending record (ID: %d) successfully closed. Previous status was %s", 
            record.getId(), currentStatus));
    }
    
    /**
     * Closes a lending record without audit information.
     * This is a backward-compatible method for existing code.
     *
     * @param id The ID of the record to close
     * @return ResponseEntity with the result of the operation
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecord(int id) {
        return closeLendingRecord(id, null, "Closed via API");
    }
    
    /**
     * Closes a lending record with damage information.
     * Records details about any damage to the game during the borrowing period.
     *
     * @param id The ID of the record to close
     * @param isDamaged Flag indicating if the game was damaged
     * @param damageNotes Description of the damage (if any)
     * @param damageSeverity Severity of the damage (0-3, where 0 is none and 3 is severe)
     * @param userId ID of the user closing the record
     * @param reason Additional reason for closing (beyond damage info)
     * @return ResponseEntity with the result of the operation
     * @throws IllegalArgumentException if no record is found with the given ID
     * @throws IllegalStateException if the record is already closed or cannot be closed
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecordWithDamageAssessment(
            int id, boolean isDamaged, String damageNotes, int damageSeverity,
            Integer userId, String reason) {
        LendingRecord record = getLendingRecordById(id);
        LendingStatus currentStatus = record.getStatus();
        
        // Check if already closed
        if (currentStatus == LendingStatus.CLOSED) {
            throw new IllegalStateException("Lending record is already closed");
        }
        
        // Record damage information
        if (isDamaged) {
            record.recordDamage(true, damageNotes, damageSeverity);
        }
        
        // Prepare closing reason
        String damageDetails = isDamaged ? 
                String.format("Game returned with %s damage.", 
                        damageSeverity == 1 ? "minor" : 
                        damageSeverity == 2 ? "moderate" : 
                        damageSeverity == 3 ? "severe" : "unspecified") : 
                "Game returned in good condition.";
                
        String closingReason = reason != null ? reason + ". " + damageDetails : damageDetails;
        
        // Record closing with audit information
        record.recordClosing(userId, closingReason);
        
        lendingRecordRepository.save(record);
        
        return ResponseEntity.ok("Lending record closed successfully");
    }
    
    /**
     * Closes a lending record with damage information but without audit information.
     * This is a backward-compatible method for existing code.
     *
     * @param id The ID of the record to close
     * @param isDamaged Flag indicating if the game was damaged
     * @param damageNotes Description of the damage (if any)
     * @param damageSeverity Severity of the damage (0-3, where 0 is none and 3 is severe)
     * @return ResponseEntity with the result of the operation
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecordWithDamageAssessment(
            int id, boolean isDamaged, String damageNotes, int damageSeverity) {
        return closeLendingRecordWithDamageAssessment(id, isDamaged, damageNotes, damageSeverity, null, null);
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
     * @param id The ID of the record to update
     * @param newEndDate The new end date to set
     * @return ResponseEntity with the result of the operation
     * @throws IllegalArgumentException if no record is found with the given ID or if the new date is invalid
     * @throws IllegalStateException if the record is closed
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
        
        return ResponseEntity.ok("End date updated successfully");
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
