package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import ca.mcgill.ecse321.gameorganizer.models.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final BorrowRequestRepository borrowRequestRepository;
    private final GameRepository gameRepository;
    private final AccountRepository accountRepository;

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
    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository, GameRepository gameRepository, AccountRepository accountRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
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
     * Retrieves all borrow requests for a game owner.
     * @param gameOwnerId the game owner's id.
     * @return List of all borrow request DTOs.
     */
    @Transactional
    public List<BorrowRequestDto> getBorrowRequestsByGameOwner(int gameOwnerId) {
        Account gameOwner = accountRepository.findById(gameOwnerId)
                .orElseThrow(() -> new EntityNotFoundException("Game owner not found with ID: " + gameOwnerId));

        if (!(gameOwner instanceof GameOwner)) {
            throw new IllegalArgumentException(
                    String.format("Account with ID %d is of type %s, expected GameOwner",
                            gameOwnerId, gameOwner.getClass().getSimpleName())
            );
        }

        List<BorrowRequest> requests = borrowRequestRepository.findByResponder(gameOwner);
        if (requests == null) {
            return Collections.emptyList();
        }

        return requests.stream()
                .map(BorrowRequestDto::new)
                .toList();
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
public BorrowRequestDto updateBorrowRequestStatus(int id, String newStatus) {
    BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
            .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

    // Authorization Check: Only the game owner can approve/decline using Spring Security
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
        throw new UnauthedException("User must be authenticated to update borrow request status.");
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

    if (request.getRequestedGame() == null || request.getRequestedGame().getOwner() == null ||
        request.getRequestedGame().getOwner().getId() != currentUser.getId()) {
        throw new UnauthedException("Access denied: Only the game owner can approve or decline requests.");
    }

    if (!newStatus.equals("APPROVED") && !newStatus.equals("DECLINED")) {
        throw new IllegalArgumentException("Invalid status.");
    }

    request.setStatus(BorrowRequestStatus.valueOf(newStatus));
    BorrowRequest updatedRequest = borrowRequestRepository.save(request);

    // If requester or game is missing, substitute a default value (e.g., 0) instead of null.
    Integer requesterId = (updatedRequest.getRequester() != null) ? updatedRequest.getRequester().getId() : 0;
    Integer gameId = (updatedRequest.getRequestedGame() != null) ? updatedRequest.getRequestedGame().getId() : 0;

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
}
