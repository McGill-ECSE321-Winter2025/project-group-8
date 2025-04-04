package ca.mcgill.ecse321.gameorganizer.controllers;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ca.mcgill.ecse321.gameorganizer.dto.RegistrationRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.RegistrationResponseDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.services.RegistrationService;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.EventService;

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

    @GetMapping("/{email}")
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
