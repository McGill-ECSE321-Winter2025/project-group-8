package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.dtos.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dtos.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
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
}
