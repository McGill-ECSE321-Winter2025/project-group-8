package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Represents a gaming event in the system.
 * Events are organized gatherings where users can meet to play games.
 * Each event has a featured game, a host, and can accommodate a maximum number of participants.
 * 
 * @author @Yessine-glitch
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Event {

    /**
     * Unique identifier for the event.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * The title or name of the event.
     */
    private String title;

    /**
     * The date and time when the event will take place.
     */
    private Date dateTime;

    /**
     * The physical location where the event will be held.
     */
    private String location;

    /**
     * A detailed description of the event.
     */
    private String description;

    /**
     * The current number of participants attending the event.
     */
    private int currentNumberParticipants;

    /**
     * The maximum number of participants that can attend the event.
     */
    private int maxParticipants;

    /**
     * The main game that will be featured at this event.
     */
    @ManyToOne // Event must have a featured game
    private Game featuredGame;

    /**
     * The account of the user who is hosting the event.
     */
    @ManyToOne // Event must have a host
    private Account host;

    /**
     * Creates a new event with the specified details. (except host)
     *
     * @param aTitle The title of the event
     * @param aDateTime The date and time when the event will occur
     * @param aLocation The location where the event will be held
     * @param aDescription A description of the event
     * @param aMaxParticipants The maximum number of participants allowed
     * @param aFeaturedGame The game that will be featured at the event
     */
    public Event(String aTitle, Date aDateTime, String aLocation, String aDescription, int aMaxParticipants, Game aFeaturedGame) {
        title = aTitle;
        dateTime = aDateTime;
        location = aLocation;
        description = aDescription;
        maxParticipants = aMaxParticipants;
        featuredGame = aFeaturedGame;
        currentNumberParticipants = 0; 
    }

    /**
     * Creates a new event with the specified details.
     *
     * @param aTitle The title of the event
     * @param aDateTime The date and time when the event will occur
     * @param aLocation The location where the event will be held
     * @param aDescription A description of the event
     * @param aMaxParticipants The maximum number of participants allowed
     * @param aFeaturedGame The game that will be featured at the event
     * @param host The account of the user hosting the event
     */
    public Event(String aTitle, Date aDateTime, String aLocation, String aDescription, int aMaxParticipants, Game aFeaturedGame, Account aHost) {
        title = aTitle;
        dateTime = aDateTime;
        location = aLocation;
        description = aDescription;
        maxParticipants = aMaxParticipants;
        featuredGame = aFeaturedGame;
        this.host = aHost;
        currentNumberParticipants = 0; 
    }

    /**
     * Returns a string representation of the Event.
     *
     * @return A string containing the event's details including ID, title, location, description,
     *         maximum participants, date/time, and featured game reference
     */
    public String toString() {
        return super.toString() + "[" +
            "id" + ":" + getId() + "," +
            "title" + ":" + getTitle() + "," +
            "location" + ":" + getLocation() + "," +
            "description" + ":" + getDescription() + "," +
            "maxParticipants" + ":" + getMaxParticipants() + "," +
            "currentNumberParticipants" + ":" + getCurrentNumberParticipants() + "]" + System.getProperties().getProperty("line.separator") +
            "  " + "dateTime" + "=" + (getDateTime() != null ? !getDateTime().equals(this) ? getDateTime().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator") +
            "  " + "featuredGame = " + (getFeaturedGame() != null ? Integer.toHexString(System.identityHashCode(getFeaturedGame())) : "null");
    }
}
