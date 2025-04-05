package ca.mcgill.ecse321.gameorganizer.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.dto.RegistrationRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.RegistrationResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.EventService;
import ca.mcgill.ecse321.gameorganizer.services.RegistrationService;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EventService eventService;

    /**
     * Creates a new registration.
     */
    @PostMapping
    public ResponseEntity<RegistrationResponseDto> createRegistration(@RequestBody RegistrationRequestDto dto) {
        Account attendee = accountService.getAccountById(dto.getAttendeeId());
        Event event = eventService.getEventById(dto.getEventId());

        Registration registration = registrationService.createRegistration(dto.getRegistrationDate(), attendee, event);
        return new ResponseEntity<>(new RegistrationResponseDto(registration), HttpStatus.CREATED);
    }

    /**
     * Retrieves a registration by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> getRegistration(@PathVariable int id) {
        Optional<Registration> registration = registrationService.getRegistration(id);
        return registration.map(value -> ResponseEntity.ok(new RegistrationResponseDto(value)))
                           .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Retrieves all registrations.
     */
    @GetMapping
    public ResponseEntity<List<RegistrationResponseDto>> getAllRegistrations() {
        List<RegistrationResponseDto> registrations = ((List<Registration>) registrationService.getAllRegistrations())
                .stream()
                .map(RegistrationResponseDto::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(registrations);
    }

    // Changed path to avoid ambiguity with getRegistration by ID
    @GetMapping("/user/{email}")
    public ResponseEntity<List<RegistrationResponseDto>> getAllRegistrationsByUserEmail(@PathVariable String email) {
        List<RegistrationResponseDto> response = registrationService.getAllRegistrationsByUserEmail(email);
        return ResponseEntity.ok(response);
    }
    /**
     * Updates an existing registration.
     */
    @PutMapping("/{id}")
    public ResponseEntity<RegistrationResponseDto> updateRegistration(@PathVariable int id,
                                                                      @RequestBody RegistrationRequestDto dto) {
        Account attendee = accountService.getAccountById(dto.getAttendeeId());
        Event event = eventService.getEventById(dto.getEventId());

        Registration updatedRegistration = registrationService.updateRegistration(id, dto.getRegistrationDate(), attendee, event);
        return ResponseEntity.ok(new RegistrationResponseDto(updatedRegistration));
    }

    /**
     * Deletes a registration by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRegistration(@PathVariable int id) {
        Optional<Registration> registration = registrationService.getRegistration(id);
        if (registration.isPresent()) {
            registrationService.deleteRegistration(id);
            return ResponseEntity.ok("Registration deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Registration not found.");
        }
    }
}
