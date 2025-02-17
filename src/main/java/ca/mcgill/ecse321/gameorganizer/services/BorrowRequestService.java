package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;

    @Autowired
    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
    }

    /**
     * Creates a borrow request if valid (game available, valid dates, etc.).
     * 
     * @param newBorrowRequest The request details.
     * @return Response with success message.
     */
    @Transactional
    public ResponseEntity<String> createBorrowRequest(BorrowRequest newBorrowRequest) {
        // Validate required fields
        if (newBorrowRequest.getRequestedGame() == null) {
            throw new IllegalArgumentException("Borrow request must include a requested game.");
        }
        if (newBorrowRequest.getRequester() == null) {
            throw new IllegalArgumentException("Borrow request must include a requester.");
        }
        if (newBorrowRequest.getStartDate() == null || newBorrowRequest.getEndDate() == null) {
            throw new IllegalArgumentException("Both start and end dates are required.");
        }
        // Ensure end date is after start date
        if (!newBorrowRequest.getEndDate().after(newBorrowRequest.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        // Prevent owners from borrowing their own game
        Game requestedGame = newBorrowRequest.getRequestedGame();
        Account requester = newBorrowRequest.getRequester();
        if (requestedGame.getOwner() != null && requestedGame.getOwner().equals(requester)) {
            throw new IllegalArgumentException("Owners cannot request their own game.");
        }

        // Check if game is available
        List<BorrowRequest> overlappingRequests = borrowRequestRepository.findOverlappingApprovedRequests(
            requestedGame.getId(), newBorrowRequest.getStartDate(), newBorrowRequest.getEndDate());
        if (!overlappingRequests.isEmpty()) {
            throw new IllegalArgumentException("Game is unavailable for the requested period.");
        }

        // Set status and request date
        newBorrowRequest.setStatus(BorrowRequestStatus.PENDING);
        newBorrowRequest.setRequestDate(new Date());
        borrowRequestRepository.save(newBorrowRequest);
        return ResponseEntity.ok("Borrow request created successfully.");
    }

    /**
     * Retrieves a borrow request by ID.
     * 
     * @param id The request ID.
     * @return The BorrowRequest.
     */
    @Transactional
    public BorrowRequest getBorrowRequestById(int id) {
        return borrowRequestRepository.findBorrowRequestById(id).orElseThrow(
            () -> new IllegalArgumentException("No borrow request found with ID " + id)
        );
    }

    /**
     * Updates the status of a borrow request.
     * 
     * @param id The request ID.
     * @param newStatus The new status ("APPROVED" or "DECLINED").
     * @return Response with success message.
     */
    @Transactional
    public ResponseEntity<String> updateBorrowRequestStatus(int id, String newStatus) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id).orElseThrow(
            () -> new IllegalArgumentException("No borrow request found with ID " + id)
        );
        if (!newStatus.equals("APPROVED") && !newStatus.equals("DECLINED")) {
            throw new IllegalArgumentException("Invalid status, it must be either APPROVED or DECLINED.");
        }
        request.setStatus(BorrowRequestStatus.valueOf(newStatus));
        borrowRequestRepository.save(request);
        return ResponseEntity.ok("Borrow request status updated successfully.");
    }

    /**
     * Deletes a borrow request.
     * 
     * @param id The request ID.
     * @return Response with success message.
     */
    @Transactional
    public ResponseEntity<String> deleteBorrowRequest(int id) {
        BorrowRequest requestToDelete = borrowRequestRepository.findBorrowRequestById(id).orElseThrow(
            () -> new IllegalArgumentException("No borrow request found with ID " + id)
        );
        borrowRequestRepository.delete(requestToDelete);
        return ResponseEntity.ok("Borrow request with id " + id + " has been deleted.");
    }
}
