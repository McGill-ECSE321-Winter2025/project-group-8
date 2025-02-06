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
    private Event forEvent;

    @ManyToOne
    private Account registeredBy;

    public Registration(Date aRegistrationDate, Event aForEvent, Account aRegisteredBy) {
        registrationDate = aRegistrationDate;
        forEvent = aForEvent;
        registeredBy = aRegisteredBy;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "registrationDate" + "=" + (getRegistrationDate() != null ? !getRegistrationDate().equals(this) ? getRegistrationDate().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "forEvent = " + (getForEvent() != null ? Integer.toHexString(System.identityHashCode(getForEvent())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "registeredBy = " + (getRegisteredBy() != null ? Integer.toHexString(System.identityHashCode(getRegisteredBy())) : "null");
    }
}
