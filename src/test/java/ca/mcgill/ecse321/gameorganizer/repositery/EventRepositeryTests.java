package ca.mcgill.ecse321.gameorganizer.repositery;

import ca.mcgill.ecse321.gameorganizer.repositories.*;
import ca.mcgill.ecse321.gameorganizer.models.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;
import java.util.Optional;

import org.aspectj.lang.annotation.After;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EventRepositeryTests {
    @Autowired
    private EventRepository eventRepositery;

    @AfterEach
    public void clearDatabase() {
        eventRepositery.deleteAll();
    }

    @Test
    public void testPersistAndLoadEvent() {
        //Create event
        String title = "D&D Night";
        Date dateTime = new Date();
        String location = "Trottier 3rd floor";
        String description = "Dungeons and Dragons night";
        int maxParticipants = 10;
        //Game featuredGame = new Game();

        Event event = new Event(title, dateTime, location, description, maxParticipants, null);
        event.setTitle(title);
        event.setDateTime(dateTime);
        event.setLocation(location);
        event.setDescription(description);
        event.setMaxParticipants(maxParticipants);
        //event.setFeaturedGame(featuredGame);

        // Save event
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
        assertEquals(null, eventFromDB.get().getFeaturedGame());

    }

}