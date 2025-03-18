package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.Date;

/**
 * Repository interface for managing LendingRecord entities.
 * Provides methods for CRUD operations and custom queries related to lending records.
 * 
 * @author @YoussGm3o8
 */
@Repository
public interface LendingRecordRepository extends JpaRepository<LendingRecord, Integer> {
    
    /**
     * Finds a lending record by its unique identifier.
     *
     * @param id the ID of the lending record
     * @return an Optional containing the found record or empty if not found
     */
    Optional<LendingRecord> findLendingRecordById(int id);

    /**
     * Retrieves all lending records with a specific status.
     *
     * @param status the status to search for
     * @return list of lending records matching the status
     */
    List<LendingRecord> findByStatus(LendingStatus status);

    /**
     * Finds all lending records associated with a specific game owner.
     *
     * @param owner the game owner to search for
     * @return list of lending records for the specified owner
     */
    List<LendingRecord> findByRecordOwner(GameOwner owner);

    /**
     * Finds all lending records with start dates within a specified range.
     *
     * @param startDate the beginning of the date range
     * @param endDate the end of the date range
     * @return list of lending records within the date range
     */
    List<LendingRecord> findByStartDateBetween(Date startDate, Date endDate);

    /**
     * Finds all lending records that have passed their end date and have a specific status.
     *
     * @param date the date to compare against end dates
     * @param status the status to filter by
     * @return list of lending records matching the criteria
     */
    List<LendingRecord> findByEndDateBeforeAndStatus(Date date, LendingStatus status);

    /**
     * Finds all lending records associated with a specific borrower.
     * Changed from findByRequest_Borrower to findByRequest_Requester to match BorrowRequest model
     *
     * @param requester the borrower account to search for
     * @return list of lending records for the specified borrower
     */
    List<LendingRecord> findByRequest_Requester(Account requester);


}
