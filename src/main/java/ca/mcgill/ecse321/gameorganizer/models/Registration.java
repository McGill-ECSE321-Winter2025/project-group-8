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
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private Date registrationDate;

    @ManyToOne
    private Account attendee;

    @ManyToOne
    private Event eventRegisteredFor;

    public Registration(Date aRegistrationDate) {
        registrationDate = aRegistrationDate;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "registrationDate" + "=" + (getRegistrationDate() != null ? !getRegistrationDate().equals(this) ? getRegistrationDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator");
    }
}
