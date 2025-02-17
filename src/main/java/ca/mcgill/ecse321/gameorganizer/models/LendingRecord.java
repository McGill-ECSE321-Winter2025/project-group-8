package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Represents a record of a game lending transaction between a game owner and a borrower.
 * This entity tracks the lending period, status, and associated request details.
 */
@Entity
@NoArgsConstructor
@Getter
@Setter
public class LendingRecord {

    /**
     * Enumeration of possible lending record statuses.
     * ACTIVE: The lending is currently ongoing
     * OVERDUE: The lending period has expired but the game hasn't been returned
     * CLOSED: The lending transaction has been completed
     */
    public enum LendingStatus {
        ACTIVE, OVERDUE, CLOSED
    }

    /** Unique identifier for the lending record */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    /** The date when the lending period begins */
    private Date startDate;

    /** The date when the game is expected to be returned */
    private Date endDate;

    /** Current status of the lending record */
    @Enumerated(EnumType.STRING)
    private LendingStatus status;

    /** The owner who is lending the game */
    @ManyToOne
    private GameOwner recordOwner;

    /** The associated borrow request that initiated this lending */
    @OneToOne
    private BorrowRequest request;

    /**
     * Creates a new lending record with the specified details.
     *
     * @param aStartDate The start date of the lending period
     * @param aEndDate The end date of the lending period
     * @param aStatus The initial status of the lending
     * @param aRequest The associated borrow request
     * @param aOwner The game owner
     * @throws IllegalArgumentException if any parameter is null or if dates are invalid
     */
    public LendingRecord(Date aStartDate, Date aEndDate, LendingStatus aStatus, BorrowRequest aRequest, GameOwner aOwner) {
        if (aStartDate == null || aEndDate == null || aStatus == null || aRequest == null || aOwner == null) {
            throw new IllegalArgumentException("Required fields cannot be null");
        }
        if (aEndDate.before(aStartDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        if (!aRequest.getRequestedGame().getOwner().equals(aOwner)) {
            throw new IllegalArgumentException("The record owner must be the owner of the game in the borrow request");
        }
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        request = aRequest;
        recordOwner = aOwner;
    }

    /**
     * Calculates the duration of the lending period in days.
     *
     * @return the number of days between start and end date
     */
    public long getDurationInDays() {
        return (endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24);
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "status" + ":" + getStatus() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this) ? getStartDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this) ? getEndDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "request = " + (getRequest() != null ? Integer.toHexString(System.identityHashCode(getRequest())) : "null") + System.getProperties().getProperty("line.separator");
    }
}

