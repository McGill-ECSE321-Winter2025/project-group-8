package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class BorrowRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Date startDate;

    private Date endDate;

    @Enumerated(EnumType.STRING)
    private BorrowRequestStatus status;

    private Date requestDate;

    // Associations

    @ManyToOne
    private Game requestedGame;

    @ManyToOne
    private Account requester;

    @ManyToOne
    private GameOwner responder;

    // Methods

    public BorrowRequest(Date aStartDate, Date aEndDate, BorrowRequestStatus aStatus, Date aRequestDate, Game aRequestedGame) {
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        requestDate = aRequestDate;
        requestedGame = aRequestedGame;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "status" + ":" + getStatus() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this) ? getStartDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this) ? getEndDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestDate" + "=" + (getRequestDate() != null ? !getRequestDate().equals(this) ? getRequestDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestedGame = " + (getRequestedGame() != null ? Integer.toHexString(System.identityHashCode(getRequestedGame())) : "null");
    }
}