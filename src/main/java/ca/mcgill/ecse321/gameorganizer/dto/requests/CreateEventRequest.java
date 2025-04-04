package ca.mcgill.ecse321.gameorganizer.dto.requests;

import java.sql.Date;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import lombok.Data;

@Data
public class CreateEventRequest {
    private String title;
    private Date dateTime;
    private String location;
    private String description;
    private int maxParticipants;
    private Game featuredGame;
    private Account host;
}