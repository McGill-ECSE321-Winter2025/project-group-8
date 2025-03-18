package ca.mcgill.ecse321.gameorganizer.integration;

import ca.mcgill.ecse321.gameorganizer.controllers.AccountController;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Calendar;
import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private GameRepository gameRepository;

    private Account account;

    private Account account2;

    private Event event;

    private Event event2;

    @BeforeEach
    public void setUp() {
        account = new Account("Bob", "bob@gmail.com", "6969");
        account2 = new Account("Alice", "alice@gmail.com", "6969");

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date yesterday = calendar.getTime();
        Date currentDate = new Date();

        Game dungeonsDragons= new Game(
                "Dungeons & Dragons",
                3,
                12,
                "image/placeholder",
                yesterday
        );

        Game yugioh = new Game(
                "Yu-Gi-Oh",
                2,
                12,
                "image/placeholder",
                yesterday
        );

        gameRepository.save(dungeonsDragons);
        gameRepository.save(yugioh);

        event = new Event(
                "D&D Campaign",
                currentDate,
                "My basement",
                "No girls allowed",
                8,
                dungeonsDragons
                );

        event2 = new Event(
                "Yu-Gi-Oh Tournament",
                currentDate,
                "My appt",
                "No boys allowed",
                8,
                yugioh
        );

        eventRepository.save(event);
        eventRepository.save(event2);
        accountRepository.save(account);
        accountRepository.save(account2);
    }

    @AfterEach
    public void tearDown() {
        accountRepository.deleteAll();
        gameRepository.deleteAll();
        eventRepository.deleteAll();
    }

    @Test
    public void testCreateAccount() {}

    @Test
    public void testGetAccount() {}



}
