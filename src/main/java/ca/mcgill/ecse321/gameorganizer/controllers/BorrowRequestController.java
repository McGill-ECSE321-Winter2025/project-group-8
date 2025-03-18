package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.dtos.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dtos.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for managing borrow requests.
 * Handles creating, retrieving, updating, and deleting borrow requests.
 *
 * @author Rayan Baida
 */
@RestController
@RequestMapping("/borrowrequests")
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
}