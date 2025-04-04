package ca.mcgill.ecse321.gameorganizer.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times; // Keep one import
import static org.mockito.Mockito.never;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Collections; // Keep one import

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

// Imports for Security Context Mocking
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.Registration;
import ca.mcgill.ecse321.gameorganizer.repositories.RegistrationRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository; // Keep one import
import ca.mcgill.ecse321.gameorganizer.services.RegistrationService;

@ExtendWith(MockitoExtension.class)
public class RegistrationServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AccountRepository accountRepository; // Keep one mock

    @InjectMocks
    private RegistrationService registrationService;

    // Test constants
    private static final int VALID_REGISTRATION_ID = 1;

    @Test
    public void testCreateRegistrationSuccess() {
        // Setup
        Date registrationDate = new Date();
        Account attendee = new Account("Attendee", "attendee@test.com", "password");
        Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
        Event event = new Event("Game Night", new Date(), "Location", "Description", 10, game, new Account());

        Registration registration = new Registration(registrationDate);
        registration.setId(VALID_REGISTRATION_ID);
        registration.setAttendee(attendee);
        registration.setEventRegisteredFor(event);

        when(registrationRepository.save(any(Registration.class))).thenReturn(registration);

        // Test
        Registration result = registrationService.createRegistration(registrationDate, attendee, event);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_REGISTRATION_ID, result.getId());
        assertEquals(attendee, result.getAttendee());
        assertEquals(event, result.getEventRegisteredFor());
        verify(registrationRepository).save(any(Registration.class));
    }

    @Test
    public void testGetRegistrationByIdSuccess() {
        // Setup
        Registration registration = new Registration(new Date());
        registration.setId(VALID_REGISTRATION_ID);
        when(registrationRepository.findRegistrationById(VALID_REGISTRATION_ID))
            .thenReturn(Optional.of(registration));

        // Test
        Optional<Registration> result = registrationService.getRegistration(VALID_REGISTRATION_ID);

        // Verify
        assertTrue(result.isPresent());
        assertEquals(VALID_REGISTRATION_ID, result.get().getId());
        verify(registrationRepository).findRegistrationById(VALID_REGISTRATION_ID);
    }

    @Test
    public void testGetRegistrationByIdNotFound() {
        // Setup
        when(registrationRepository.findRegistrationById(anyInt())).thenReturn(Optional.empty());

        // Test
        Optional<Registration> result = registrationService.getRegistration(VALID_REGISTRATION_ID);

        // Verify
        assertTrue(result.isEmpty());
        verify(registrationRepository).findRegistrationById(VALID_REGISTRATION_ID);
    }

    @Test
    public void testGetAllRegistrations() {
        // Setup
        List<Registration> registrations = new ArrayList<>();
        Registration registration = new Registration(new Date());
        registration.setId(VALID_REGISTRATION_ID);
        registrations.add(registration);

        when(registrationRepository.findAll()).thenReturn(registrations);

        // Test
        Iterable<Registration> result = registrationService.getAllRegistrations();

        // Verify
        assertNotNull(result);
        assertEquals(1, ((List<Registration>) result).size());
        assertEquals(VALID_REGISTRATION_ID, ((List<Registration>) result).get(0).getId());
        verify(registrationRepository).findAll();
    }

    @Test
    public void testUpdateRegistrationSuccess() {
        // Setup Attendee and Security Context
        Account attendee = new Account("Attendee", "attendee@test.com", "password");
        attendee.setId(99); // Assign an ID
        Authentication auth = new UsernamePasswordAuthenticationToken(attendee.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks and other test data
            Date newRegistrationDate = new Date();
            Account newAttendee = new Account("New Attendee", "new@test.com", "password");
            newAttendee.setId(100);
            Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
            Event newEvent = new Event("New Event", new Date(), "New Location", "New Description", 10, game, new Account());

            Registration existingRegistration = new Registration(new Date());
            existingRegistration.setId(VALID_REGISTRATION_ID);
            existingRegistration.setAttendee(attendee); // Set the attendee who is authenticated

            when(accountRepository.findByEmail(attendee.getEmail())).thenReturn(Optional.of(attendee)); // Mock finding authenticated user
            when(registrationRepository.findRegistrationById(VALID_REGISTRATION_ID))
                .thenReturn(Optional.of(existingRegistration));
            when(registrationRepository.save(any(Registration.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Test
            Registration result = registrationService.updateRegistration(VALID_REGISTRATION_ID,
                newRegistrationDate, newAttendee, newEvent);

            // Verify
            assertNotNull(result);
            assertEquals(VALID_REGISTRATION_ID, result.getId());
            assertEquals(newRegistrationDate, result.getRegistrationDate());
            assertEquals(newAttendee, result.getAttendee());
            assertEquals(newEvent, result.getEventRegisteredFor());
            verify(registrationRepository).save(any(Registration.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    public void testUpdateRegistrationNotFound() {
        // Setup
        when(registrationRepository.findRegistrationById(VALID_REGISTRATION_ID))
            .thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () ->
            registrationService.updateRegistration(VALID_REGISTRATION_ID, new Date(),
                new Account(), new Event()));
    }

    @Test
    public void testDeleteRegistration() {
        // Setup Attendee and Security Context
        Account attendee = new Account("Attendee", "attendee@test.com", "password");
        attendee.setId(99); // Assign an ID
        Authentication auth = new UsernamePasswordAuthenticationToken(attendee.getEmail(), "password", Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);

        try {
            // Setup Mocks and other test data
            Game game = new Game("Test Game", 2, 4, "test.jpg", new Date());
            Event event = new Event("Game Night", new Date(), "Location", "Description", 10, game, new Account());
            event.setCurrentNumberParticipants(5); // Ensure participants > 0

            Registration registration = new Registration(new Date());
            registration.setId(VALID_REGISTRATION_ID);
            registration.setEventRegisteredFor(event);
            registration.setAttendee(attendee); // Set the attendee who is authenticated

            when(accountRepository.findByEmail(attendee.getEmail())).thenReturn(Optional.of(attendee)); // Mock finding authenticated user
            when(registrationRepository.findRegistrationById(VALID_REGISTRATION_ID))
                .thenReturn(Optional.of(registration));
            // existsById is implicitly checked by deleteById, no need to mock separately unless logic depends on it beforehand

            // Test
            registrationService.deleteRegistration(VALID_REGISTRATION_ID);

            // Verify
            verify(registrationRepository).deleteById(VALID_REGISTRATION_ID);
            // Optionally verify participant count decreased if relevant
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
