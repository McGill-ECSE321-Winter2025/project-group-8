package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Integer> {
    
    Optional<BorrowRequest> findBorrowRequestById(int id);

    @Query("SELECT br FROM BorrowRequest br " +
           "WHERE br.requestedGame.id = :gameId " +
           "AND br.status = 'APPROVED' " +
           "AND br.startDate < :endDate " +
           "AND br.endDate > :startDate")
    List<BorrowRequest> findOverlappingApprovedRequests(@Param("gameId") int gameId, 
                                                        @Param("startDate") Date startDate, 
                                                        @Param("endDate") Date endDate);
}
