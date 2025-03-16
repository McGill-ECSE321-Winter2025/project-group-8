package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.dtos.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dtos.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.dtos.LendingRecordDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BorrowRequestService {

    private final BorrowRequestRepository borrowRequestRepository;
    private final GameRepository gameRepository;
    private final AccountRepository accountRepository;
    @Autowired
    private LendingRecordRepository lendingRecordRepository;

    @Autowired
    public BorrowRequestService(BorrowRequestRepository borrowRequestRepository, GameRepository gameRepository, AccountRepository accountRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.gameRepository = gameRepository;
        this.accountRepository = accountRepository;
    }

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
    
        Game game = gameOpt.get();
        Account requester = requesterOpt.get();
    
        if (game.getOwner().equals(requester)) {
            throw new IllegalArgumentException("Owners cannot request their own game.");
        }
    
        // ✅ **NEW: Validate dates**
        if (!requestDTO.getEndDate().after(requestDTO.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date.");
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
    

    @Transactional
    public BorrowRequestDto getBorrowRequestById(int id) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

        return new BorrowRequestDto(
                request.getId(),
                request.getRequester().getId(),
                request.getRequestedGame().getId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStatus().name(),
                request.getRequestDate()
        );
    }

    @Transactional
    public List<BorrowRequestDto> getAllBorrowRequests() {
        return borrowRequestRepository.findAll().stream()
                .map(request -> new BorrowRequestDto(
                        request.getId(),
                        request.getRequester().getId(),
                        request.getRequestedGame().getId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus().name(),
                        request.getRequestDate()
                ))
                .collect(Collectors.toList());
    }

    @Transactional
    public BorrowRequestDto updateBorrowRequestStatus(int id, String newStatus) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));

        if (!newStatus.equals("APPROVED") && !newStatus.equals("DECLINED")) {
            throw new IllegalArgumentException("Invalid status.");
        }

        request.setStatus(BorrowRequestStatus.valueOf(newStatus));
        BorrowRequest updatedRequest = borrowRequestRepository.save(request);

        return new BorrowRequestDto(
                updatedRequest.getId(),
                updatedRequest.getRequester().getId(),
                updatedRequest.getRequestedGame().getId(),
                updatedRequest.getStartDate(),
                updatedRequest.getEndDate(),
                updatedRequest.getStatus().name(),
                updatedRequest.getRequestDate()
        );
    }

    @Transactional
    public void deleteBorrowRequest(int id) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(id)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + id));
        borrowRequestRepository.delete(request);
    }

    /**
     * Accepts a borrow request by changing its status to APPROVED and creates a corresponding lending record.
     * This implements the core functionality for Use Case: Accept Borrow Request.
     *
     * @param requestId The ID of the borrow request to accept
     * @return A DTO representing the accepted borrow request
     * @throws IllegalArgumentException if the request doesn't exist or has already been processed
     * @throws IllegalStateException if there are overlapping approved requests
     */
    @Transactional
    public BorrowRequestDto acceptBorrowRequest(int requestId) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + requestId));
        
        // Validate request status
        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only accept PENDING requests.");
        }
        
        // Check for overlapping requests
        if (!checkForOverlaps(request.getRequestedGame().getId(), request.getStartDate(), request.getEndDate(), requestId)) {
            throw new IllegalStateException("Cannot approve request due to overlapping approved requests.");
        }
        
        // Update request status
        request.setStatus(BorrowRequestStatus.APPROVED);
        BorrowRequest updatedRequest = borrowRequestRepository.save(request);
        
        // Create lending record
        GameOwner gameOwner = (GameOwner) request.getRequestedGame().getOwner();
        LendingRecord lendingRecord = new LendingRecord(
                request.getStartDate(),
                request.getEndDate(),
                LendingStatus.ACTIVE,
                request,
                gameOwner
        );
        lendingRecordRepository.save(lendingRecord);
        
        return new BorrowRequestDto(
                updatedRequest.getId(),
                updatedRequest.getRequester().getId(),
                updatedRequest.getRequestedGame().getId(),
                updatedRequest.getStartDate(),
                updatedRequest.getEndDate(),
                updatedRequest.getStatus().name(),
                updatedRequest.getRequestDate()
        );
    }
    
    /**
     * Rejects a borrow request by changing its status to DECLINED.
     * This implements the core functionality for Use Case: Deny Borrow Request.
     *
     * @param requestId The ID of the borrow request to reject
     * @return A DTO representing the rejected borrow request
     * @throws IllegalArgumentException if the request doesn't exist or has already been processed
     */
    @Transactional
    public BorrowRequestDto denyBorrowRequest(int requestId) {
        BorrowRequest request = borrowRequestRepository.findBorrowRequestById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("No borrow request found with ID " + requestId));
        
        // Validate request status
        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new IllegalArgumentException("Can only deny PENDING requests.");
        }
        
        // Update request status
        request.setStatus(BorrowRequestStatus.DECLINED);
        BorrowRequest updatedRequest = borrowRequestRepository.save(request);
        
        return new BorrowRequestDto(
                updatedRequest.getId(),
                updatedRequest.getRequester().getId(),
                updatedRequest.getRequestedGame().getId(),
                updatedRequest.getStartDate(),
                updatedRequest.getEndDate(),
                updatedRequest.getStatus().name(),
                updatedRequest.getRequestDate()
        );
    }
    
    /**
     * Checks if there are any overlapping approved borrow requests for a game during a specified period.
     * Used to validate before approving a new borrow request.
     *
     * @param gameId The ID of the game to check
     * @param startDate The start date of the period to check
     * @param endDate The end date of the period to check
     * @param excludeRequestId Optional ID of a request to exclude from the check (e.g., when updating)
     * @return true if no overlaps exist, false otherwise
     */
    @Transactional
    public boolean checkForOverlaps(int gameId, Date startDate, Date endDate, int... excludeRequestId) {
        List<BorrowRequest> overlappingRequests = borrowRequestRepository.findOverlappingApprovedRequests(
                gameId, startDate, endDate);
        
        // If excludeRequestId is provided, filter it out
        if (excludeRequestId.length > 0) {
            int idToExclude = excludeRequestId[0];
            overlappingRequests = overlappingRequests.stream()
                    .filter(request -> request.getId() != idToExclude)
                    .collect(Collectors.toList());
        }
        
        return overlappingRequests.isEmpty();
    }
    
    /**
     * Retrieves all pending borrow requests for games owned by a specific game owner.
     * Implements the core functionality for Use Case: View Pending Requests for Owner.
     *
     * @param ownerId The ID of the game owner
     * @return A list of DTOs representing pending borrow requests
     * @throws IllegalArgumentException if the owner doesn't exist
     */
    @Transactional
    public List<BorrowRequestDto> getAllPendingRequestsForOwner(int ownerId) {
        Optional<Account> ownerOpt = accountRepository.findById(ownerId);
        if (!ownerOpt.isPresent() || !(ownerOpt.get() instanceof GameOwner)) {
            throw new IllegalArgumentException("No game owner found with ID " + ownerId);
        }
        
        GameOwner owner = (GameOwner) ownerOpt.get();
        List<BorrowRequest> pendingRequests = borrowRequestRepository.findByRequestedGame_OwnerAndStatus(
                owner, BorrowRequestStatus.PENDING);
        
        return pendingRequests.stream()
                .map(request -> new BorrowRequestDto(
                        request.getId(),
                        request.getRequester().getId(),
                        request.getRequestedGame().getId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus().name(),
                        request.getRequestDate()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Retrieves all borrow requests made by a specific user.
     * Implements the core functionality for Use Case: View User's Borrow Requests.
     *
     * @param userId The ID of the user
     * @return A list of DTOs representing the user's borrow requests
     * @throws IllegalArgumentException if the user doesn't exist
     */
    @Transactional
    public List<BorrowRequestDto> getBorrowRequestsByUser(int userId) {
        Account user = accountRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user found with ID " + userId));
        
        List<BorrowRequest> userRequests = borrowRequestRepository.findByRequester(user);
        
        return userRequests.stream()
                .map(request -> new BorrowRequestDto(
                        request.getId(),
                        request.getRequester().getId(),
                        request.getRequestedGame().getId(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getStatus().name(),
                        request.getRequestDate()
                ))
                .collect(Collectors.toList());
    }
}
