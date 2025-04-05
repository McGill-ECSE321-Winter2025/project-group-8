package ca.mcgill.ecse321.gameorganizer.dto;

import ca.mcgill.ecse321.gameorganizer.models.Event;
// Removed: import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Account; // Re-added
import ca.mcgill.ecse321.gameorganizer.models.Game; // Re-added
// Removed: import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.dto.UserSummaryDto; // Added
import ca.mcgill.ecse321.gameorganizer.dto.GameSummaryDto; // Added

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