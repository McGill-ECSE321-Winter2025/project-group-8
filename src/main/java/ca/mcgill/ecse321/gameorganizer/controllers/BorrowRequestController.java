package ca.mcgill.ecse321.gameorganizer.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.dto.request.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.request.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.exceptions.ForbiddenException; // Import
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException; // Import

/**
 * Controller for managing borrow requests.
 * Handles creating, retrieving, updating, and deleting borrow requests.
 * 
 * @author Rayan Baida
 */
@RestController
@RequestMapping("/api/borrowrequests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;
    private final BorrowRequestRepository borrowRequestRepository;

    /**
     * Constructor to inject the BorrowRequestService and BorrowRequestRepository.
     *
     * @param borrowRequestService Service handling borrow request logic.
     * @param borrowRequestRepository Repository handling borrow request data.
     */
    @Autowired
    public BorrowRequestController(BorrowRequestService borrowRequestService, BorrowRequestRepository borrowRequestRepository) {
        this.borrowRequestService = borrowRequestService;
        this.borrowRequestRepository = borrowRequestRepository;
    }

    /**
     * Creates a new borrow request.
     *
     * @param dto Data transfer object containing request details.
     * @return The created borrow request.
     */
    @PostMapping
    public ResponseEntity<BorrowRequestDto> createBorrowRequest(@RequestBody CreateBorrowRequestDto dto) {
        System.out.println("Received Borrow Request: " + dto);
        try {
            return ResponseEntity.ok(borrowRequestService.createBorrowRequest(dto));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * Retrieves a borrow request by its ID.
     *
     * @param id The ID of the borrow request.
     * @return The borrow request if found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestDto> getBorrowRequestById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(borrowRequestService.getBorrowRequestById(id));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrow request with ID " + id + " not found.");
        }
    }

    /**
     * Retrieves all borrow requests.
     *
     * @return A list of all borrow requests.
     */
    @GetMapping
    public ResponseEntity<List<BorrowRequestDto>> getAllBorrowRequests() {
        return ResponseEntity.ok(borrowRequestService.getAllBorrowRequests());
    }

    /**
     * Updates the status of a borrow request.
     *
     * @param id The ID of the borrow request to update.
     * @param requestDto The updated borrow request details.
     * @return The updated borrow request.
     */
    @PutMapping("/{id}")
    public ResponseEntity<BorrowRequestDto> updateBorrowRequestStatus(
            @PathVariable int id,
            @RequestBody BorrowRequestDto requestDto) {
        try {
            BorrowRequestStatus status;
            try {
                status = BorrowRequestStatus.valueOf(requestDto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status value: " + requestDto.getStatus());
            }
           return ResponseEntity.ok(borrowRequestService.updateBorrowRequestStatus(id, status));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrow request with ID " + id + " not found.");
        }
    }

    /**
     * Deletes a borrow request by its ID.
     *
     * @param id The ID of the borrow request to delete.
     * @return HTTP 200 response if successful.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBorrowRequest(@PathVariable int id) {
        try {
            // Removed test environment special handling
            // Normal flow: call service, which now handles auth
            borrowRequestService.deleteBorrowRequest(id);
            return ResponseEntity.noContent().build(); // Return 204 No Content on success
        } catch (IllegalArgumentException e) {
             // Let GlobalExceptionHandler handle this (typically 404 or 400)
             // Consider logging e.getMessage()
             throw e;
        } catch (ForbiddenException e) {
             // Let GlobalExceptionHandler handle this (typically 403)
             // Consider logging e.getMessage()
             throw e;
        } catch (UnauthedException e) {
             // Let GlobalExceptionHandler handle this (typically 401)
             // Consider logging e.getMessage()
             throw e;
        }
        // Other potential exceptions will also be caught by GlobalExceptionHandler
    }

    /**
     * Retrieve borrow requests filtered by status.
     *
     * @param status The status to filter by (e.g., "PENDING", "APPROVED", etc.).
     * @return A list of borrow requests with the specified status.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BorrowRequestDto>> getBorrowRequestsByStatus(@PathVariable String status) {
        List<BorrowRequestDto> filteredRequests = borrowRequestService.getAllBorrowRequests().stream()
                .filter(request -> request.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredRequests);
    }

    /**
     * Retrieve all borrow requests for a particular requester.
     * 
     * @param requesterId The ID of the user who initiated the borrow request.
     * @return A list of borrow requests for the specified requester.
     */
    @GetMapping("/requester/{requesterId}")
    public ResponseEntity<List<BorrowRequestDto>> getBorrowRequestsByRequester(@PathVariable int requesterId) {
        List<BorrowRequestDto> filteredRequests = borrowRequestService.getAllBorrowRequests().stream()
                .filter(request -> request.getRequesterId() == requesterId)
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredRequests);
    }
    
    /**
     * Retrieve all borrow requests for a particular game owner.
     * 
     * @param gameOwnerId The ID of the game owner.
     * @return A list of borrow requests for the games owned by the specified game owner.
     */
    @GetMapping("/gameOwner/{gameOwnerId}")
    public ResponseEntity<List<BorrowRequestDto>> getBorrowRequestsByGameOwner(@PathVariable int gameOwnerId) {
        try {
            // For now, return an empty list until proper implementation
            List<BorrowRequestDto> ownerRequests = borrowRequestService.getAllBorrowRequests().stream()
                    .filter(request -> {
                        // Here you would filter based on the games owned by the game owner
                        // This is a placeholder implementation
                        return true; // Return all requests for now
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ownerRequests);
        } catch (Exception e) {
            // Log the error
            System.err.println("Error retrieving borrow requests for game owner: " + e.getMessage());
            // Return an empty list rather than an error to prevent frontend errors
            return ResponseEntity.ok(List.of());
        }
    }
}