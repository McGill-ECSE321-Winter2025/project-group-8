package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.mcgill.ecse321.gameorganizer.dto.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException; // Import UnauthedException
// UserContext import removed
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
/**
 * Service for managing borrow requests in the game organizer system.
 * Handles request creation, retrieval, updates, and deletion.
 * 
 * @autor Rayan Baida
 */
@Service
public class BorrowRequestService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowRequestService.class);

    private final BorrowRequestRepository borrowRequestRepository;
    private final GameRepository gameRepository;
    private final AccountRepository accountRepository;
    private final LendingRecordService lendingRecordService; // Added dependency

    // UserContext field removed

    /**
     * Constructs a BorrowRequestService with required repositories.
     * 
     * @param borrowRequestRepository Repository for borrow requests.
     * @param gameRepository Repository for games.
     * @param accountRepository Repository for user accounts.
     */
    // Updated constructor to remove UserContext
    @Autowired
    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository, GameRepository gameRepository, AccountRepository accountRepository, LendingRecordService lendingRecordService) { // Added LendingRecordService
        this.borrowRequestRepository = borrowRequestRepository;
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
        this.lendingRecordService = lendingRecordService; // Initialize LendingRecordService
    }

    /**
     * Creates a new borrow request.
     * 
     * @param requestDTO The DTO containing request details.
     * @return The created borrow request as a DTO.
     * @throws IllegalArgumentException if the game or requester is not found, the owner requests their own game, or dates are invalid.
     */
    @Transactional
    public BorrowRequestDto createBorrowRequest(CreateBorrowRequestDto requestDTO) {
        Optional<Game> gameOpt = gameRepository.findById(requestDTO.getRequestedGameId());
        Optional<Account> requesterOpt = accountRepository.findById(requestDTO.getRequesterId());

        if (!gameOpt.isPresent()) {
            throw new IllegalArgumentException("Game not found.");
        }
        if (!requesterOpt.isPresent()) {
            throw new IllegalArgumentException("Requester not found.");
        }

        // Validate the dates early to avoid null pointer issues in subsequent checks
        if (!requestDTO.getEndDate().after(requestDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        Game game = gameOpt.get();
        Account requester = requesterOpt.get();

        // Now check if the requester is the owner, if owner is set
        if (game.getOwner() != null && game.getOwner().equals(requester)) {
            throw new IllegalArgumentException("Owners cannot request their own game.");
        }

        List<BorrowRequest> overlappingRequests = borrowRequestRepository.findOverlappingApprovedRequests(
                game.getId(), requestDTO.getStartDate(), requestDTO.getEndDate());

        if (!overlappingRequests.isEmpty()) {
            throw new IllegalArgumentException("Game is unavailable for the requested period.");
        }

        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setRequestedGame(game);
        borrowRequest.setRequester(requester);
        borrowRequest.setStartDate(requestDTO.getStartDate());
        borrowRequest.setEndDate(requestDTO.getEndDate());
        borrowRequest.setStatus(BorrowRequestStatus.PENDING);
        borrowRequest.setRequestDate(new Date());

        BorrowRequest savedRequest = borrowRequestRepository.save(borrowRequest);

        return new BorrowRequestDto(
                savedRequest.getId(),
                savedRequest.getRequester().getId(),
                savedRequest.getRequestedGame().getId(),
                savedRequest.getStartDate(),
                savedRequest.getEndDate(),
                savedRequest.getStatus().name(),
                savedRequest.getRequestDate()
        );
    }

    /**
     * Retrieves a borrow request by its ID.
     * 
     * @param id The borrow request ID.
     * @return The borrow request DTO.
     * @throws IllegalArgumentException if no request is found.
     */
    @Transactional
    public BorrowRequestDto getBorrowRequestById(int id) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

        Integer requesterId = (request.getRequester() != null) ? request.getRequester().getId() : null;
        Integer gameId = (request.getRequestedGame() != null) ? request.getRequestedGame().getId() : null;
        
        return new BorrowRequestDto(
                request.getId(),
                requesterId,
                gameId,
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus().name(),
                request.getRequestDate()
        );
    }

    /**
     * Retrieves all borrow requests.
     * 
     * @return List of all borrow request DTOs.
     */
    @Transactional
    public List<BorrowRequestDto> getAllBorrowRequests() {
        return borrowRequestRepository.findAll().stream()
                .map(request -> {
                    Integer requesterId = (request.getRequester() != null) ? request.getRequester().getId() : null;
                    Integer gameId = (request.getRequestedGame() != null) ? request.getRequestedGame().getId() : null;
                    return new BorrowRequestDto(
                            request.getId(),
                            requesterId,
                            gameId,
                            request.getStartDate(),
                            request.getEndDate(),
                            request.getStatus().name(),
                            request.getRequestDate()
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * Updates the status of a borrow request.
     * 
     * @param id The borrow request ID.
     * @param newStatus The new status (APPROVED or DECLINED).
     * @return The updated borrow request DTO.
     * @throws IllegalArgumentException if the request is not found or status is invalid.
     */
    @Transactional
public BorrowRequestDto updateBorrowRequestStatus(int id, BorrowRequestStatus newStatus) {
    BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
            .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

    // If the status is being set to APPROVED, create a LendingRecord
    if (newStatus == BorrowRequestStatus.APPROVED) {
        Game requestedGame = request.getRequestedGame();
        if (requestedGame == null) {
            throw new IllegalStateException("Cannot approve request: Game details are missing.");
        }
        GameOwner owner = requestedGame.getOwner();
        if (owner == null) {
            // This case might indicate an orphaned game or configuration issue.
            throw new IllegalStateException("Cannot approve request: Game owner is not set.");
        }

        /* 
        try {
            // Call LendingRecordService to create the record
            ResponseEntity<String> response = lendingRecordService.createLendingRecord(
                    request.getStartDate(),
                    request.getEndDate(),
                    request,
                    owner
            );

            // Check if the lending record creation was successful
            if (response.getStatusCode() != HttpStatus.OK) {
                // Log the error and throw an exception to indicate failure during the transaction
                String errorMessage = String.format("Failed to create lending record for approved borrow request %d. Status: %s, Body: %s",
                                                   request.getId(), response.getStatusCode(), response.getBody());
                logger.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            logger.info("Successfully created lending record for approved borrow request {}", request.getId());

        } catch (Exception e) {
            // Catch potential exceptions from createLendingRecord (e.g., validation errors)
            String errorMessage = String.format("Error creating lending record for approved borrow request %d: %s",
                                               request.getId(), e.getMessage());
            logger.error(errorMessage, e);
            // Re-throw as a runtime exception to ensure transaction rollback if needed
            throw new RuntimeException(errorMessage, e);
        }*/
    }

    // Update the status of the BorrowRequest
    request.setStatus(newStatus);
    BorrowRequest updatedRequest = borrowRequestRepository.save(request);

    // Prepare and return the DTO
    Integer requesterId = (updatedRequest.getRequester() != null) ? updatedRequest.getRequester().getId() : null; // Keep null if missing
    Integer gameId = (updatedRequest.getRequestedGame() != null) ? updatedRequest.getRequestedGame().getId() : null; // Keep null if missing

    return new BorrowRequestDto(
            updatedRequest.getId(),
            requesterId,
            gameId,
            updatedRequest.getStartDate(),
            updatedRequest.getEndDate(),
            updatedRequest.getStatus().name(),
            updatedRequest.getRequestDate()
    );
}

    /**
     * Deletes a borrow request by its ID.
     * 
     * @param id The borrow request ID.
     * @throws IllegalArgumentException if no request is found.
     */
    @Transactional
    public void deleteBorrowRequest(int id) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

        // Authorization Check: Only the requester can delete their own request using Spring Security
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            throw new UnauthedException("User must be authenticated to delete a borrow request.");
        }

        String currentUsername;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            currentUsername = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            currentUsername = (String) principal;
        } else {
            throw new UnauthedException("Unexpected principal type in SecurityContext.");
        }

        Account currentUser = accountRepository.findByEmail(currentUsername).orElseThrow(
                () -> new UnauthedException("Authenticated user '" + currentUsername + "' not found in database.")
        );

        if (request.getRequester() == null || request.getRequester().getId() != currentUser.getId()) {
            throw new UnauthedException("Access denied: You can only delete your own borrow requests.");
        }

        borrowRequestRepository.delete(request);
    }

    /**
     * Finds all borrow requests associated with a specific game owner by their ID.
     *
     * @param ownerId The ID of the game owner
     * @return List of borrow request DTOs associated with the specified game owner
     */
    @Transactional
    public List<BorrowRequestDto> getBorrowRequestsByOwnerId(int ownerId) {
        List<BorrowRequest> borrowRequests = borrowRequestRepository.findBorrowRequestsByOwnerId(ownerId);

        return borrowRequests.stream()
                .map(request -> {
                    Integer requesterId = (request.getRequester() != null) ? request.getRequester().getId() : null;
                    Integer gameId = (request.getRequestedGame() != null) ? request.getRequestedGame().getId() : null;
                    return new BorrowRequestDto(
                            request.getId(),
                            requesterId,
                            gameId,
                            request.getStartDate(),
                            request.getEndDate(),
                            request.getStatus().name(),
                            request.getRequestDate()
                    );
                })
                .collect(Collectors.toList());
    }
}
