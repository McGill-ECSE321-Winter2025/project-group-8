package ca.mcgill.ecse321.gameorganizer.dtos;

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
        this.dateTime = event.getDateTime() != null ? new Date(event.getDateTime().getTime()) : null;
        this.location = event.getLocation();
        this.description = event.getDescription();
        this.maxParticipants = event.getMaxParticipants();
        this.featuredGame = event.getFeaturedGame();
        this.host = event.getHost();
    }

    private Integer eventId;
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int maxParticipants;
    private Game featuredGame;
    private Account host;
}