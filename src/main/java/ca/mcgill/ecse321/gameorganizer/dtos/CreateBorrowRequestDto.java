package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.Date;

public class CreateBorrowRequestDto {
    private int requesterId;
    private int requestedGameId;
    private Date startDate;
    private Date endDate;

    // Constructor
    public CreateBorrowRequestDto(int requesterId, int requestedGameId, Date startDate, Date endDate) {
        this.requesterId = requesterId;
        this.requestedGameId = requestedGameId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters
    public int getRequesterId() { return requesterId; }
    public int getRequestedGameId() { return requestedGameId; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
}
