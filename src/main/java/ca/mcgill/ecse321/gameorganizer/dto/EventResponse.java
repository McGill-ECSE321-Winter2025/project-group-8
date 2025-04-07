package ca.mcgill.ecse321.gameorganizer.dto;

import java.sql.Date;
import java.util.UUID;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import lombok.Data;

@Data
public class EventResponse {

    public EventResponse(Event event) {
        this.eventId = event.getId();
        this.title = event.getTitle();
        // Safely convert java.util.Date/Timestamp to java.sql.Date
        if (event.getDateTime() != null) {
            this.dateTime = new java.sql.Date(event.getDateTime().getTime());
        } else {
            this.dateTime = null;
        }
        this.location = event.getLocation();
        this.description = event.getDescription();
        this.currentNumberParticipants = event.getCurrentNumberParticipants();
        this.maxParticipants = event.getMaxParticipants();
        this.featuredGame = event.getFeaturedGame();
        this.host = event.getHost();
    }

    private UUID eventId;
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int currentNumberParticipants;
    private int maxParticipants;
    private Game featuredGame;
    private Account host;
}
