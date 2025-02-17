package ca.mcgill.ecse321.gameorganizer.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;

@Service
public class RegistrationService {

    @Autowired
    private RegistrationRepository registrationRepository;

    public Registration createRegistration(Date registrationDate, Account attendee, Event eventRegisteredFor) {
        Registration registration = new Registration(registrationDate);
        registration.setAttendee(attendee);
        registration.setEventRegisteredFor(eventRegisteredFor);
        return registrationRepository.save(registration);
    }

    public Optional<Registration> getRegistration(int id) {
        return registrationRepository.findRegistrationById(id);
    }

    public Iterable<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

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

    public void deleteRegistration(int id) {
        registrationRepository.deleteById(id);
    }
}
