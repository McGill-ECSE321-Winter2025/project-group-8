package ca.mcgill.ecse321.gameorganizer.integration;

import ca.mcgill.ecse321.gameorganizer.controllers.AccountController;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountControllerIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @Autowired
    private AccountRepository accountRepository;

    private Account account;

    private Account account2;

    @BeforeEach
    public void setUp() {
        account = new Account("Bob", "bob@gmail.com", "6969");
        account2 = new Account("Alice", "alice@gmail.com", "6969");
        accountRepository.save(account);
        accountRepository.save(account2);
    }

    public void




}
