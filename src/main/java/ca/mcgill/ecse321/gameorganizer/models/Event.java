package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

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

    // Associations

    @ManyToOne
    private Game featuredGame;

    @OneToMany
    private List<Event> registrations;

    // Methods

    public Event(String aTitle, Date aDateTime, String aLocation, String aDescription, int aMaxParticipants, Game aFeaturedGame) {
        title = aTitle;
        dateTime = aDateTime;
        location = aLocation;
        description = aDescription;
        maxParticipants = aMaxParticipants;
        featuredGame = aFeaturedGame;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "title" + ":" + getTitle() + "," +
                "location" + ":" + getLocation() + "," +
                "description" + ":" + getDescription() + "," +
                "maxParticipants" + ":" + getMaxParticipants() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateTime" + "=" + (getDateTime() != null ? !getDateTime().equals(this) ? getDateTime().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
                "  " + "featuredGame = " + (getFeaturedGame() != null ? Integer.toHexString(System.identityHashCode(getFeaturedGame())) : "null");
    }
}
