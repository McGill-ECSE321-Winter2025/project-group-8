package ca.mcgill.ecse321.gameorganizer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Integer> {
    Optional<BorrowRequest> findBorrowRequestById(int id);
}