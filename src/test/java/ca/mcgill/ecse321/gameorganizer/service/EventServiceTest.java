package ca.mcgill.ecse321.gameorganizer.service;

import java.sql.Date; // Keep java.sql.Date if used, or change to java.util.Date consistently
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gameorganizer.dto.CreateEventRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner; // Import GameOwner if Account doesn't directly implement/extend it
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository; // Import AccountRepository
import ca.mcgill.ecse321.gameorganizer.services.EventService;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private GameRepository gameRepository;

    @Mock // Add mock for AccountRepository
    private AccountRepository accountRepository;

    @InjectMocks
    private EventService eventService;

    // Test constants
    private static final UUID VALID_EVENT_ID = UUID.randomUUID();
    private static final int VALID_GAME_ID = 1; // Added constant for game ID
    private static final String VALID_TITLE = "Game Night";
    private static final String VALID_LOCATION = "Game Room";
    private static final String VALID_DESCRIPTION = "Fun game night!";
    private static final int VALID_MAX_PARTICIPANTS = 10;
    private static final String VALID_HOST_EMAIL = "host@test.com"; // Added constant
    private static final int VALID_HOST_ID = 100; // Added constant

    @Test
    public void testCreateEventSuccess() {
        // Setup
        // Use java.util.Date for consistency if service expects it, or java.sql.Date if DB requires
        java.util.Date eventUtilDate = new java.util.Date();
        Date eventSqlDate = new Date(eventUtilDate.getTime()); // Convert if needed by model/service

        // Host must be a GameOwner since Game.setOwner expects GameOwner
 		GameOwner host = new GameOwner("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID); // Give host an ID
        // Ensure Game has necessary attributes, including an owner
        Game game = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        game.setId(VALID_GAME_ID); // Assign the constant ID
        // Set the owner on the game (assuming setOwner accepts Account or a compatible type)
 		game.setOwner(host); // Removed invalid cast to GameOwner

        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(VALID_TITLE);
        request.setDateTime(eventSqlDate); // Use appropriate Date type
        request.setLocation(VALID_LOCATION);
        request.setDescription(VALID_DESCRIPTION);
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(game);
        // request.setHost(host); // Host is now looked up by email in service

		
		// Ensure the game object used for mocking save is fully initialized, including the host
        Event savedEvent = new Event(VALID_TITLE, eventSqlDate, VALID_LOCATION, VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, game, host);
        savedEvent.setId(VALID_EVENT_ID); 

        // Mock repository lookups
        when(accountRepository.findByEmail(VALID_HOST_EMAIL)).thenReturn(Optional.of(host));
        // Mock game lookup using the ID from the game object in the request
        when(gameRepository.findById(request.getFeaturedGame().getId())).thenReturn(Optional.of(game));
 
        when(eventRepository.save(any(Event.class))).thenReturn(savedEvent);


        // Test
        Event result = eventService.createEvent(request, VALID_HOST_EMAIL);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_TITLE, result.getTitle());
        assertEquals(VALID_LOCATION, result.getLocation());
        assertEquals(VALID_DESCRIPTION, result.getDescription());
        assertEquals(VALID_MAX_PARTICIPANTS, result.getMaxParticipants());
        assertNotNull(result.getHost()); // Verify host is set
        assertEquals(VALID_HOST_EMAIL, result.getHost().getEmail()); // Verify correct host
        verify(accountRepository).findByEmail(VALID_HOST_EMAIL); // Verify lookup happened
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithNullTitle() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(null);
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(new Game()); // Game needed for validation check
        // No need to set host on DTO
        // No need to mock accountRepository.findByEmail as it won't be called

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request, VALID_HOST_EMAIL));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testCreateEventWithEmptyTitle() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle("  ");
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        request.setFeaturedGame(new Game()); // Game needed for validation check
        // No need to set host on DTO
        // No need to mock accountRepository.findByEmail as it won't be called

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request, VALID_HOST_EMAIL));
        verify(eventRepository, never()).save(any(Event.class));
    }

     @Test
    public void testCreateEventHostNotFound() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(VALID_TITLE);
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(VALID_MAX_PARTICIPANTS);
        // Provide a valid game object even if the test fails before using it fully
        Game game = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        game.setId(VALID_GAME_ID);
        // Create a dummy owner for the game, even if the host lookup fails later
        // This prevents potential NPE during Event creation if the Game requires an owner
        GameOwner dummyOwner = new GameOwner("DummyOwner", "dummy@owner.com", "pwd");
        dummyOwner.setId(999); 
        game.setOwner(dummyOwner); 
        // Ensure game ID is set *before* putting it in the request
        game.setId(VALID_GAME_ID); // Moved ID setting earlier
        request.setFeaturedGame(game);

        // Mock GameRepository lookup (needed even if host lookup fails first, to avoid NPE before that)
        // Mock game lookup using the ID from the game object in the request
        when(gameRepository.findById(request.getFeaturedGame().getId())).thenReturn(Optional.of(game));
        // Mock host lookup to return empty
        when(accountRepository.findByEmail(VALID_HOST_EMAIL)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request, VALID_HOST_EMAIL));
        verify(eventRepository, never()).save(any(Event.class));
    }


    @Test
    public void testCreateEventWithInvalidMaxParticipants() {
        // Setup
        CreateEventRequest request = new CreateEventRequest();
        request.setTitle(VALID_TITLE);
        request.setDateTime(new Date(System.currentTimeMillis()));
        request.setMaxParticipants(0);
        request.setFeaturedGame(new Game()); // Game needed for validation check
        // No need to set host on DTO
        // No need to mock accountRepository.findByEmail as it won't be called

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.createEvent(request, VALID_HOST_EMAIL));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    public void testGetEventByIdSuccess() {
        // Setup
        Account host = new Account("Host", VALID_HOST_EMAIL, "password"); // Create host
        host.setId(VALID_HOST_ID);
        Event event = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), host); // Use host
        event.setId(VALID_EVENT_ID); // Set ID

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(event));

        // Test
        Event result = eventService.getEventById(VALID_EVENT_ID);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_TITLE, result.getTitle());
        verify(eventRepository).findEventById(VALID_EVENT_ID);
    }

    @Test
    public void testGetEventByIdNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.getEventById(VALID_EVENT_ID));
        verify(eventRepository).findEventById(VALID_EVENT_ID);
    }

    @Test
    public void testGetAllEvents() {
        // Setup
        List<Event> events = new ArrayList<>();
        Account host = new Account("Host", VALID_HOST_EMAIL, "password"); // Create host
        host.setId(VALID_HOST_ID);
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), host)); // Use host
        when(eventRepository.findAll()).thenReturn(events);

        // Test
        List<Event> result = eventService.getAllEvents();

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(VALID_TITLE, result.get(0).getTitle());
        verify(eventRepository).findAll();
    }

    @Test
    public void testUpdateEventSuccess() {
        // Setup
        // Create a proper host with an email and ID
        Account host = new Account("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID);
        // Create the existing event with the host
        Event existingEvent = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), host); // Set the host here
        existingEvent.setId(VALID_EVENT_ID); // Set ID

        String newTitle = "Updated Event";
        Date newDate = new Date(System.currentTimeMillis());
        String newLocation = "New Location";
        String newDescription = "Updated description";
        int newMaxParticipants = 15;

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(existingEvent));
        // Mock save to return the modified event (or a new one if service creates new instance)
        // It's often easier to just return the argument passed to save if the service modifies in place
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Test
        Event result = eventService.updateEvent(VALID_EVENT_ID, newTitle, newDate, newLocation,
            newDescription, newMaxParticipants, VALID_HOST_EMAIL); // Use the host's email for auth check

        // Verify
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newLocation, result.getLocation());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newMaxParticipants, result.getMaxParticipants());
        assertEquals(VALID_HOST_EMAIL, result.getHost().getEmail()); // Ensure host wasn't changed
        verify(eventRepository).findEventById(VALID_EVENT_ID);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void testUpdateEventNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () ->
            eventService.updateEvent(VALID_EVENT_ID, "New Title", new Date(System.currentTimeMillis()),
                "New Location", "New Description", 15, VALID_HOST_EMAIL));
        verify(eventRepository).findEventById(VALID_EVENT_ID); // Verify find was called
        verify(eventRepository, never()).save(any(Event.class));
    }

     @Test
    public void testUpdateEventNotHost() {
        // Setup
        Account actualHost = new Account("ActualHost", "actualhost@test.com", "pwd");
        actualHost.setId(VALID_HOST_ID);
        Event existingEvent = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), actualHost);
        existingEvent.setId(VALID_EVENT_ID);

        String wrongUserEmail = "wronguser@test.com"; // Different email

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(existingEvent));

        // Test & Verify
        // Expecting ResponseStatusException (Forbidden)
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
            eventService.updateEvent(VALID_EVENT_ID, "New Title", new Date(System.currentTimeMillis()),
                "New Location", "New Description", 15, wrongUserEmail)); // Use wrong email

        verify(eventRepository).findEventById(VALID_EVENT_ID);
        verify(eventRepository, never()).save(any(Event.class)); // Save should not be called
    }


    @Test
    public void testDeleteEventSuccess() {
        // Setup
        // Create a proper host with an email and ID
        Account host = new Account("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID);
        // Create the event with the host
        Event event = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), host); // Set the host here
        event.setId(VALID_EVENT_ID); // Set ID

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(event));

        // Test
        ResponseEntity<String> response = eventService.deleteEvent(VALID_EVENT_ID, VALID_HOST_EMAIL); // Use the host's email for auth check

        // Verify
        assertEquals(200, response.getStatusCodeValue()); // Check for OK status
        verify(eventRepository).findEventById(VALID_EVENT_ID);
        verify(eventRepository).delete(event); // Verify delete was called with the correct event
    }

    @Test
    public void testDeleteEventNotFound() {
        // Setup
        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.deleteEvent(VALID_EVENT_ID, VALID_HOST_EMAIL));
        verify(eventRepository).findEventById(VALID_EVENT_ID); // Verify find was called
        verify(eventRepository, never()).delete(any(Event.class));
    }

     @Test
    public void testDeleteEventNotHost() {
        // Setup
        Account actualHost = new Account("ActualHost", "actualhost@test.com", "pwd");
        actualHost.setId(VALID_HOST_ID);
        Event eventToDelete = new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), actualHost);
        eventToDelete.setId(VALID_EVENT_ID);

        String wrongUserEmail = "wronguser@test.com"; // Different email

        when(eventRepository.findEventById(VALID_EVENT_ID)).thenReturn(Optional.of(eventToDelete));

        // Test & Verify
        // Expecting ResponseStatusException (Forbidden)
        assertThrows(org.springframework.web.server.ResponseStatusException.class, () ->
            eventService.deleteEvent(VALID_EVENT_ID, wrongUserEmail)); // Use wrong email

        verify(eventRepository).findEventById(VALID_EVENT_ID);
        verify(eventRepository, never()).delete(any(Event.class)); // Delete should not be called
    }


    // --- Search Tests ---
    // (Assuming these don't involve the AccountRepository directly in the service methods being tested,
    // but ensuring the setup uses valid host objects where applicable)

    @Test
    public void testFindEventsByGameName() {
        // Setup
        String gameName = "Test Game";
        List<Event> events = new ArrayList<>();
        Account host = new Account("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID);
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(gameName, 2, 4, "img.jpg", new java.util.Date()), host)); // Use host
        when(eventRepository.findEventByFeaturedGameName(gameName)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByGameName(gameName);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findEventByFeaturedGameName(gameName);
    }

    @Test
    public void testFindEventsByGameNameEmpty() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByGameName(""));
        verify(eventRepository, never()).findEventByFeaturedGameName(anyString());
    }

    @Test
    public void testFindEventsByLocationContaining() {
        // Setup
        String locationSearch = "Room";
        List<Event> events = new ArrayList<>();
        Account host = new Account("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID);
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, new Game(), host)); // Use host
        when(eventRepository.findEventByLocationContaining(locationSearch)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByLocationContaining(locationSearch);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findEventByLocationContaining(locationSearch);
    }

    @Test
    public void testFindEventsByLocationContainingEmpty() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByLocationContaining(""));
        verify(eventRepository, never()).findEventByLocationContaining(anyString());
    }

    @Test
    public void testFindEventsByGameMinPlayers() {
        // Setup
        int minPlayers = 2;
        List<Event> events = new ArrayList<>();
        Account host = new Account("Host", VALID_HOST_EMAIL, "password");
        host.setId(VALID_HOST_ID);
        Game game = new Game("Test Game", minPlayers, 4, "img.jpg", new java.util.Date()); // Ensure game meets criteria
        events.add(new Event(VALID_TITLE, new Date(System.currentTimeMillis()), VALID_LOCATION,
            VALID_DESCRIPTION, VALID_MAX_PARTICIPANTS, game, host)); // Use host and game
        when(eventRepository.findByFeaturedGameMinPlayersGreaterThanEqual(minPlayers)).thenReturn(events);

        // Test
        List<Event> result = eventService.findEventsByGameMinPlayers(minPlayers);

        // Verify
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findByFeaturedGameMinPlayersGreaterThanEqual(minPlayers);
    }

    @Test
    public void testFindEventsByGameMinPlayersInvalid() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> eventService.findEventsByGameMinPlayers(0));
        verify(eventRepository, never()).findByFeaturedGameMinPlayersGreaterThanEqual(0);
    }
}
