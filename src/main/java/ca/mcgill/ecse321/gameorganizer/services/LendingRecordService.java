package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingStatus;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class LendingRecordService {

    /**
     * Creates a new LendingRecord for a game borrow.
     *
     * @param startDate the start date of the lending period
     * @param endDate the end (due) date of the lending period
     * @param request the borrow request
     * @param owner the owner of the game being lent
     * @return the newly created LendingRecord
     */
    public LendingRecord createLendingRecord(Date startDate, Date endDate, BorrowRequest request, GameOwner owner) {
        return null;
    }

    /**
     * Retrieves a LendingRecord by its unique identifier.
     *
     * @param id the unique identifier of the LendingRecord
     * @return the LendingRecord if found, otherwise null
     */
    public LendingRecord getLendingRecordById(int id) {
        return null;
    }

    /**
     * Retrieves all LendingRecords for a given GameOwner.
     *
     * @param owner the GameOwner whose records are to be retrieved
     * @return a list of LendingRecords for the specified owner
     */
    public List<LendingRecord> getLendingRecordsByOwner(GameOwner owner) {
        return null;
    }

    /**
     * Retrieves all LendingRecords for a given borrower.
     *
     * @param borrower the Account representing the borrower
     * @return a list of LendingRecords for the specified borrower
     */
    public List<LendingRecord> getLendingRecordsByBorrower(Account borrower) {
        return null;
    }

    /**
     * Retrieves LendingRecords within a specified date range.
     *
     * @param startDate the start date of the range
     * @param endDate the end date of the range
     * @return a list of LendingRecords that fall within the specified date range
     */
    public List<LendingRecord> getLendingRecordsByDateRange(Date startDate, Date endDate) {
        return null;
    }

    /**
     * Updates the status of a LendingRecord.
     *
     * @param id the unique identifier of the LendingRecord
     * @param newStatus the new status to be set
     * @return the updated LendingRecord
     */
    public LendingRecord updateStatus(int id, LendingStatus newStatus) {
        return null;
    }

    /**
     * Closes a LendingRecord by marking it as CLOSED.
     *
     * @param id the unique identifier of the LendingRecord
     * @return the closed LendingRecord
     */
    public LendingRecord closeLendingRecord(int id) {
        return null;
    }

    /**
     * Finds and returns all overdue LendingRecords.
     * The system should automatically mark any record as OVERDUE if the due date has passed and it is not closed.
     *
     * @return a list of overdue LendingRecords
     */
    public List<LendingRecord> findOverdueRecords() {
        return null;
    }

    /**
     * Updates the end date (due date) of a LendingRecord, allowing the GameOwner to extend the due date.
     *
     * @param id the unique identifier of the LendingRecord
     * @param newEndDate the new end date for the lending period
     * @return the updated LendingRecord
     */
    public LendingRecord updateEndDate(int id, Date newEndDate) {
        return null;
    }

}
