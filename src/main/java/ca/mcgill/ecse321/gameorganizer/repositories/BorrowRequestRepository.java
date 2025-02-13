package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Integer> {
    Optional<BorrowRequest> findBorrowRequestById(int id);
    
    List<BorrowRequest> findOverlappingApprovedRequests(@Param("gameId") int gameId, 
                                                        @Param("startDate") Date startDate, 
                                                        @Param("endDate") Date endDate);
}

