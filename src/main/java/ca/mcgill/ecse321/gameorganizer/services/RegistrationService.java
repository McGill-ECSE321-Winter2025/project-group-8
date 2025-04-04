package ca.mcgill.ecse321.gameorganizer.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import ca.mcgill.ecse321.gameorganizer.dto.RegistrationResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;

/**
 * Service class that handles business logic for event registration operations.
 * Provides methods for creating, retrieving, updating, and deleting event registrations.
 * 
 * @author @Shine111111
 */
@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * Creates a new registration for an event.
     *
     * @param registrationDate The date of registration
     * @param attendee The account of the person registering
     * @param eventRegisteredFor The event being registered for
     * @return The created Registration object
     */
    public Registration createRegistration(Date registrationDate, Account attendee, Event eventRegisteredFor) {
        Registration registration = new Registration(registrationDate);
        if (registrationRepository.existsByAttendeeAndEventRegisteredFor(attendee, eventRegisteredFor)) {
            throw new IllegalArgumentException("Registration already exists for this account and event.");
        }
        if (eventRegisteredFor.getCurrentNumberParticipants() >= eventRegisteredFor.getMaxParticipants()) {
            throw new IllegalArgumentException("Event is already at full capacity.");
        }
        registration.setAttendee(attendee);
        eventRegisteredFor.setCurrentNumberParticipants(eventRegisteredFor.getCurrentNumberParticipants() + 1);
        registration.setEventRegisteredFor(eventRegisteredFor);
        return registrationRepository.save(registration);
    }

    /**
     * Retrieves a registration by its ID.
     *
     * @param id The ID of the registration to retrieve
     * @return Optional containing the Registration if found
     */
    public Optional<Registration> getRegistration(int id) {
        return registrationRepository.findRegistrationById(id);
    }

    /**
     * Retrieves all registrations in the system.
     *
     * @return Iterable of all Registration objects
     */
    public Iterable<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }
    /**
     * Retrieves all registrations in the system for a selected user.
     * @param email the user's email
     *
     * @return List of all RegistrationResponseDTO objects
     */
    public List<RegistrationResponseDto> getAllRegistrationsByUserEmail(String email) {
        List<Registration> registrations = registrationRepository.findRegistrationByAttendeeEmail(email);
        List<RegistrationResponseDto> response = new ArrayList<>();
        for (Registration registration : registrations) {
            response.add(new RegistrationResponseDto(registration));
        }
        return response;
    }
    /**
     * Updates an existing registration.
     *
     * @param id The ID of the registration to update
     * @param registrationDate The new registration date
     * @param attendee The new attendee account
     * @param eventRegisteredFor The new event
     * @return The updated Registration object
     * @throws IllegalArgumentException if the registration is not found
     */
    public Registration updateRegistration(int id, Date registrationDate, Account attendee, Event eventRegisteredFor) {
        Optional<Registration> optionalRegistration = registrationRepository.findRegistrationById(id);
        if (optionalRegistration.isPresent()) {
            Registration registration = optionalRegistration.get();
            registration.setRegistrationDate(registrationDate);
            registration.setAttendee(attendee);
            registration.setEventRegisteredFor(eventRegisteredFor);
            return registrationRepository.save(registration);
        } else {
            throw new IllegalArgumentException("Registration not found");
        }
    }

    /**
     * Deletes a registration by its ID and updates the event's participant count.
     *
     * @param id The ID of the registration to delete
     * @param event The event associated with the registration
     * @throws IllegalArgumentException if the registration or event is not valid
     */
    public void deleteRegistration(int id, Event event) {
        if (!registrationRepository.existsById(id)) {
            throw new IllegalArgumentException("Registration with the given ID does not exist.");
        }
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null.");
        }
        if (event.getCurrentNumberParticipants() <= 0) {
            throw new IllegalArgumentException("Event participant count is already zero.");
        }
        event.setCurrentNumberParticipants(event.getCurrentNumberParticipants() - 1);
        registrationRepository.deleteById(id);
    }
 
}
