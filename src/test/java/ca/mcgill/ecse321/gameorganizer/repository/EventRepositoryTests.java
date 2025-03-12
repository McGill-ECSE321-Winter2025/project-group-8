package ca.mcgill.ecse321.gameorganizer.repository;

import ca.mcgill.ecse321.gameorganizer.repositories.*;
import jakarta.transaction.Transactional;
import ca.mcgill.ecse321.gameorganizer.models.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EventRepositoryTests {
    @Autowired
    private EventRepository eventRepositery;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void clearDatabase() {
        eventRepositery.deleteAll();
    }

    @Test
    public void testPersistAndLoadEvent() {
        //Create event
        String title = "D&D Night";
        java.util.Date utilDate = new Date();
        java.sql.Date dateTime = new java.sql.Date(utilDate.getTime());
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());

        Event event = new Event(title, dateTime, location, description, maxParticipants, featuredGame);

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event = eventRepositery.save(event);
        int eventId = event.getId();

        //Read event from database
        Optional<Event> eventFromDB = eventRepositery.findEventById(eventId);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertEquals(title, eventFromDB.get().getTitle());
        assertEquals(dateTime, eventFromDB.get().getDateTime());
        assertEquals(location, eventFromDB.get().getLocation());
        assertEquals(description, eventFromDB.get().getDescription());
        assertEquals(maxParticipants, eventFromDB.get().getMaxParticipants());
        assertEquals(featuredGame, eventFromDB.get().getFeaturedGame());
    }

    @Test
    public void testLoadNonexistantEvent() {
        //Create event
        String title = "D&D Night";
        java.util.Date utilDate = new Date();
        java.sql.Date dateTime = new java.sql.Date(utilDate.getTime());
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());

        Event event = new Event(title, dateTime, location, description, maxParticipants, featuredGame);

        // Save event and the associated game and delete them
        gameRepository.save(featuredGame);
        event = eventRepositery.save(event);
        int eventId = event.getId();
        eventRepositery.delete(event);

        //Read event from database
        Optional<Event> eventFromDB = eventRepositery.findEventById(eventId);

        //Assert correct response
        assertFalse(eventFromDB.isPresent());
    }

    @Test
    public void testFindByTitle() {
        //Create event
        String title1 = "D&D Night";
        String title2 = "Monopoly Night";
        String title3 = "Werewolf";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title1, dateTime, location, description, maxParticipants, featuredGame);
        Event event2 = new Event(title2, dateTime, location, description, maxParticipants, featuredGame);
        Event event3 = new Event(title3, dateTime, location, description, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByTitle(title1);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(1, eventFromDB.size());
        Event retrievedEvent = eventFromDB.get(0);
        assertEquals(title1, retrievedEvent.getTitle());
        assertEquals(dateTime, retrievedEvent.getDateTime());
        assertEquals(location, retrievedEvent.getLocation());
        assertEquals(description, retrievedEvent.getDescription());
        assertEquals(maxParticipants, retrievedEvent.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent.getFeaturedGame());
    }

    @Test
    public void testFindByTitleContaining() {
        //Create event
        String title1 = "D&D Night";
        String title2 = "Monopoly Night";
        String title3 = "Werewolf";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title1, dateTime, location, description, maxParticipants, featuredGame);
        Event event2 = new Event(title2, dateTime, location, description, maxParticipants, featuredGame);
        Event event3 = new Event(title3, dateTime, location, description, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByTitleContaining("Night");

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(2, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        assertEquals(title1, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
        assertEquals(title2, retrievedEvent2.getTitle());
        assertEquals(dateTime, retrievedEvent2.getDateTime());
        assertEquals(location, retrievedEvent2.getLocation());
        assertEquals(description, retrievedEvent2.getDescription());
        assertEquals(maxParticipants, retrievedEvent2.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent2.getFeaturedGame());
    }

    @Test
    public void testFindByDateTime() {
        //Create event
        String title = "D&D Night";
        Date dateTime1 = new Date();
        Date dateTime2 = new Date(dateTime1.getTime() + 1000 * 60 * 60); 
        Date dateTime3 = new Date(dateTime1.getTime() + 1000 * 60 * 60 * 2); 
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title, dateTime1, location, description, maxParticipants, featuredGame);
        Event event2 = new Event(title, dateTime2, location, description, maxParticipants, featuredGame);
        Event event3 = new Event(title, dateTime3, location, description, maxParticipants, featuredGame);
        Event event4 = new Event(title, dateTime2, location, description, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        java.sql.Date sqlDateTime = new java.sql.Date(dateTime2.getTime());
        List<Event> eventFromDB = eventRepositery.findEventByDateTime(sqlDateTime);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(2, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime2, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
        assertEquals(title, retrievedEvent2.getTitle());
        assertEquals(dateTime2, retrievedEvent2.getDateTime());
        assertEquals(location, retrievedEvent2.getLocation());
        assertEquals(description, retrievedEvent2.getDescription());
        assertEquals(maxParticipants, retrievedEvent2.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent2.getFeaturedGame());
    }

    @Test
    public void testFindByLocation() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location1 = "Trottier 3rd floor";
        String location2 = "Trottier 4th floor";
        String location3 = "Trottier 5th floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title, dateTime, location1, description, maxParticipants, featuredGame);
        Event event2 = new Event(title, dateTime, location2, description, maxParticipants, featuredGame);
        Event event3 = new Event(title, dateTime, location2, description, maxParticipants, featuredGame);
        Event event4 = new Event(title, dateTime, location3, description, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByLocation(location2);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(2, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location2, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
        assertEquals(title, retrievedEvent2.getTitle());
        assertEquals(dateTime, retrievedEvent2.getDateTime());
        assertEquals(location2, retrievedEvent2.getLocation());
        assertEquals(description, retrievedEvent2.getDescription());
        assertEquals(maxParticipants, retrievedEvent2.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent2.getFeaturedGame());
    }
    
    @Test
    public void testFindByLocationContaining() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location1 = "Trottier 3rd floor";
        String location2 = "Trottier 4th floor";
        String location3 = "Trottier 5th floor";
        String location4 = "McConnell basement";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title, dateTime, location1, description, maxParticipants, featuredGame);
        Event event2 = new Event(title, dateTime, location2, description, maxParticipants, featuredGame);
        Event event3 = new Event(title, dateTime, location3, description, maxParticipants, featuredGame);
        Event event4 = new Event(title, dateTime, location4, description, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByLocationContaining("Trottier");

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(3, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        Event retrievedEvent3 = eventFromDB.get(2);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location1, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
        assertEquals(title, retrievedEvent2.getTitle());
        assertEquals(dateTime, retrievedEvent2.getDateTime());
        assertEquals(location2, retrievedEvent2.getLocation());
        assertEquals(description, retrievedEvent2.getDescription());
        assertEquals(maxParticipants, retrievedEvent2.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent2.getFeaturedGame());
        assertEquals(title, retrievedEvent3.getTitle());
        assertEquals(dateTime, retrievedEvent3.getDateTime());
        assertEquals(location3, retrievedEvent3.getLocation());
        assertEquals(description, retrievedEvent3.getDescription());
        assertEquals(maxParticipants, retrievedEvent3.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent3.getFeaturedGame());
    }

    @Test
    public void testFindByDescription() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description1 = "Dungeons and Dragons night";
        String description2 = "Monopoly night";
        String description3 = "Werewolf night";
        String description4 = "Twister";
        int maxParticipants = 10;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title, dateTime, location, description1, maxParticipants, featuredGame);
        Event event2 = new Event(title, dateTime, location, description2, maxParticipants, featuredGame);
        Event event3 = new Event(title, dateTime, location, description3, maxParticipants, featuredGame);
        Event event4 = new Event(title, dateTime, location, description4, maxParticipants, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByDescription(description2);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(1, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description2, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
    }

    @Test
    public void testFindByMaxParticipants() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants1 = 10;
        int maxParticipants2 = 20;
        int maxParticipants3 = 30;
        int maxParticipants4 = 20;
        Game featuredGame = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Event event1 = new Event(title, dateTime, location, description, maxParticipants1, featuredGame);
        Event event2 = new Event(title, dateTime, location, description, maxParticipants2, featuredGame);
        Event event3 = new Event(title, dateTime, location, description, maxParticipants3, featuredGame);
        Event event4 = new Event(title, dateTime, location, description, maxParticipants4, featuredGame);   

        // Save event and the associated game
        gameRepository.save(featuredGame);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByMaxParticipants(maxParticipants2);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(2, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants2, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent1.getFeaturedGame());
        assertEquals(title, retrievedEvent2.getTitle());
        assertEquals(dateTime, retrievedEvent2.getDateTime());
        assertEquals(location, retrievedEvent2.getLocation());
        assertEquals(description, retrievedEvent2.getDescription());
        assertEquals(maxParticipants2, retrievedEvent2.getMaxParticipants());
        assertEquals(featuredGame, retrievedEvent2.getFeaturedGame());
    }

    @Test
    public void testFindByFeaturedGameMinPlayers() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame1 = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Game featuredGame2 = new Game("Monopoly", 2, 4, "Monopoly.jpg", new Date());
        Game featuredGame3 = new Game("Werewolf", 5, 10, "Werewolf.jpg", new Date());
        Game featuredGame4 = new Game("Twister", 2, 4, "Twister.jpg", new Date());
        Event event1 = new Event(title, dateTime, location, description, maxParticipants, featuredGame1);
        Event event2 = new Event(title, dateTime, location, description, maxParticipants, featuredGame2);
        Event event3 = new Event(title, dateTime, location, description, maxParticipants, featuredGame3);
        Event event4 = new Event(title, dateTime, location, description, maxParticipants, featuredGame4);   

        // Save event and the associated game
        gameRepository.save(featuredGame1);
        gameRepository.save(featuredGame2);
        gameRepository.save(featuredGame3);
        gameRepository.save(featuredGame4);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findByFeaturedGameMinPlayers(2);

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(2, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        Event retrievedEvent2 = eventFromDB.get(1);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame2, retrievedEvent1.getFeaturedGame());
        assertEquals(title, retrievedEvent2.getTitle());
    }

    @Test
    public void testFindByFeaturedGameId() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        Game featuredGame1 = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
        Game featuredGame2 = new Game("Monopoly", 2, 4, "Monopoly.jpg", new Date());
        Game featuredGame3 = new Game("Werewolf", 5, 10, "Werewolf.jpg", new Date());
        Game featuredGame4 = new Game("Twister", 2, 4, "Twister.jpg", new Date());
        Event event1 = new Event(title, dateTime, location, description, maxParticipants, featuredGame1);
        Event event2 = new Event(title, dateTime, location, description, maxParticipants, featuredGame2);
        Event event3 = new Event(title, dateTime, location, description, maxParticipants, featuredGame3);
        Event event4 = new Event(title, dateTime, location, description, maxParticipants, featuredGame4);   

        // Save event and the associated game
        gameRepository.save(featuredGame1);
        gameRepository.save(featuredGame2);
        gameRepository.save(featuredGame3);
        gameRepository.save(featuredGame4);
        event1 = eventRepositery.save(event1);
        event2 = eventRepositery.save(event2);  
        event3 = eventRepositery.save(event3);
        event4 = eventRepositery.save(event4);  

        //Read event from database
        List<Event> eventFromDB = eventRepositery.findEventByFeaturedGameId(featuredGame2.getId());

        //Assert correct response
        assertNotNull(eventFromDB);
        assertFalse(eventFromDB.isEmpty());
        assertEquals(1, eventFromDB.size());
        Event retrievedEvent1 = eventFromDB.get(0);
        assertEquals(title, retrievedEvent1.getTitle());
        assertEquals(dateTime, retrievedEvent1.getDateTime());
        assertEquals(location, retrievedEvent1.getLocation());
        assertEquals(description, retrievedEvent1.getDescription());
        assertEquals(maxParticipants, retrievedEvent1.getMaxParticipants());
        assertEquals(featuredGame2, retrievedEvent1.getFeaturedGame());
    }

    @Test
    public void testFindByFeaturedGameName() {
    // Create event details
    String title = "Board Game Night";
    Date dateTime = new Date();
    String location = "Trottier 3rd floor";
    String description = "A fun night of board games!";
    int maxParticipants = 10;

    // Create two games with the same name "Monopoly"
    Game featuredGame1 = new Game("Dungeons and Dragons", 3, 6, "D&D.jpg", new Date());
    Game featuredGame2 = new Game("Monopoly", 2, 4, "Monopoly.jpg", new Date()); 
    Game featuredGame3 = new Game("Werewolf", 5, 10, "Werewolf.jpg", new Date());
    Game featuredGame4 = new Game("Twister", 2, 4, "Twister.jpg", new Date());
    Game featuredGame5 = new Game("Monopoly", 2, 6, "Monopoly2.jpg", new Date());

    // Create events with associated games
    Event event1 = new Event(title, dateTime, location, description, maxParticipants, featuredGame1);
    Event event2 = new Event(title, dateTime, location, description, maxParticipants, featuredGame2);
    Event event3 = new Event(title, dateTime, location, description, maxParticipants, featuredGame3);
    Event event4 = new Event(title, dateTime, location, description, maxParticipants, featuredGame4);
    Event event5 = new Event(title, dateTime, location, description, maxParticipants, featuredGame5); 

    // Save games
    gameRepository.save(featuredGame1);
    gameRepository.save(featuredGame2);
    gameRepository.save(featuredGame3);
    gameRepository.save(featuredGame4);
    gameRepository.save(featuredGame5);

    // Save events
    eventRepositery.save(event1);
    eventRepositery.save(event2); 
    eventRepositery.save(event3);
    eventRepositery.save(event4);
    eventRepositery.save(event5);  

    // Retrieve events where the game name is "Monopoly"
    List<Event> eventFromDB = eventRepositery.findEventByFeaturedGameName("Monopoly");

    // Assertions
    assertNotNull(eventFromDB);
    assertFalse(eventFromDB.isEmpty());
    assertEquals(2, eventFromDB.size());

    // Ensure the retrieved events match Monopoly games
    List<Game> retrievedGames = eventFromDB.stream().map(Event::getFeaturedGame).toList();
    assertTrue(retrievedGames.contains(featuredGame2));
    assertTrue(retrievedGames.contains(featuredGame5));
}


@Test
@Transactional
public void testFindEventByHostId() {
    // Create two hosts
    Account host1 = new Account("Host One", "host1@test.com", "password1");
    Account host2 = new Account("Host Two", "host2@test.com", "password2");
    host1 = accountRepository.save(host1);
    host2 = accountRepository.save(host2);

    // Create four games
    Game game1 = new Game("Dungeons and Dragons", 3, 6, "dnd.jpg", new Date());
    Game game2 = new Game("Monopoly", 2, 4, "monopoly.jpg", new Date());
    Game game3 = new Game("Risk", 2, 6, "risk.jpg", new Date());
    Game game4 = new Game("Chess", 2, 2, "chess.jpg", new Date());
    
    game1 = gameRepository.save(game1);
    game2 = gameRepository.save(game2);
    game3 = gameRepository.save(game3);
    game4 = gameRepository.save(game4);

    // Create three events (1 for host1, 2 for host2)
    // Event for host1
    Event event1 = new Event("D&D Night", new java.sql.Date(new Date().getTime()),
            "Room 101", "First event", 6, game1);
    event1.setHost(host1);
    eventRepositery.save(event1);

    // Events for host2
    Event event2 = new Event("Monopoly Night", new java.sql.Date(new Date().getTime()),
            "Room 102", "Second event", 4, game2);
    event2.setHost(host2);
    eventRepositery.save(event2);

    Event event3 = new Event("Risk Tournament", new java.sql.Date(new Date().getTime()),
            "Room 103", "Third event", 6, game3);
    event3.setHost(host2);
    eventRepositery.save(event3);

    // Game4 remains unassociated with any event

    // Retrieve events for host2
    List<Event> eventsForHost2 = eventRepositery.findEventByHostId(host2.getId());

    // Assertions
    assertNotNull(eventsForHost2, "The list of events should not be null");
    assertEquals(2, eventsForHost2.size(), "Host2 should have exactly 2 events");
    
    // Verify both events belong to host2 and have correct games
    for (Event event : eventsForHost2) {
        assertEquals(host2.getId(), event.getHost().getId(), "Event should belong to host2");
        assertTrue(
            event.getFeaturedGame().getName().equals("Monopoly") || 
            event.getFeaturedGame().getName().equals("Risk"),
            "Event should feature either Monopoly or Risk"
        );
    }

    // Additional verification for specific events
    boolean hasMonopolyEvent = eventsForHost2.stream()
            .anyMatch(e -> e.getFeaturedGame().getName().equals("Monopoly"));
    boolean hasRiskEvent = eventsForHost2.stream()
            .anyMatch(e -> e.getFeaturedGame().getName().equals("Risk"));
    
    assertTrue(hasMonopolyEvent, "Host2 should have a Monopoly event");
    assertTrue(hasRiskEvent, "Host2 should have a Risk event");
}

@Test
@Transactional
public void testFindEventByHostName() {
    // Create two hosts
    Account host1 = new Account("Host One", "host1@test.com", "password1");
    Account host2 = new Account("Host Two", "host2@test.com", "password2");
    host1 = accountRepository.save(host1);
    host2 = accountRepository.save(host2);

    // Create four games
    Game game1 = new Game("Dungeons and Dragons", 3, 6, "dnd.jpg", new Date());
    Game game2 = new Game("Monopoly", 2, 4, "monopoly.jpg", new Date());
    Game game3 = new Game("Risk", 2, 6, "risk.jpg", new Date());
    Game game4 = new Game("Chess", 2, 2, "chess.jpg", new Date());

    game1 = gameRepository.save(game1);
    game2 = gameRepository.save(game2);
    game3 = gameRepository.save(game3);
    game4 = gameRepository.save(game4);

    // Create three events (1 for host1, 2 for host2)
    // Event for host1
    Event event1 = new Event("D&D Night", new java.sql.Date(new Date().getTime()),
            "Room 101", "First event", 6, game1);
    event1.setHost(host1);
    eventRepositery.save(event1);

    // Events for host2
    Event event2 = new Event("Monopoly Night", new java.sql.Date(new Date().getTime()),
            "Room 102", "Second event", 4, game2);
    event2.setHost(host2);
    eventRepositery.save(event2);

    Event event3 = new Event("Risk Tournament", new java.sql.Date(new Date().getTime()),
            "Room 103", "Third event", 6, game3);
    event3.setHost(host2);
    eventRepositery.save(event3);

    // Game4 remains unassociated with any event

    // Retrieve events for host2 by host name
    List<Event> eventsForHost2ByName = eventRepositery.findEventByHostName(host2.getName());

    // Assertions
    assertNotNull(eventsForHost2ByName, "The list of events should not be null");
    assertEquals(2, eventsForHost2ByName.size(), "Host2 should have exactly 2 events");

    // Verify both events belong to host2 and have correct games
    for (Event event : eventsForHost2ByName) {
        assertEquals(host2.getId(), event.getHost().getId(), "Event should belong to host2");
        assertTrue(
            event.getFeaturedGame().getName().equals("Monopoly") || 
            event.getFeaturedGame().getName().equals("Risk"),
            "Event should feature either Monopoly or Risk"
        );
    }

    // Additional verification for specific events
    boolean hasMonopolyEvent = eventsForHost2ByName.stream()
            .anyMatch(e -> e.getFeaturedGame().getName().equals("Monopoly"));
    boolean hasRiskEvent = eventsForHost2ByName.stream()
            .anyMatch(e -> e.getFeaturedGame().getName().equals("Risk"));

    assertTrue(hasMonopolyEvent, "Host2 should have a Monopoly event");
    assertTrue(hasRiskEvent, "Host2 should have a Risk event");
    }
}