package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.dto.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequestStatus;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BorrowRequestIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    private GameOwner testOwner;
    private Account testRequester;
    private Game testGame;
    private BorrowRequest testRequest;
    private static final String BASE_URL = "/borrowrequests";

    @BeforeEach
    public void setup() {
        // Create test game owner
        testOwner = new GameOwner("owner", "owner@example.com", "password123");
        testOwner = accountRepository.save(testOwner);

        // Create test requester
        testRequester = new Account("requester", "requester@example.com", "password123");
        testRequester = accountRepository.save(testRequester);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new Date());
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);

        // Create test borrow request
        Date startDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date endDate = new Date(System.currentTimeMillis() + 172800000); // Day after tomorrow
        testRequest = new BorrowRequest(startDate, endDate, BorrowRequestStatus.PENDING, new Date(), testGame);
        testRequest.setRequester(testRequester);
        testRequest = borrowRequestRepository.save(testRequest);
    }

    @AfterEach
    public void cleanup() {
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testCreateBorrowRequestSuccess() {
        // Create request
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            testGame.getId(),
            startDate,
            endDate
        );

        // Send request
        ResponseEntity<BorrowRequestDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            BorrowRequestDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRequester.getId(), response.getBody().getRequesterId());
        assertEquals(testGame.getId(), response.getBody().getRequestedGameId());
        assertEquals("PENDING", response.getBody().getStatus());
    }

    @Test
    public void testCreateBorrowRequestWithInvalidGame() {
        // Create request with non-existent game
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            999,
            startDate,
            endDate
        );

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testGetBorrowRequestByIdSuccess() {
        // Send request
        ResponseEntity<BorrowRequestDto> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            BorrowRequestDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRequest.getId(), response.getBody().getId());
        assertEquals(testRequester.getId(), response.getBody().getRequesterId());
        assertEquals(testGame.getId(), response.getBody().getRequestedGameId());
    }

    @Test
    public void testGetBorrowRequestByIdNotFound() {
        // Send request for non-existent request
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/999"),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetAllBorrowRequests() {
        // Create another borrow request
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        BorrowRequest request2 = new BorrowRequest(startDate, endDate, BorrowRequestStatus.PENDING, new Date(), testGame);
        request2.setRequester(testRequester);
        borrowRequestRepository.save(request2);

        // Send request
        ResponseEntity<List<BorrowRequestDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<BorrowRequestDto>>() {}
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    public void testUpdateBorrowRequestStatusSuccess() {
        // Create update request
        BorrowRequestDto request = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED",
            testRequest.getRequestDate()
        );

        // Send request
        ResponseEntity<BorrowRequestDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            BorrowRequestDto.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("APPROVED", response.getBody().getStatus());
    }

    @Test
    public void testUpdateNonExistentBorrowRequest() {
        // Create update request
        BorrowRequestDto request = new BorrowRequestDto(
            999,
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED",
            testRequest.getRequestDate()
        );

        // Send request
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.PUT,
            new HttpEntity<>(request),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteBorrowRequestSuccess() {
        // Send delete request
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.DELETE,
            null,
            Void.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(borrowRequestRepository.findById(testRequest.getId()).isPresent());
    }

    @Test
    public void testDeleteNonExistentBorrowRequest() {
        // Send delete request for non-existent request
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
