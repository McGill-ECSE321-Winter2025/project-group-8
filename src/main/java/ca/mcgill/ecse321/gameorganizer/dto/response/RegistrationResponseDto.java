package ca.mcgill.ecse321.gameorganizer.dto.response;

import java.util.Date;
import java.util.UUID;

import ca.mcgill.ecse321.gameorganizer.models.Registration;
import lombok.Getter;

/**
 * DTO for returning registration details.
 */
@Getter
public class RegistrationResponseDto {
    private int id;
    private Date registrationDate;
    private int attendeeId;
    private UUID eventId;

    public RegistrationResponseDto(Registration registration) {
        this.id = registration.getId();
        this.registrationDate = registration.getRegistrationDate();
        this.attendeeId = registration.getAttendee().getId();
        this.eventId = registration.getEventRegisteredFor().getId();
    }
}
