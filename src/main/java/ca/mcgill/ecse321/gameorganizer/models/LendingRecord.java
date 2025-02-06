package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class LendingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private Date startDate;

    private Date endDate;

    private String status;

    @OneToOne
    private BorrowRequest request;

    @ManyToOne
    private GameOwner owner;

    public LendingRecord(Date aStartDate, Date aEndDate, String aStatus, BorrowRequest aRequest, GameOwner aOwner) {
        startDate = aStartDate;
        endDate = aEndDate;
        status = aStatus;
        request = aRequest;
        owner = aOwner;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "status" + ":" + getStatus() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "startDate" + "=" + (getStartDate() != null ? !getStartDate().equals(this) ? getStartDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "endDate" + "=" + (getEndDate() != null ? !getEndDate().equals(this) ? getEndDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "request = " + (getRequest() != null ? Integer.toHexString(System.identityHashCode(getRequest())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "owner = " + (getOwner() != null ? Integer.toHexString(System.identityHashCode(getOwner())) : "null");
    }
}

