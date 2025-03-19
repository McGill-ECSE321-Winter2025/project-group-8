package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.SecurityTestConfig;
import ca.mcgill.ecse321.gameorganizer.dto.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.dto.LendingRecordResponseDto;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateLendingRecordStatusDto;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityTestConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LendingRecordIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private LendingRecordRepository lendingRecordRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    private GameOwner testOwner;
    private Account testBorrower;
    private Game dummyGame;             // Dummy game for borrow requests
    private BorrowRequest dummyRequest; // Dummy borrow request used in setup (already associated with a record)
    private LendingRecord testRecord;
    
    // Base URL for lending record endpoints
    private static final String BASE_URL = "/api/v1/lending-records";
    
    
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
    
    @BeforeEach
    public void setup() {
        // Clean up repositories
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        accountRepository.deleteAll();
        gameRepository.deleteAll();

        // Create test game owner
        testOwner = new GameOwner("Owner", "owner@example.com", "pass");
        testOwner = (GameOwner) accountRepository.save(testOwner);

        // Create test borrower
        testBorrower = new Account("Borrower", "borrower@example.com", "pass");
        testBorrower = accountRepository.save(testBorrower);
        
        // Create a dummy game and set its owner to testOwner
        dummyGame = new Game();
        dummyGame.setName("Dummy Game");
        // Set any additional required properties if necessary
        dummyGame.setOwner(testOwner); // Important!
        dummyGame = gameRepository.save(dummyGame);
        
        // Create a dummy BorrowRequest and set its requestedGame and requester
        dummyRequest = new BorrowRequest();
        dummyRequest.setRequestedGame(dummyGame);
        dummyRequest.setRequester(testBorrower);
        dummyRequest = borrowRequestRepository.save(dummyRequest);
        
        // Create a test lending record that uses the dummy request.
        testRecord = new LendingRecord();
        testRecord.setStartDate(new Date(System.currentTimeMillis() - 86400000L)); // Yesterday
        testRecord.setEndDate(new Date(System.currentTimeMillis() + 86400000L));   // Tomorrow
        testRecord.setStatus(LendingStatus.ACTIVE);
        testRecord.setRecordOwner(testOwner);
        testRecord.setRequest(dummyRequest);
        testRecord = lendingRecordRepository.save(testRecord);
    }
    
    @AfterEach
    public void cleanup() {
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }
    
    // ============================================================
    // CREATE Tests (4 tests)
    // ============================================================
    
    
    @Test
    @Order(1)
    public void testCreateLendingRecordSuccess() {
        // Create a new borrow request so we don't conflict with dummyRequest already used in testRecord.
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);
        
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        long startDate = now + 10800000L; // 3 hours from now
        long endDate = now + 86400000L;   // 1 day from now
        requestMap.put("startDate", startDate);
        requestMap.put("endDate", endDate);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        
        System.out.println("Response Body: " + response.getBody());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        String responseBody = response.getBody();
        assertNotNull(responseBody);
        // Check that the response string contains the "id" field (case-insensitive)
        assertTrue(responseBody.toLowerCase().contains("\"id\""), "Response body does not contain 'id'");
    }
    
    
    @Test
    @Order(2)
    public void testCreateLendingRecordMissingRequestId() {
        Map<String, Object> requestMap = new HashMap<>();
        // Omit "requestId"
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    
    @Test
    @Order(3)
    public void testCreateLendingRecordWithInvalidDates() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        // Set startDate later than endDate
        requestMap.put("startDate", now + 86400000L);
        requestMap.put("endDate", now);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    
    @Test
    @Order(4)
    public void testCreateLendingRecordWithNonExistentOwner() {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        // Use an ownerId that does not exist
        requestMap.put("ownerId", 99999);
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ============================================================
    // UPDATE Tests (3 tests)
    // ============================================================
    
    
    @Test
    @Order(5)
    public void testUpdateEndDateSuccess() {
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/end-date"),
            HttpMethod.PUT,
            new HttpEntity<>(newEndDate),
            String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    
    @Test
    @Order(6)
    public void testUpdateEndDateWithInvalidDate() {
        Date invalidEndDate = new Date(System.currentTimeMillis() - 2 * 86400000L);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/end-date"),
            HttpMethod.PUT,
            new HttpEntity<>(invalidEndDate),
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    
    @Test
    @Order(7)
    public void testUpdateNonExistentLendingRecord() {
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/99999/end-date"),
            HttpMethod.PUT,
            new HttpEntity<>(newEndDate),
            String.class
        );
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
    
    // ============================================================
    // DELETE Tests (3 tests)
    // ============================================================
    
    
    @Test
    @Order(8)
    public void testDeleteLendingRecordSuccess() {
        // First, close the record via the confirm-return endpoint.
        ResponseEntity<String> closeResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, closeResponse.getStatusCode(), "Closing the record failed");
        
        ResponseEntity<String> deleteResponse = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertFalse(lendingRecordRepository.findById(testRecord.getId()).isPresent());
    }
    
    
    @Test
    @Order(9)
    public void testDeleteNonExistentLendingRecord() {
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/99999"),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    
    @Test
    @Order(10)
    public void testDeleteLendingRecordTwice() {
        // First, close the record via confirm-return.
        ResponseEntity<String> closeResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, closeResponse.getStatusCode(), "Closing the record failed");
        
        // First deletion attempt
        ResponseEntity<String> response1 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, response1.getStatusCode(), "First deletion did not return OK");
        
        // Second deletion attempt should return NOT_FOUND
        ResponseEntity<String> response2 = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId()),
            HttpMethod.DELETE,
            null,
            String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response2.getStatusCode(), "Second deletion did not return NOT_FOUND");
    }

    // ============================================================
    // GET Tests
    // ============================================================

    @Test
    @Order(11)
    public void testGetAllLendingRecords() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));
        assertTrue(response.getBody().containsKey("totalItems"));
        assertTrue(response.getBody().containsKey("currentPage"));
    }

    @Test
    @Order(12)
    public void testGetLendingRecordById() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId()),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testRecord.getId(), ((Number)response.getBody().get("id")).intValue());
    }

    @Test
    @Order(13)
    public void testGetLendingRecordsByOwner() {
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId()),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        Map<String, Object> record = (Map<String, Object>) response.getBody().get(0);
        assertEquals(testRecord.getId(), ((Number)record.get("id")).intValue());
    }

    @Test
    @Order(14)
    public void testGetLendingRecordsByOwnerAndStatus() {
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/status/ACTIVE"),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        Map<String, Object> record = (Map<String, Object>) response.getBody().get(0);
        assertEquals("ACTIVE", record.get("status"));
    }

    @Test
    @Order(15)
    public void testGetLendingRecordsByBorrower() {
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/borrower/" + testBorrower.getId()),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        Map<String, Object> record = (Map<String, Object>) response.getBody().get(0);
        Map<String, Object> borrower = (Map<String, Object>) record.get("borrower");
        assertEquals(testBorrower.getId(), ((Number)borrower.get("id")).intValue());
    }

    @Test
    @Order(16)
    public void testGetActiveLendingRecordsByBorrower() {
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/borrower/" + testBorrower.getId() + "/active"),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
        Map<String, Object> record = (Map<String, Object>) response.getBody().get(0);
        assertEquals("ACTIVE", record.get("status"));
    }

    @Test
    @Order(17)
    public void testFilterLendingRecords() {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/filter"),
            filterDto,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));
    }

    @Test
    @Order(18)
    public void testGetOverdueRecords() {
        // First create an overdue record
        LendingRecord overdueRecord = new LendingRecord();
        overdueRecord.setStartDate(new Date(System.currentTimeMillis() - 2 * 86400000L)); // 2 days ago
        overdueRecord.setEndDate(new Date(System.currentTimeMillis() - 86400000L));      // Yesterday
        overdueRecord.setStatus(LendingStatus.ACTIVE);  // Still active but end date has passed
        overdueRecord.setRecordOwner(testOwner);
        
        // Create a new borrow request for this overdue record
        BorrowRequest overdueRequest = new BorrowRequest();
        overdueRequest.setRequestedGame(dummyGame);
        overdueRequest.setRequester(testBorrower);
        overdueRequest = borrowRequestRepository.save(overdueRequest);
        
        overdueRecord.setRequest(overdueRequest);
        overdueRecord = lendingRecordRepository.save(overdueRecord);
        
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/overdue"),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    @Test
    @Order(19)
    public void testGetOverdueRecordsByOwner() {
        // First create an overdue record
        LendingRecord overdueRecord = new LendingRecord();
        overdueRecord.setStartDate(new Date(System.currentTimeMillis() - 2 * 86400000L)); // 2 days ago
        overdueRecord.setEndDate(new Date(System.currentTimeMillis() - 86400000L));      // Yesterday
        overdueRecord.setStatus(LendingStatus.ACTIVE);  // Still active but end date has passed
        overdueRecord.setRecordOwner(testOwner);
        
        // Create a new borrow request for this overdue record
        BorrowRequest overdueRequest = new BorrowRequest();
        overdueRequest.setRequestedGame(dummyGame);
        overdueRequest.setRequester(testBorrower);
        overdueRequest = borrowRequestRepository.save(overdueRequest);
        
        overdueRecord.setRequest(overdueRequest);
        overdueRecord = lendingRecordRepository.save(overdueRecord);
        
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/overdue"),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }

    // ============================================================
    // UPDATE Status Tests
    // ============================================================

    @Test
    @Order(20)
    public void testUpdateLendingRecordStatus() {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Game is overdue");
        
        ResponseEntity<Map> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/status"),
            HttpMethod.PUT,
            new HttpEntity<>(statusDto),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(LendingStatus.OVERDUE.name(), response.getBody().get("newStatus"));
    }

    @Test
    @Order(21)
    public void testInvalidStatusTransition() {
        // First, close the record
        ResponseEntity<String> closeResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, closeResponse.getStatusCode(), "Closing the record failed");
        
        // Now try to change from CLOSED to ACTIVE, which should be invalid
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.ACTIVE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Trying invalid transition");
        
        ResponseEntity<Map> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/status"),
            HttpMethod.PUT,
            new HttpEntity<>(statusDto),
            Map.class
        );
        
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    @Order(22)
    public void testMarkGameAsReturned() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/mark-returned?userId=" + testBorrower.getId()),
            null,
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        // The status should now be OVERDUE which is used as placeholder for "Pending Return Confirmation"
        LendingRecord updatedRecord = lendingRecordRepository.findById(testRecord.getId()).orElse(null);
        assertNotNull(updatedRecord);
        assertEquals(LendingStatus.OVERDUE, updatedRecord.getStatus());
    }

    @Test
    @Order(23)
    public void testConfirmGameReturn() {
        Map<String, Object> params = new HashMap<>();
        params.put("isDamaged", true);
        params.put("damageNotes", "The game box is damaged");
        params.put("damageSeverity", 2);
        params.put("userId", testOwner.getId());
        
        String url = createURLWithPort(BASE_URL + "/" + testRecord.getId() + 
                      "/confirm-return?isDamaged=true&damageNotes=The game box is damaged&damageSeverity=2&userId=" + 
                      testOwner.getId());
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertTrue((Boolean) response.getBody().get("isDamaged"));
        assertEquals(2, response.getBody().get("damageSeverity"));
        
        // Verify the record status is now CLOSED
        LendingRecord updatedRecord = lendingRecordRepository.findById(testRecord.getId()).orElse(null);
        assertNotNull(updatedRecord);
        assertEquals(LendingStatus.CLOSED, updatedRecord.getStatus());
    }
}
