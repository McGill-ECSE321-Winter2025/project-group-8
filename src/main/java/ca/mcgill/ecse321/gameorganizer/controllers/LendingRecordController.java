package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * REST controller for managing lending record operations.
 * Provides endpoints for viewing lending history and managing game returns.
 * 
 * @author YoussGm3o8
 */
@RestController
@RequestMapping("/api/lending-records")
public class LendingRecordController {

    @Autowired
    private LendingRecordService lendingRecordService;

    /**
     * Retrieves all lending records for a game owner.
     * Implements Use Case 9: View Lending History
     *
     * @param ownerId the ID of the game owner
     * @return list of lending records
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<LendingRecord>> getLendingHistoryByOwner(@PathVariable int ownerId) {
        // In a real implementation, you would use a service to fetch the GameOwner by ID
        GameOwner owner = new GameOwner(); // Placeholder - in real app, get from service
        owner.setId(ownerId);
        
        List<LendingRecord> records = lendingRecordService.getLendingRecordsByOwner(owner);
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves lending records for a game owner filtered by status.
     * Implements Use Case 9: View Lending History with filtering
     *
     * @param ownerId the ID of the game owner
     * @param status the status to filter by (ACTIVE, OVERDUE, CLOSED)
     * @return filtered list of lending records
     */
    @GetMapping("/owner/{ownerId}/status/{status}")
    public ResponseEntity<List<LendingRecord>> getLendingHistoryByOwnerAndStatus(
            @PathVariable int ownerId,
            @PathVariable String status) {
        // In a real implementation, you would use a service to fetch the GameOwner by ID
        GameOwner owner = new GameOwner(); // Placeholder - in real app, get from service
        owner.setId(ownerId);
        
        List<LendingRecord> allRecords = lendingRecordService.getLendingRecordsByOwner(owner);
        
        // Filter by status - in a real implementation, this might be done at the repository level
        LendingStatus requestedStatus = LendingStatus.valueOf(status.toUpperCase());
        List<LendingRecord> filteredRecords = allRecords.stream()
                .filter(record -> record.getStatus() == requestedStatus)
                .toList();
        
        return ResponseEntity.ok(filteredRecords);
    }

    /**
     * Retrieves lending records for a game owner within a date range.
     * Implements Use Case 9: View Lending History with date filtering
     *
     * @param ownerId the ID of the game owner
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return filtered list of lending records
     */
    @GetMapping("/owner/{ownerId}/date-range")
    public ResponseEntity<List<LendingRecord>> getLendingHistoryByOwnerAndDateRange(
            @PathVariable int ownerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        
        List<LendingRecord> records = lendingRecordService.getLendingRecordsByDateRange(startDate, endDate);
        
        // Further filter by owner - in a real implementation, this might be done at the repository level
        // In a real implementation, you would use a service to fetch the GameOwner by ID
        GameOwner owner = new GameOwner(); // Placeholder - in real app, get from service
        owner.setId(ownerId);
        
        List<LendingRecord> filteredRecords = records.stream()
                .filter(record -> record.getRecordOwner().getId() == ownerId)
                .toList();
        
        return ResponseEntity.ok(filteredRecords);
    }

    /**
     * Retrieves a single lending record by ID.
     * Supports Use Case 9: View Lending History (detailed view)
     *
     * @param id the ID of the lending record
     * @return the lending record
     */
    @GetMapping("/{id}")
    public ResponseEntity<LendingRecord> getLendingRecordById(@PathVariable int id) {
        LendingRecord record = lendingRecordService.getLendingRecordById(id);
        return ResponseEntity.ok(record);
    }

    /**
     * Marks a game as returned by the borrower.
     * Implements Use Case 13: Return Borrowed Game (borrower action)
     *
     * @param id the ID of the lending record
     * @return response with success message
     */
    @PostMapping("/{id}/mark-returned")
    public ResponseEntity<String> markGameAsReturned(@PathVariable int id) {
        // Update the status to PENDING_RETURN_CONFIRMATION
        // In a real implementation, you might have this status in the enum
        // For now, we'll use OVERDUE as a placeholder for "Pending Return Confirmation"
        return lendingRecordService.updateStatus(id, LendingStatus.OVERDUE);
    }

    /**
     * Confirms receipt of a returned game by the owner.
     * Implements Use Case 13: Return Borrowed Game (owner confirmation)
     *
     * @param id the ID of the lending record
     * @param isDamaged flag indicating if the game was returned damaged
     * @return response with success message
     */
    @PostMapping("/{id}/confirm-return")
    public ResponseEntity<String> confirmGameReturn(
            @PathVariable int id,
            @RequestParam(required = false, defaultValue = "false") boolean isDamaged) {
        
        if (isDamaged) {
            // In a real implementation, you might track damage information
            // For now, we'll just close the record
            return lendingRecordService.closeLendingRecord(id);
        } else {
            // Close the lending record normally
            return lendingRecordService.closeLendingRecord(id);
        }
    }

    /**
     * Reports a dispute regarding a game return.
     * Implements Use Case 13: Return Borrowed Game (Alternative Scenario 7a and 10a)
     *
     * @param id the ID of the lending record
     * @param disputeDetails details about the dispute
     * @return response with success message
     */
    @PostMapping("/{id}/dispute-return")
    public ResponseEntity<String> disputeGameReturn(
            @PathVariable int id,
            @RequestBody Map<String, String> disputeDetails) {
        
        // In a real implementation, you would store the dispute details
        // For now, we'll just keep the status as ACTIVE to indicate unresolved
        return lendingRecordService.updateStatus(id, LendingStatus.ACTIVE);
    }

    /**
     * Retrieves all lending records for a borrower.
     * Supports Use Case 13: Return Borrowed Game (view borrowed games)
     *
     * @param borrowerId the ID of the borrower
     * @return list of lending records
     */
    @GetMapping("/borrower/{borrowerId}")
    public ResponseEntity<List<LendingRecord>> getLendingRecordsByBorrower(@PathVariable int borrowerId) {
        // In a real implementation, you would use a service to fetch the Account by ID
        Account borrower = new Account(); // Placeholder - in real app, get from service
        borrower.setId(borrowerId);
        
        List<LendingRecord> records = lendingRecordService.getLendingRecordsByBorrower(borrower);
        return ResponseEntity.ok(records);
    }

    /**
     * Retrieves active lending records for a borrower.
     * Supports Use Case 13: Return Borrowed Game (view active borrows)
     *
     * @param borrowerId the ID of the borrower
     * @return list of active lending records
     */
    @GetMapping("/borrower/{borrowerId}/active")
    public ResponseEntity<List<LendingRecord>> getActiveLendingRecordsByBorrower(@PathVariable int borrowerId) {
        // In a real implementation, you would use a service to fetch the Account by ID
        Account borrower = new Account(); // Placeholder - in real app, get from service
        borrower.setId(borrowerId);
        
        List<LendingRecord> allRecords = lendingRecordService.getLendingRecordsByBorrower(borrower);
        
        // Filter by active status
        List<LendingRecord> activeRecords = allRecords.stream()
                .filter(record -> record.getStatus() == LendingStatus.ACTIVE)
                .toList();
        
        return ResponseEntity.ok(activeRecords);
    }
}
