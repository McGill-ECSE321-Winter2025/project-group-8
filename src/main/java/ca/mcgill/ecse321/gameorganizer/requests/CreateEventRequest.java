package ca.mcgill.ecse321.gameorganizer.requests;

import ca.mcgill.ecse321.gameorganizer.models.Event;
import lombok.Data;

@Data
public class CreateEventRequest {
    private String title;
    private String dateTime;
    private String location;
    private String description;
    private int maxParticipants;
    private String featuredGame;
}