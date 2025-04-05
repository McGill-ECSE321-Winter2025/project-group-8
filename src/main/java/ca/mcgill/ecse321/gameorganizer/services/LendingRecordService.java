package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.ResourceNotFoundException;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException; // Import UnauthedException
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;

/**
 * Service class that handles business logic for lending record operations.
 * Provides methods for managing the lending of games between owners and borrowers.
 * 
 * @author @YoussGm3o8
 */
@Service
public class LendingRecordService {
    
    private static final Logger log = LoggerFactory.getLogger(LendingRecordService.class);
    @Autowired
    private LendingRecordRepository lendingRecordRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final AccountRepository accountRepository; // Inject AccountRepository

    @Autowired
    public LendingRecordService(LendingRecordRepository lendingRecordRepository, BorrowRequestRepository borrowRequestRepository, AccountRepository accountRepository) {
        this.lendingRecordRepository = lendingRecordRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.accountRepository = accountRepository;
    }

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

            // Validate owner consistency: the owner of the game must match the passed owner.
            if (request.getRequestedGame() == null || request.getRequestedGame().getOwner() == null ||
                request.getRequestedGame().getOwner().getId() != owner.getId()) {
                throw new IllegalArgumentException("The record owner must be the owner of the game in the borrow request");
            }

            // Check if the BorrowRequest is already associated with an existing LendingRecord
            if (lendingRecordRepository.findByRequest(request).isPresent()) {
                throw new IllegalArgumentException("The borrow request already has a lending record associated with it");
            }

            // Validate dates
            Date now = new Date();
            if (endDate.before(startDate)) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
            // Allow a margin of 1 second for the start date (to account for processing delays)
            if (startDate.getTime() < now.getTime() - 1000) {
                throw new IllegalArgumentException("Start date cannot be in the past");
            }

            // Create and save new lending record
            LendingRecord record = new LendingRecord(startDate, endDate, LendingStatus.ACTIVE, request, owner);
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
                
        // Delegate to the other method
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
        LendingStatus status = null;
        if (filterDto.getStatus() != null && !filterDto.getStatus().isEmpty()) {
            try {
                status = LendingStatus.valueOf(filterDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status is ignored
            }
        }
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
        LendingStatus status = null;
        if (filterDto.getStatus() != null && !filterDto.getStatus().isEmpty()) {
            try {
                status = LendingStatus.valueOf(filterDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status is ignored
            }
        }
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
        log.info("Attempting to update status for record ID: {} to {} by user ID: {}. Reason: {}", id, newStatus, userId, reason);
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        
        LendingRecord record;
        try {
            record = getLendingRecordById(id);
        } catch (ResourceNotFoundException e) {
            return createErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
        }
        LendingStatus currentStatus = record.getStatus();
        log.debug("Record found: ID={}, Status={}", record.getId(), currentStatus);
        
        if (currentStatus == newStatus) {
            return ResponseEntity.ok("Status already set to " + newStatus.name());
        }

        // Authorization Check: Only owner or borrower can update status
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication object: {}", authentication != null ? authentication.getName() : "null");
        if (authentication == null) {
            throw new IllegalStateException("Authentication context is missing.");
        }
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthedException("User not authenticated.");
        }
        String userEmail = authentication.getName(); // Assuming email is the username used in UserDetails
        log.debug("Auth check: Authenticated user email: '{}'", userEmail);
        Account currentUser = accountRepository.findByEmail(userEmail).orElseThrow(() -> new UnauthedException("Authenticated user not found in database."));
        log.debug("Current user fetched from DB: {}", currentUser != null ? currentUser.getEmail() : "null");
        // Note: orElseThrow in the line above handles the case where the account is not found for the email.
        // If we need to handle a null currentUser specifically (e.g., if orElseThrow was removed or changed),
        // a check like this would be appropriate:
        // if (currentUser == null) {
        //     throw new ResourceNotFoundException("Authenticated user account could not be retrieved for email: " + userEmail);
        // }
        log.debug("Auth check: Found current user in DB: {}", currentUser != null ? currentUser.getEmail() : "null");
        boolean ownerOrBorrowerCheck = isOwnerOrBorrower(currentUser, record);
        log.debug("isOwnerOrBorrower check result for user {} on record {}: {}", currentUser != null ? currentUser.getEmail() : "null", id, ownerOrBorrowerCheck);
        if (!ownerOrBorrowerCheck) {
             log.warn("Authorization failed for user {} to update status for record ID: {}. User is not owner or borrower.", currentUser != null ? currentUser.getEmail() : "null", id);
             throw new UnauthedException("Access denied: Only the game owner or borrower can update the lending status.");
        }
        
        log.debug("Auth check passed for user {} on record ID: {}", currentUser.getEmail(), id);
        log.debug("Attempting validateStatusTransition for record {} from {} to {}", id, currentStatus, newStatus);
        validateStatusTransition(record, newStatus);
        log.debug("validateStatusTransition passed for record {}", id);
        
        if (isRecordOverdue(record) && newStatus == LendingStatus.ACTIVE) {
            record.setStatus(LendingStatus.OVERDUE);
            record.setLastModifiedDate(new Date());
            record.setLastModifiedBy(userId);
            record.setStatusChangeReason("System automated change: Record is overdue");
            lendingRecordRepository.save(record);
            return ResponseEntity.ok("Record is overdue - status automatically set to OVERDUE instead of ACTIVE");
        }
        
        record.setStatus(newStatus);
        record.setLastModifiedDate(new Date());
        record.setLastModifiedBy(userId);
        record.setStatusChangeReason(reason != null ? reason : "Status updated by user");
        
        log.debug("Attempting to save updated record ID: {} with status {}", record.getId(), record.getStatus());
        // REMOVED REDUNDANT SAVE CALL HERE
        try {
            log.debug("Attempting final save for record ID: {}", record.getId());
            lendingRecordRepository.save(record);
            log.info("Successfully saved updated record ID: {}", record.getId());
        } catch (Exception e) {
            log.error("Error saving record ID: {} during status update", record.getId(), e);
            throw e; // Re-throw the exception to be handled by controller advice or caller
        }
        
        // Original save call removed, handled in try-catch above
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
        
        if (currentStatus == LendingStatus.CLOSED && newStatus != LendingStatus.CLOSED) {
            throw new IllegalStateException(
                String.format("Cannot change status of a closed lending record (ID: %d)", record.getId()));
        }
        
        if (newStatus == LendingStatus.ACTIVE && isRecordOverdue(record)) {
            throw new IllegalStateException(
                String.format("Cannot set record (ID: %d) to ACTIVE as it is overdue", record.getId()));
        }
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
     * @return ResponseEntity with the result of the operation including the updated record ID
     * @throws IllegalArgumentException if no record is found with the given ID
     * @throws IllegalStateException if the record is already closed or cannot be closed
     */
    @Transactional
    public ResponseEntity<String> closeLendingRecord(int id, Integer userId, String reason) {
        LendingRecord record = getLendingRecordById(id);
        LendingStatus currentStatus = record.getStatus();

        // Authorization Check: Only owner can close
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthedException("User not authenticated.");
        }
        String userEmail = authentication.getName(); // Assuming email is the username used in UserDetails
        Account currentUser = accountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UnauthedException("Authenticated user not found in database."));
        if (currentUser == null || record.getRecordOwner() == null || record.getRecordOwner().getId() != currentUser.getId()) {
             throw new UnauthedException("Access denied: Only the game owner can close the lending record.");
        }
        
        if (currentStatus == LendingStatus.CLOSED) {
            throw new IllegalStateException("Lending record is already closed");
        }
        
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
        log.info("Attempting closeLendingRecordWithDamageAssessment for record ID: {}. isDamaged={}, damageSeverity={}, userId={}, reason={}", id, isDamaged, damageSeverity, userId, reason);
        LendingStatus currentStatus = record.getStatus();

        // Authorization Check: Only owner can close
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthedException("User not authenticated.");
        }
        String userEmail = authentication.getName(); // Assuming email is the username used in UserDetails
        log.debug("Auth check (close): Authenticated user email: '{}'", userEmail);
        Account currentUser = accountRepository.findByEmail(userEmail).orElseThrow(() -> new UnauthedException("Authenticated user not found in database."));
        log.debug("Auth check (close): Found current user in DB: {}", currentUser != null ? currentUser.getEmail() : "null");
        if (currentUser == null || record.getRecordOwner() == null || record.getRecordOwner().getId() != currentUser.getId()) {
             log.warn("Authorization failed for user {} to close record ID: {}. User is not owner.", currentUser != null ? currentUser.getEmail() : "null", id);
             throw new UnauthedException("Access denied: Only the game owner can close the lending record.");
        }
        
        log.debug("Checking current status for record ID: {}. Current status: {}", id, currentStatus);
        if (currentStatus == LendingStatus.CLOSED) {
            log.warn("Attempted to close already closed record ID: {}", id);
            throw new IllegalStateException("Lending record is already closed");
        }
        
        if (isDamaged) {
            record.recordDamage(true, damageNotes, damageSeverity);
        }
        
        String damageDetails = isDamaged ? 
                String.format("Game returned with %s damage.", 
                        damageSeverity == 1 ? "minor" : 
                        damageSeverity == 2 ? "moderate" : 
                        damageSeverity == 3 ? "severe" : "unspecified") : 
                "Game returned in good condition.";
                
        String closingReason = reason != null ? reason + ". " + damageDetails : damageDetails;
        
        record.recordClosing(userId, closingReason);
        
        log.debug("Attempting to save closed record ID: {} with damage assessment.", record.getId());
        // REMOVED REDUNDANT SAVE CALL HERE
        try {
            lendingRecordRepository.save(record);
            log.info("Successfully saved closed record ID: {}", record.getId());
        } catch (Exception e) {
            log.error("Error saving record ID: {} during close with damage assessment", record.getId(), e);
            throw e;
        }
        
        // Original save call removed
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
        log.info("Attempting to update end date for record ID: {} to {}", id, newEndDate);
        if (newEndDate == null) {
            throw new IllegalArgumentException("New end date cannot be null");
        }

        LendingRecord record = getLendingRecordById(id);

        // Authorization Check: Only owner can update end date
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthedException("User not authenticated.");
        }
        String userEmail = authentication.getName(); // Assuming email is the username used in UserDetails
        log.debug("Auth check (updateEndDate): Authenticated user email: '{}'", userEmail);
        Account currentUser = accountRepository.findByEmail(userEmail).orElseThrow(() -> new UnauthedException("Authenticated user not found in database."));
        log.debug("Auth check (updateEndDate): Found current user in DB: {}", currentUser != null ? currentUser.getEmail() : "null");
        if (currentUser == null || record.getRecordOwner() == null || record.getRecordOwner().getId() != currentUser.getId()) {
             log.warn("Authorization failed for user {} to update end date for record ID: {}. User is not owner.", currentUser != null ? currentUser.getEmail() : "null", id);
             throw new UnauthedException("Access denied: Only the game owner can update the end date.");
        }
        
        if (record.getStatus() == LendingStatus.CLOSED) {
            log.warn("Attempted to update end date on closed record ID: {}", id);
            throw new IllegalStateException("Cannot update end date of a closed lending record");
        }
        
        if (newEndDate.before(record.getStartDate())) {
            log.warn("Attempted to set invalid end date ({}) for record ID: {}. End date cannot be before start date ({}).", newEndDate, id, record.getStartDate());
            throw new IllegalArgumentException("New end date cannot be before start date");
        }
        
        record.setEndDate(newEndDate);
        log.debug("Attempting to save record ID: {} with updated end date.", record.getId());
        // REMOVED REDUNDANT SAVE CALL HERE
        try {
            lendingRecordRepository.save(record);
            log.info("Successfully saved record ID: {} with updated end date.", record.getId());
        } catch (Exception e) {
            log.error("Error saving record ID: {} during end date update", record.getId(), e);
            throw e;
        }
        
        // Original save call removed
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
        try {
            LendingRecord record = getLendingRecordById(id);

            // Authorization Check: Only owner can delete (non-active) record
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
                throw new UnauthedException("User not authenticated.");
            }
            String userEmail = authentication.getName(); // Assuming email is the username used in UserDetails
            Account currentUser = accountRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UnauthedException("Authenticated user not found in database."));
            if (currentUser == null || record.getRecordOwner() == null || record.getRecordOwner().getId() != currentUser.getId()) {
                 throw new UnauthedException("Access denied: Only the game owner can delete this lending record.");
            }
            
            if (record.getStatus() == LendingStatus.ACTIVE) {
                throw new IllegalStateException("Cannot delete an active lending record");
            }

            lendingRecordRepository.delete(record);
            return ResponseEntity.ok("Lending record deleted successfully");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Helper method to check if the current user is the owner or the borrower of the lending record.
     * @param currentUser The currently authenticated user.
     * @param record The lending record.
     * @return true if the user is the owner or borrower, false otherwise.
     */
    private boolean isOwnerOrBorrower(Account currentUser, LendingRecord record) {
        if (currentUser == null || record == null) {
            return false;
        }
        boolean isOwner = record.getRecordOwner() != null && record.getRecordOwner().getId() == currentUser.getId();
        boolean isBorrower = record.getRequest() != null && record.getRequest().getRequester() != null &&
                             record.getRequest().getRequester().getId() == currentUser.getId();
        return isOwner || isBorrower;
    }
}
