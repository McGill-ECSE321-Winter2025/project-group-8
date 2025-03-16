package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.Date;

public class BorrowRequestDto {
    private int id;
    private int requesterId;
    private int requestedGameId;
    private Date startDate;
    private Date endDate;
    private String status;
    private Date requestDate;

    // Constructor
    public BorrowRequestDto(int id, int requesterId, int requestedGameId, Date startDate, Date endDate, String status, Date requestDate) {
        this.id = id;
        this.requesterId = requesterId;
        this.requestedGameId = requestedGameId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.requestDate = requestDate;
    }

    // Getters
    public int getId() { return id; }
    public int getRequesterId() { return requesterId; }
    public int getRequestedGameId() { return requestedGameId; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public String getStatus() { return status; }
    public Date getRequestDate() { return requestDate; }
}
