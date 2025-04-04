package ca.mcgill.ecse321.gameorganizer.dto;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;

import java.sql.Date;
import java.util.UUID;
import lombok.Data;

@Data
public class EventResponse {

    public EventResponse(Event event) {
        this.eventId = event.getId();
        this.title = event.getTitle();
        this.dateTime = (Date) event.getDateTime();
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