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

import ca.mcgill.ecse321.gameorganizer.dto.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.middleware.RequireUser;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;

/**
 * Controller for managing borrow requests.
 * Handles creating, retrieving, updating, and deleting borrow requests.
 * 
 * @author Rayan Baida
 */
@RestController
@RequestMapping("/api/v1/borrowrequests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    /**
     * Constructor to inject the BorrowRequestService.
     *
     * @param borrowRequestService Service handling borrow request logic.
     */
    @Autowired
    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    /**
     * Creates a new borrow request.
     *
     * @param dto Data transfer object containing request details.
     * @return The created borrow request.
     */
    @RequireUser
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
            return ResponseEntity.ok(borrowRequestService.updateBorrowRequestStatus(id, requestDto.getStatus()));
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
            borrowRequestService.deleteBorrowRequest(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrow request with ID " + id + " not found.");
        }
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

}