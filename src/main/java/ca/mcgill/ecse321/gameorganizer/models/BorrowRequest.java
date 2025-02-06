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

    private String status;

    private Date requestDate;

    @OneToOne
    private Account requestedBy;

    @ManyToOne
    private GameOwner managedBy;

    @ManyToOne
    private Game requestedGame;

    public BorrowRequest(Date aStartDate, Date aEndDate, String aStatus, Date aRequestDate, Account aRequestedBy, GameOwner aManagedBy, Game aRequestedGame) {
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        requestDate = aRequestDate;
        requestedBy = aRequestedBy;
        managedBy = aManagedBy;
        requestedGame = aRequestedGame;
    }

    public void delete() {
        requestedBy = null;
        managedBy = null;
        requestedGame = null;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "status" + ":" + getStatus() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this) ? getStartDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this) ? getEndDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestDate" + "=" + (getRequestDate() != null ? !getRequestDate().equals(this) ? getRequestDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestedBy = " + (getRequestedBy() != null ? Integer.toHexString(System.identityHashCode(getRequestedBy())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "managedBy = " + (getManagedBy() != null ? Integer.toHexString(System.identityHashCode(getManagedBy())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "requestedGame = " + (getRequestedGame() != null ? Integer.toHexString(System.identityHashCode(getRequestedGame())) : "null");
    }
}