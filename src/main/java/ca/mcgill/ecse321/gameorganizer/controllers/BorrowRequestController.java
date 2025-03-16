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

@RestController
@RequestMapping("/borrowrequests")
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    @Autowired
    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @PostMapping
    public ResponseEntity<BorrowRequestDto> createBorrowRequest(@RequestBody CreateBorrowRequestDto dto) {
        System.out.println("Received Borrow Request: " + dto);
        try {
            return ResponseEntity.ok(borrowRequestService.createBorrowRequest(dto));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowRequestDto> getBorrowRequestById(@PathVariable int id) {
        try {
            return ResponseEntity.ok(borrowRequestService.getBorrowRequestById(id));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Borrow request with ID " + id + " not found.");
        }
    }

    @GetMapping
    public ResponseEntity<List<BorrowRequestDto>> getAllBorrowRequests() {
        return ResponseEntity.ok(borrowRequestService.getAllBorrowRequests());
    }

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
