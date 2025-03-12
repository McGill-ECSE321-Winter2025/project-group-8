package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing BorrowRequest entities.
 * Provides CRUD operations and custom queries for game borrowing requests.
 * Extends JpaRepository to inherit basic database operations.
 * 
 * @author @rayanBaida
 */
@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Integer> {
    
    /**
     * Finds a borrow request by its unique identifier.
     *
     * @param id The ID of the borrow request to find
     * @return Optional containing the borrow request if found, empty Optional otherwise
     */
    Optional<BorrowRequest> findBorrowRequestById(int id);

    /**
     * Finds all approved borrow requests that overlap with a given date range for a specific game.
     * This is used to check if a game is available for a new borrow request.
     *
     * @param gameId The ID of the game to check
     * @param startDate The start date of the period to check
     * @param endDate The end date of the period to check
     * @return List of overlapping approved borrow requests
     */
    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE br.requestedGame.id = :gameId " +
           "AND br.status = 'APPROVED' " +
           "AND br.startDate < :endDate " +
           "AND br.endDate > :startDate")
    List<BorrowRequest> findOverlappingApprovedRequests(@Param("gameId") int gameId, 
                                                        @Param("startDate") Date startDate, 
                                                        @Param("endDate") Date endDate);
}
