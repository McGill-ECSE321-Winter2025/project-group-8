package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LendingRecordRepository extends JpaRepository<LendingRecord, Integer> {
    Optional<LendingRecord> findLendingRecordById(int id);
}
