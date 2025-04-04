package ca.mcgill.ecse321.gameorganizer.dto;

import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;

import java.util.Date;

/**
 * Represents a borrow request for a game.
 * Stores details about the request, including the requester, game, and status.
 * 
 * @author Rayan Baida
 */
public class BorrowRequestDto {
    private int id;
    private int requesterId;
    private int requestedGameId;
    private Date startDate;
    private Date endDate;
    private String status;
    private Date requestDate;

    /**
     * Constructs a new borrow request DTO.
     * 
     * @param id Unique ID of the request.
     * @param requesterId ID of the user making the request.
     * @param requestedGameId ID of the game being requested.
     * @param startDate Start date of the borrow period.
     * @param endDate End date of the borrow period.
     * @param status Current status of the request (e.g., pending, approved, declined).
     * @param requestDate Date when the request was made.
     */
    public BorrowRequestDto(int id, int requesterId, int requestedGameId, Date startDate, Date endDate, String status, Date requestDate) {
        this.id = id;
        this.requesterId = requesterId;
        this.requestedGameId = requestedGameId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.requestDate = requestDate;
    }

    public BorrowRequestDto(BorrowRequest request) {
        this.id = request.getId();
        this.requesterId = request.getRequester().getId();
        this.requestedGameId = request.getRequestedGame().getId();
        this.startDate = request.getStartDate();
        this.endDate = request.getEndDate();
        this.requestDate = request.getRequestDate();
    }

    /** @return The unique ID of the borrow request. */
    public int getId() { return id; }
    
    /** @return The ID of the requester. */
    public int getRequesterId() { return requesterId; }
    
    /** @return The ID of the requested game. */
    public int getRequestedGameId() { return requestedGameId; }
    
    /** @return The start date of the borrow period. */
    public Date getStartDate() { return startDate; }
    
    /** @return The end date of the borrow period. */
    public Date getEndDate() { return endDate; }
    
    /** @return The status of the borrow request. */
    public String getStatus() { return status; }
    
    /** @return The date when the request was made. */
    public Date getRequestDate() { return requestDate; }
}