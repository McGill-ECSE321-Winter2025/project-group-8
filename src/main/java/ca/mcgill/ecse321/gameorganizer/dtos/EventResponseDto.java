package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.Date;
import ca.mcgill.ecse321.gameorganizer.models.Event;

/**
 * Data Transfer Object for Event responses in the API
 */
public class EventResponseDto {
    private Integer id;
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int maxParticipants;
    private GameResponseDto featuredGame;
    private AccountSummaryDto host;

    /**
     * Nested DTO for Account summary information
     */
    public static class AccountSummaryDto {
        private int id;
        private String name;
        private String email;

        public AccountSummaryDto(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    /**
     * Constructs an EventResponseDto from an Event entity
     *
     * @param event The event entity to convert to DTO
     */
    public EventResponseDto(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.dateTime = event.getDateTime();
        this.location = event.getLocation();
        this.description = event.getDescription();
        this.maxParticipants = event.getMaxParticipants();

        if (event.getFeaturedGame() != null) {
            this.featuredGame = new GameResponseDto(event.getFeaturedGame());
        }

        if (event.getHost() != null) {
            this.host = new AccountSummaryDto(
                    event.getHost().getId(),
                    event.getHost().getName(),
                    event.getHost().getEmail()
            );
        }
    }

    // Getters

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public String getDescription() {
        return description;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public GameResponseDto getFeaturedGame() {
        return featuredGame;
    }

    public AccountSummaryDto getHost() {
        return host;
    }
}