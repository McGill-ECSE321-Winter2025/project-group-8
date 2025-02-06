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
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private String title;

    private Date dateTime;

    private String location;

    private String description;

    private int maxParticipants;

    @ManyToOne
    private Account createdBy;

    @ManyToOne
    private Game featuredGame;

    public Event(String aTitle, Date aDateTime, String aLocation, String aDescription, int aMaxParticipants, Account aCreatedBy, Game aFeaturedGame) {
        title = aTitle;
        dateTime = aDateTime;
        location = aLocation;
        description = aDescription;
        maxParticipants = aMaxParticipants;
        createdBy = aCreatedBy;
        featuredGame = aFeaturedGame;
    }

    public void delete() {
        createdBy = null;
        featuredGame = null;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "title" + ":" + getTitle() + "," +
                "location" + ":" + getLocation() + "," +
                "description" + ":" + getDescription() + "," +
                "maxParticipants" + ":" + getMaxParticipants() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateTime" + "=" + (getDateTime() != null ? !getDateTime().equals(this) ? getDateTime().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "createdBy = " + (getCreatedBy() != null ? Integer.toHexString(System.identityHashCode(getCreatedBy())) : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "featuredGame = " + (getFeaturedGame() != null ? Integer.toHexString(System.identityHashCode(getFeaturedGame())) : "null");
    }
}
