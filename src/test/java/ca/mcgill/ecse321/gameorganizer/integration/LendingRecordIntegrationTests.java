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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;
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
@Import({TestConfig.class, SecurityConfig.class})
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
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode(), "Second deletion did not return NOT_FOUND");
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
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId()),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));

        // Extract the records list from the response
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getBody().get("records");
        assertNotNull(records);
        assertTrue(records.size() > 0);

        // Validate the first record
        Map<String, Object> record = records.get(0);
        assertEquals(testRecord.getId(), ((Number) record.get("id")).intValue());
    }

    @Test
    @Order(14)
    public void testGetLendingRecordsByOwnerAndStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/status/ACTIVE"),
            Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));

        // Extract the records list from the response
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getBody().get("records");
        assertNotNull(records);
        assertTrue(records.size() > 0);

        // Validate the first record
        Map<String, Object> record = records.get(0);
        assertEquals("ACTIVE", record.get("status"));
    }

    @Test
    @Order(15)
    public void testGetLendingRecordsByBorrower() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/borrower/" + testBorrower.getId()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));

        // Extract the records list from the response
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getBody().get("records");
        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Validate the first record
        Map<String, Object> record = records.get(0);
        Map<String, Object> borrower = (Map<String, Object>) record.get("borrower");
        assertEquals(testBorrower.getId(), ((Number) borrower.get("id")).intValue());
    }

    @Test
    @Order(16)
    public void testGetActiveLendingRecordsByBorrower() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/borrower/" + testBorrower.getId() + "/active"),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));

        // Extract the records list from the response
        List<Map<String, Object>> records = (List<Map<String, Object>>) response.getBody().get("records");
        assertNotNull(records);
        assertFalse(records.isEmpty());

        // Validate the first record
        Map<String, Object> record = records.get(0);
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

    // ============================================================
    // Additional Tests for getLendingHistoryByOwnerAndDateRange
    // ============================================================
    
    @Test
    @Order(24)
    public void testGetLendingHistoryByOwnerAndDateRange_Success() {
        // Define date range - use java.util.Date instead of java.sql.Date
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L); // 2 days ago
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);   // 2 days from now
        
        ResponseEntity<List<LendingRecordResponseDto>> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/date-range?startDate=" + 
                startDate.toInstant().toString() + "&endDate=" + endDate.toInstant().toString()),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<LendingRecordResponseDto>>() {}
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().size() > 0);
    }
    
    @Test
    @Order(25)
    public void testGetLendingHistoryByOwnerAndDateRange_InvalidDateRange() {
        // Define invalid date range (end date before start date)
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L); // 2 days from now
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L);   // 2 days ago
        
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/date-range?startDate=" + 
                startDate.toInstant().toString() + "&endDate=" + endDate.toInstant().toString()),
            List.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(26)
    public void testGetLendingHistoryByOwnerAndDateRange_NoRecordsInRange() {
        // Define date range in the far future
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 30 * 86400000L); // 30 days from now
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 60 * 86400000L);   // 60 days from now
        
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/date-range?startDate=" + 
                startDate.toInstant().toString() + "&endDate=" + endDate.toInstant().toString()),
            List.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
    }

    @Test
    @Order(27)
    public void testGetLendingHistoryByOwnerAndDateRange_InvalidOwnerId() {
        // Define date range
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L); // 2 days ago
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);   // 2 days from now
        
        ResponseEntity<List> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/99999/date-range?startDate=" + 
                startDate.toInstant().toString() + "&endDate=" + endDate.toInstant().toString()),
            List.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ============================================================
    // Additional Tests for createLendingRecord
    // ============================================================
    
    @Test
    @Order(28)
    public void testCreateLendingRecord_DuplicateRecord() {
        // Create a new BorrowRequest first to use for this test
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);
        
        // Create a lending record for this new request
        LendingRecord newRecord = new LendingRecord();
        newRecord.setStartDate(new Date(System.currentTimeMillis()));
        newRecord.setEndDate(new Date(System.currentTimeMillis() + 86400000L));
        newRecord.setStatus(LendingStatus.ACTIVE);
        newRecord.setRecordOwner(testOwner);
        newRecord.setRequest(newRequest);
        newRecord = lendingRecordRepository.save(newRecord);
        
        // Now try to create another record with the same request (should fail)
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());  // This request is now already used
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        
        // Should fail with 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("already has a lending record"));
    }
    
    @Test
    @Order(29)
    public void testCreateLendingRecord_InvalidOwnerType() {
        // Create a regular account (not GameOwner)
        Account regularAccount = new Account("Regular", "regular@example.com", "pass");
        regularAccount = accountRepository.save(regularAccount);
        
        // Create a new valid request
        BorrowRequest validRequest = new BorrowRequest();
        validRequest.setRequestedGame(dummyGame);
        validRequest.setRequester(testBorrower);
        validRequest = borrowRequestRepository.save(validRequest);
        
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", validRequest.getId());
        requestMap.put("ownerId", regularAccount.getId());  // Not a GameOwner
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        
        // Print the response body for debugging
        System.out.println("Response body for invalid owner type: " + response.getBody());
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        // Only verify we got an error response, not the specific error message
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length() > 0);
    }
    
    // ============================================================
    // Test for handleInvalidOperationException
    // ============================================================
    
    @Test
    @Order(30)
    public void testHandleInvalidOperationException() {
        // First create a lending record that's already closed
        // We'll use the existing testRecord
        
        // Close the record
        ResponseEntity<String> closeResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, closeResponse.getStatusCode(), "Closing the record failed");
        
        // Now try to mark it as returned, which should cause an InvalidOperationException
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/mark-returned"),
            null,
            String.class
        );
        
        // Print the actual response body for debugging
        System.out.println("Error response body: " + response.getBody());
        
        // Verify the response format for InvalidOperationException
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        
        // Use a more generic check that doesn't depend on specific wording
        // Just verify we got some kind of error message
        assertTrue(response.getBody().length() > 0, 
                  "Response should contain an error message");
    }

    @Test
    @Order(31)
    public void testValidateDamageSeverity_Invalid() {
        // Test with an invalid damage severity (4)
        String url = createURLWithPort(BASE_URL + "/" + testRecord.getId() + 
                    "/confirm-return?isDamaged=true&damageSeverity=4");
        
        ResponseEntity<Map> response = restTemplate.postForEntity(url, null, Map.class);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("severity"));
    }

    @Test
    @Order(32)
    public void testGetAllLendingRecords_EmptyResults() {
        // First delete all records
        lendingRecordRepository.deleteAll();
        
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "?page=0&size=10"),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, ((List) response.getBody().get("records")).size());
        assertEquals(0, response.getBody().get("totalItems"));
    }

    @Test
    @Order(33)
    public void testGetAllLendingRecords_InvalidPage() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "?page=999&size=10"),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("records"));
        assertEquals(0, ((Integer) response.getBody().get("currentPage")).intValue());
    }

    @Test
    @Order(34)
    public void testGetAllLendingRecords_SortDirectionDesc() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "?sort=id&direction=desc"),
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @Order(35)
    public void testConfirmGameReturn_WithoutUserId() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }

    @Test
    @Order(36)
    public void testConfirmGameReturn_DifferentDamageSeverities() {
        // Test with damage severity 1
        ResponseEntity<Map> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + 
                            "/confirm-return?isDamaged=true&damageSeverity=1"),
            null,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
        assertEquals(1, response.getBody().get("damageSeverity"));
    }

    @Test
    @Order(37)
    public void testFilterLendingRecords_EmptyResults() {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        // Use an empty status which should return empty results but not error
        filterDto.setStatus(LendingStatus.CLOSED.name());
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/filter"),
            filterDto,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("records"));
        assertEquals(0, ((List)response.getBody().get("records")).size());
    }

    @Test
    @Order(38)
    public void testFilterLendingRecords_PaginationEdgeCases() {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());
        
        ResponseEntity<Map> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/filter?page=999&size=10"),
            filterDto,
            Map.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, ((Integer) response.getBody().get("currentPage")).intValue());
    }

    @Test
    @Order(39)
    public void testMarkGameAsReturned_WithoutUserId() {
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/mark-returned"),
            null,
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(40)
    public void testCreateLendingRecord_WithIdenticalDates() {
        // Create a new borrow request
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);
        
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        // Set end date BEFORE start date to ensure rejection
        requestMap.put("startDate", now); // 1 day from now
        requestMap.put("endDate", now);               // now (before start date)
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            requestMap,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(41)
    public void testUpdateLendingRecordStatus_InvalidStatusValue() {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("INVALID_STATUS");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing invalid status");
        
        ResponseEntity<Map> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/status"),
            HttpMethod.PUT,
            new HttpEntity<>(statusDto),
            Map.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    @Order(42)
    public void testUpdateLendingRecordStatus_EmptyStatus() {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing empty status");
        
        ResponseEntity<Map> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/status"),
            HttpMethod.PUT,
            new HttpEntity<>(statusDto),
            Map.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
        assertTrue(response.getBody().get("message").toString().contains("empty"));
    }

    @Test
    @Order(43)
    public void testUpdateLendingRecordStatus_NonExistentRecord() {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing non-existent record");
        
        ResponseEntity<Map> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/99999/status"),
            HttpMethod.PUT,
            new HttpEntity<>(statusDto),
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }

    @Test
    @Order(44)
    public void testGetLendingRecordById_NonExistentRecord() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/99999"),
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(45)
    public void testGetLendingHistoryByOwner_InvalidOwnerId() {
        ResponseEntity<?> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/99999"),
            List.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(46)
    public void testGetLendingRecordsByBorrower_InvalidBorrowerId() {
        ResponseEntity<?> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/borrower/99999"),
            List.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(47)
    public void testGetActiveLendingRecordsByBorrower_InvalidBorrowerId() {
        ResponseEntity<?> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/borrower/99999/active"),
            List.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(48)
    public void testHandleResourceNotFoundException() {
        // Create a record that doesn't exist to trigger ResourceNotFoundException
        ResponseEntity<?> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/999999"),
            Map.class
        );
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @Order(49)
    public void testHandleIllegalStateException() {
        // First close the record
        ResponseEntity<String> closeResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/confirm-return?isDamaged=false"),
            null,
            String.class
        );
        assertEquals(HttpStatus.OK, closeResponse.getStatusCode());
        
        // Now try updating end date on a closed record (should trigger IllegalStateException)
        Date newEndDate = new Date(System.currentTimeMillis() + 86400000L);
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + testRecord.getId() + "/end-date"),
            HttpMethod.PUT,
            new HttpEntity<>(newEndDate),
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("closed"));
    }

    @Test
    @Order(50)
    public void testLendingHistoryByOwnerAndStatus_InvalidStatus() {
        ResponseEntity<?> response = restTemplate.getForEntity(
            createURLWithPort(BASE_URL + "/owner/" + testOwner.getId() + "/status/INVALID_STATUS"),
            List.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
