package ca.mcgill.ecse321.gameorganizer.dto;

import java.sql.Date;
import java.util.UUID;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.dto.UserSummaryDto;
import ca.mcgill.ecse321.gameorganizer.dto.GameSummaryDto;
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
        // Convert Game to GameSummaryDto
        if (event.getFeaturedGame() != null) {
            Game game = event.getFeaturedGame();
            this.featuredGame = new GameSummaryDto(game.getId(), game.getName(), game.getImage()); // Changed getCoverLink() to getImage()
        } else {
            this.featuredGame = null;
        }
        // Convert Account to UserSummaryDto
        if (event.getHost() != null) {
            Account hostAccount = event.getHost();
            this.host = new UserSummaryDto(hostAccount.getId(), hostAccount.getName()); // Changed getUsername() to getName()
            // Note: Using Account's getId() and getName().
        } else {
            this.host = null;
        }
    }

    private UUID eventId;
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int currentNumberParticipants;
    private int maxParticipants;
    private GameSummaryDto featuredGame; // Changed type
    private UserSummaryDto host;         // Changed type
}
