package ca.mcgill.ecse321.gameorganizer.integration;

import java.sql.Date;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer; 
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.BorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.CreateBorrowRequestDto;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
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
@Import({TestConfig.class, SecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    private static final String BASE_URL = "/api/v1/borrowrequests";

    @BeforeEach
    public void setup() {
        // Clean repositories first
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        // Create test game owner as a GameOwner
        testOwner = new GameOwner("owner", "owner@example.com", "password123");
        testOwner = (GameOwner) accountRepository.save(testOwner);

        // Create test requester
        testRequester = new Account("requester", "requester@example.com", "password123");
        testRequester = accountRepository.save(testRequester);

        // Create test game
        testGame = new Game("Test Game", 2, 4, "test.jpg", new java.util.Date());
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);

        // Create test borrow request
        Date startDate = new Date(System.currentTimeMillis() + 86400000); // Tomorrow
        Date endDate = new Date(System.currentTimeMillis() + 172800000);   // Day after tomorrow
        testRequest = new BorrowRequest(startDate, endDate, BorrowRequestStatus.PENDING, new java.util.Date(), testGame);
        testRequest.setRequester(testRequester);
        testRequest = borrowRequestRepository.save(testRequest);
    }

    @AfterEach
    public void cleanup() {
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // Build URL using BASE_URL (which already contains /api/v1/borrowrequests)
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    private HttpHeaders createAuthHeaders() {
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(testRequester.getEmail());
        loginRequest.setPassword("password123"); // Use the correct password for the testRequester

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort("/api/v1/auth/login"),
            loginRequest,
            LoginResponse.class
        );

        String sessionId = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionId);
        headers.set("User-Id", String.valueOf(testRequester.getId()));
        return headers;
    }

    // ----- CREATE Tests (4 tests) -----

    @Test
    @Order(1)
    public void testCreateBorrowRequestSuccess() {
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            testGame.getId(),
            startDate,
            endDate
        );


        HttpHeaders headers = createAuthHeaders(); // Add authentication headers
        HttpEntity<CreateBorrowRequestDto> requestEntity = new HttpEntity<>(request, headers);

        ResponseEntity<BorrowRequestDto> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            BorrowRequestDto.class
        );

        // Expect 200 OK on success
        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(testRequester.getId(), dto.getRequesterId());
        assertEquals(testGame.getId(), dto.getRequestedGameId());
        assertEquals("PENDING", dto.getStatus());
    }

    @Test
    @Order(2)
    public void testCreateBorrowRequestWithInvalidGame() {
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            999,  // invalid game id
            startDate,
            endDate
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void testCreateBorrowRequestWithInvalidRequester() {
        Date startDate = new Date(System.currentTimeMillis() + 86400000);
        Date endDate = new Date(System.currentTimeMillis() + 172800000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            999,  // invalid requester id
            testGame.getId(),
            startDate,
            endDate
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testCreateBorrowRequestWithInvalidDates() {
        // Start date is after end date should result in BAD_REQUEST
        Date startDate = new Date(System.currentTimeMillis() + 172800000);
        Date endDate = new Date(System.currentTimeMillis() + 86400000);
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            testGame.getId(),
            startDate,
            endDate
        );

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ----- UPDATE Tests (3 tests) -----

    @Test
    @Order(5)
    public void testUpdateBorrowRequestStatusSuccess() {
        // Use the parameterized constructor to create an updated DTO.
        BorrowRequestDto updateDto = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED",
            testRequest.getRequestDate()
        );

        ResponseEntity<BorrowRequestDto> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(updateDto),
            BorrowRequestDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto updatedDto = response.getBody();
        assertNotNull(updatedDto);
        assertEquals("APPROVED", updatedDto.getStatus());
    }

    @Test
    @Order(6)
    public void testUpdateBorrowRequestWithInvalidStatus() {
        // Use an invalid status value.
        BorrowRequestDto updateDto = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "INVALID_STATUS",
            testRequest.getRequestDate()
        );

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.PUT,
            new HttpEntity<>(updateDto),
            String.class
        );
        // Controller catches exception and returns NOT_FOUND
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentBorrowRequest() {
        BorrowRequestDto updateDto = new BorrowRequestDto(
            999,
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED",
            testRequest.getRequestDate()
        );

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.PUT,
            new HttpEntity<>(updateDto),
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ----- DELETE Tests (3 tests) -----

    @Test
    @Order(8)
    public void testDeleteBorrowRequestSuccess() {
        ResponseEntity<Void> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.DELETE,
            null,
            Void.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(borrowRequestRepository.findById(testRequest.getId()).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentBorrowRequest() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/999"),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void testDeleteBorrowRequestTwice() {
        ResponseEntity<Void> response1 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.DELETE,
            null,
            Void.class
        );
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        ResponseEntity<String> response2 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
    }

    @Test
    @Order(11)
    public void testGetBorrowRequestByIdSuccess() {
        ResponseEntity<BorrowRequestDto> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testRequest.getId()),
            BorrowRequestDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto dto = response.getBody();
        assertNotNull(dto);
        assertEquals(testRequest.getId(), dto.getId());
        assertEquals(testRequester.getId(), dto.getRequesterId());
        assertEquals(testGame.getId(), dto.getRequestedGameId());
    }

    @Test
    @Order(12)
    public void testGetNonExistentBorrowRequestById() {
        ResponseEntity<String> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/999"),
            String.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(13)
    public void testGetAllBorrowRequests() {
        ResponseEntity<BorrowRequestDto[]> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL),
            BorrowRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
    }

    @Test
    @Order(14)
    public void testGetBorrowRequestsByStatusSuccess() {
        ResponseEntity<BorrowRequestDto[]> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/status/PENDING"),
            BorrowRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto[] requests = response.getBody();
        assertNotNull(requests);
        assertTrue(requests.length > 0);
        for (BorrowRequestDto request : requests) {
            assertEquals("PENDING", request.getStatus());
        }
    }

    @Test
    @Order(15)
    public void testGetBorrowRequestsByStatusNoResults() {
        ResponseEntity<BorrowRequestDto[]> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/status/APPROVED"),
            BorrowRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto[] requests = response.getBody();
        assertNotNull(requests);
        assertEquals(0, requests.length); // Should be empty since we only created "PENDING" requests
    }

    @Test
    @Order(16)
    public void testGetBorrowRequestsByRequesterSuccess() {
        ResponseEntity<BorrowRequestDto[]> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/requester/" + testRequester.getId()),
            BorrowRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto[] requests = response.getBody();
        assertNotNull(requests);
        assertTrue(requests.length > 0);
        for (BorrowRequestDto request : requests) {
            assertEquals(testRequester.getId(), request.getRequesterId());
        }
    }

    @Test
    @Order(17)
    public void testGetBorrowRequestsByRequesterNoResults() {
        ResponseEntity<BorrowRequestDto[]> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/requester/999"), // Non-existent user ID
            BorrowRequestDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        BorrowRequestDto[] requests = response.getBody();
        assertNotNull(requests);
        assertEquals(0, requests.length); // Should be empty since user ID 999 has no requests
    }

}
