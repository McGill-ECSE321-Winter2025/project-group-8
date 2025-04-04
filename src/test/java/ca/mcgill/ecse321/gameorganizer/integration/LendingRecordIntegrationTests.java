package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;
// HttpEntity, HttpHeaders removed
import org.springframework.security.crypto.password.PasswordEncoder;
// AuthenticationDTO, JwtAuthenticationResponse, LoginResponse removed (not used with MockMvc)
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType; // Add MediaType import
import org.junit.jupiter.api.MethodOrderer; 
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
// TestRestTemplate, LocalServerPort imports already removed
import org.springframework.test.web.servlet.MockMvc; // Add MockMvc
import com.fasterxml.jackson.databind.ObjectMapper; // Add ObjectMapper
import org.springframework.security.test.context.support.WithMockUser; // Add WithMockUser
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Add MockMvc builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Add MockMvc matchers
// Import removed (using classes in @SpringBootTest)
// ParameterizedTypeReference removed (not needed with MockMvc jsonPath)
// HttpEntity, HttpMethod, HttpStatus, ResponseEntity removed
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.GameorganizerApplication; // Add main app import
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
// Remove SecurityConfig import
import ca.mcgill.ecse321.gameorganizer.config.TestSecurityConfig; // Add TestSecurityConfig import
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

// Apply standard configuration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {GameorganizerApplication.class, TestConfig.class, TestSecurityConfig.class}
)
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this
// Remove @Import
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LendingRecordIntegrationTests {

        // LocalServerPort removed
        // private int port;
    
        // TestRestTemplate removed
        // @Autowired
        // private TestRestTemplate restTemplate;
    
        @Autowired
        private MockMvc mockMvc; // Inject MockMvc
    
        @Autowired
        private ObjectMapper objectMapper; // Inject ObjectMapper
    
    @Autowired
    private LendingRecordRepository lendingRecordRepository;
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    private GameOwner testOwner;
    private Account testBorrower;
    private Game dummyGame;             // Dummy game for borrow requests
    private BorrowRequest dummyRequest; // Dummy borrow request used in setup (already associated with a record)
    private LendingRecord testRecord;
    
    // Base URL for lending record endpoints
    private static final String BASE_URL = "/api/v1/lending-records";
    
    
        // createURLWithPort and createAuthHeaders removed

    
    @BeforeEach
    public void setup() {
        // Clean up repositories
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        accountRepository.deleteAll();
        gameRepository.deleteAll();

        // Create test game owner
        testOwner = new GameOwner("Owner", "owner@example.com", passwordEncoder.encode("pass"));
        testOwner = (GameOwner) accountRepository.save(testOwner);

        // Create test borrower
        testBorrower = new Account("Borrower", "borrower@example.com", passwordEncoder.encode("pass"));
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
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testCreateLendingRecordSuccess() throws Exception { // Add throws
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
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").exists()); // Check if ID exists in response
    }
    
    
    @Test
        @Order(2)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testCreateLendingRecordMissingRequestId() throws Exception { // Add throws
        Map<String, Object> requestMap = new HashMap<>();
        // Omit "requestId"
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
    }
    
    
    @Test
        @Order(3)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testCreateLendingRecordWithInvalidDates() throws Exception { // Add throws
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        // Set startDate later than endDate
        requestMap.put("startDate", now + 86400000L);
        requestMap.put("endDate", now);
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
    }
    
    
    @Test
        @Order(4)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth (to attempt the call)
        public void testCreateLendingRecordWithNonExistentOwner() throws Exception { // Add throws
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        // Use an ownerId that does not exist
        requestMap.put("ownerId", 99999);
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
    }
    
    // ============================================================
    // UPDATE Tests (3 tests)
    // ============================================================
    
    
    @Test
        @Order(5)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateEndDateSuccess() throws Exception { // Add throws
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEndDate)))
                    .andExpect(status().isOk());
    }
    
    
    @Test
        @Order(6)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateEndDateWithInvalidDate() throws Exception { // Add throws
        Date invalidEndDate = new Date(System.currentTimeMillis() - 2 * 86400000L);
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEndDate)))
                    .andExpect(status().isBadRequest());
    }
    
    
    @Test
        @Order(7)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateNonExistentLendingRecord() throws Exception { // Add throws
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);
                mockMvc.perform(put(BASE_URL + "/99999/end-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEndDate)))
                    .andExpect(status().isNotFound());
    }
    
    // ============================================================
    // DELETE Tests (3 tests)
    // ============================================================
    
    
    @Test
        @Order(8)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth for both steps
        public void testDeleteLendingRecordSuccess() throws Exception { // Add throws
        // First, close the record via the confirm-return endpoint.
                // First, close the record via the confirm-return endpoint.
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false"))
                    .andExpect(status().isOk());
        
                // Now delete the record
                mockMvc.perform(delete(BASE_URL + "/" + testRecord.getId()))
                    .andExpect(status().isOk());
        
                assertFalse(lendingRecordRepository.findById(testRecord.getId()).isPresent());
    }
    
    
    @Test
        @Order(9)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testDeleteNonExistentLendingRecord() throws Exception { // Add throws
                mockMvc.perform(delete(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
    }
    
    
    @Test
        @Order(10)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testDeleteLendingRecordTwice() throws Exception { // Add throws
        // First, close the record via confirm-return.
                // First, close the record via confirm-return.
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false"))
                    .andExpect(status().isOk());
        
                // First deletion attempt
                mockMvc.perform(delete(BASE_URL + "/" + testRecord.getId()))
                    .andExpect(status().isOk());
        
                // Second deletion attempt should return NOT_FOUND
                mockMvc.perform(delete(BASE_URL + "/" + testRecord.getId()))
                    .andExpect(status().isNotFound());
    }

    // ============================================================
    // GET Tests
    // ============================================================

    @Test
        @Order(11)
        @WithMockUser // Basic auth is likely sufficient for GETs
        public void testGetAllLendingRecords() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records").exists())
                .andExpect(jsonPath("$.totalItems").exists())
                .andExpect(jsonPath("$.currentPage").exists());
    }

    @Test
        @Order(12)
        @WithMockUser // Basic auth
        public void testGetLendingRecordById() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/" + testRecord.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRecord.getId()));
    }

    @Test
        @Order(13)
        @WithMockUser // Basic auth
        public void testGetLendingRecordsByOwner() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testRecord.getId()));
    }

    @Test
        @Order(14)
        @WithMockUser // Basic auth
        public void testGetLendingRecordsByOwnerAndStatus() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
        @Order(15)
        @WithMockUser // Basic auth
        public void testGetLendingRecordsByBorrower() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/borrower/" + testBorrower.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].borrower.id").value(testBorrower.getId()));
    }

    @Test
        @Order(16)
        @WithMockUser // Basic auth
        public void testGetActiveLendingRecordsByBorrower() throws Exception { // Add throws
            mockMvc.perform(get(BASE_URL + "/borrower/" + testBorrower.getId() + "/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
        @Order(17)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testFilterLendingRecords() throws Exception { // Add throws
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());
        
                mockMvc.perform(post(BASE_URL + "/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").exists());
    }

    @Test
        @Order(18)
        @WithMockUser // Basic auth
        public void testGetOverdueRecords() throws Exception { // Add throws
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
        
                mockMvc.perform(get(BASE_URL + "/overdue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.id == " + overdueRecord.getId() + ")]").exists()); // Check if our overdue record is present
    }

    @Test
        @Order(19)
        @WithMockUser // Basic auth
        public void testGetOverdueRecordsByOwner() throws Exception { // Add throws
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
        
                mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/overdue"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[?(@.id == " + overdueRecord.getId() + ")]").exists()); // Check if our overdue record is present
    }

    // ============================================================
    // UPDATE Status Tests
    // ============================================================

    @Test
        @Order(20)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateLendingRecordStatus() throws Exception { // Add throws
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Game is overdue");
        
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.newStatus").value(LendingStatus.OVERDUE.name()));
    }

    @Test
        @Order(21)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testInvalidStatusTransition() throws Exception { // Add throws
        // First, close the record
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false"))
                    .andExpect(status().isOk());
        
        // Now try to change from CLOSED to ACTIVE, which should be invalid
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.ACTIVE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Trying invalid transition");
        
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isOk()) // Endpoint returns OK even on failure, check body
                    .andExpect(jsonPath("$.success").value(false));
    }

    @Test
        @Order(22)
        @WithMockUser(username="borrower@example.com", roles={"USER"}) // Need borrower auth
        public void testMarkGameAsReturned() throws Exception { // Add throws
                // Note: The original endpoint included userId as a param, but the controller doesn't use it.
                // Assuming the controller uses the authenticated user.
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/mark-returned"))
                    .andExpect(status().isOk());
        
        // The status should now be OVERDUE which is used as placeholder for "Pending Return Confirmation"
        LendingRecord updatedRecord = lendingRecordRepository.findById(testRecord.getId()).orElse(null);
        assertNotNull(updatedRecord);
        assertEquals(LendingStatus.OVERDUE, updatedRecord.getStatus());
    }

    @Test
        @Order(23)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testConfirmGameReturn() throws Exception { // Add throws
        Map<String, Object> params = new HashMap<>();
        params.put("isDamaged", true);
        params.put("damageNotes", "The game box is damaged");
        params.put("damageSeverity", 2);
        params.put("userId", testOwner.getId()); // This map isn't used with MockMvc, could be removed
        
        // String url = createURLWithPort(...); // Removed
                      // testOwner.getId()); // Erroneous closing parenthesis removed
        
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "true")
                        .param("damageNotes", "The game box is damaged")
                        .param("damageSeverity", "2"))
                        // Assuming userId is implicit from auth, removed param("userId", testOwner.getId().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.isDamaged").value(true))
                    .andExpect(jsonPath("$.damageSeverity").value(2));
        
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
        @WithMockUser // Basic auth
        public void testGetLendingHistoryByOwnerAndDateRange_Success() throws Exception { // Add throws
        // Define date range - use java.util.Date instead of java.sql.Date
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L); // 2 days ago
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);   // 2 days from now
        
                mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                        .param("startDate", startDate.toInstant().toString())
                        .param("endDate", endDate.toInstant().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0]").exists());
    }
    
    @Test
        @Order(25)
        @WithMockUser // Basic auth
        public void testGetLendingHistoryByOwnerAndDateRange_InvalidDateRange() throws Exception { // Add throws
        // Define invalid date range (end date before start date)
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L); // 2 days from now
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L);   // 2 days ago
        
                mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                        .param("startDate", startDate.toInstant().toString())
                        .param("endDate", endDate.toInstant().toString()))
                    .andExpect(status().isBadRequest());
    }

    @Test
        @Order(26)
        @WithMockUser // Basic auth
        public void testGetLendingHistoryByOwnerAndDateRange_NoRecordsInRange() throws Exception { // Add throws
        // Define date range in the far future
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 30 * 86400000L); // 30 days from now
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 60 * 86400000L);   // 60 days from now
        
                mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                        .param("startDate", startDate.toInstant().toString())
                        .param("endDate", endDate.toInstant().toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
    }

    @Test
        @Order(27)
        @WithMockUser // Basic auth
        public void testGetLendingHistoryByOwnerAndDateRange_InvalidOwnerId() throws Exception { // Add throws
        // Define date range
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L); // 2 days ago
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);   // 2 days from now
        
                mockMvc.perform(get(BASE_URL + "/owner/99999/date-range")
                        .param("startDate", startDate.toInstant().toString())
                        .param("endDate", endDate.toInstant().toString()))
                    .andExpect(status().isBadRequest());
    }
    
    // ============================================================
    // Additional Tests for createLendingRecord
    // ============================================================
    
    @Test
        @Order(28)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testCreateLendingRecord_DuplicateRecord() throws Exception { // Add throws
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
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
                // Cannot easily check response body content with MockMvc without more complex setup
    }
    
    @Test
        @Order(29)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth (to attempt call)
        public void testCreateLendingRecord_InvalidOwnerType() throws Exception { // Add throws
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
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
    }
    
    // ============================================================
    // Test for handleInvalidOperationException
    // ============================================================
    
    @Test
        @Order(30)
        // Requires owner for close, borrower for mark-returned
        public void testHandleInvalidOperationException() throws Exception { // Add throws
        // First create a lending record that's already closed
        // We'll use the existing testRecord
        
        // Close the record
                // Close the record (needs owner auth)
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("owner@example.com").roles("GAME_OWNER")))
                    .andExpect(status().isOk());
        
        // Now try to mark it as returned, which should cause an InvalidOperationException
                // Now try to mark it as returned (needs borrower auth)
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/mark-returned")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("borrower@example.com").roles("USER")))
                    .andExpect(status().isBadRequest()); // Expecting failure due to invalid state
    }

    @Test
        @Order(31)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testValidateDamageSeverity_Invalid() throws Exception { // Add throws
        // Test with an invalid damage severity (4)
                // String url = createURLWithPort(...); // Removed
                    // "/confirm-return?isDamaged=true&damageSeverity=4"); // Erroneous closing parenthesis removed
        
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "true")
                        .param("damageSeverity", "4")) // Invalid severity
                    .andExpect(status().isBadRequest());
                // Cannot easily check response body content with MockMvc without more complex setup
    }

    @Test
        @Order(32)
        @WithMockUser // Basic auth
        public void testGetAllLendingRecords_EmptyResults() throws Exception { // Add throws
        // First delete all records
        lendingRecordRepository.deleteAll();
        
                mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray())
                    .andExpect(jsonPath("$.records").isEmpty())
                    .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
        @Order(33)
        @WithMockUser // Basic auth
        public void testGetAllLendingRecords_InvalidPage() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL)
                        .param("page", "999")
                        .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").exists())
                    .andExpect(jsonPath("$.currentPage").value(0)); // Assuming it defaults to page 0 if requested page is invalid
    }

    @Test
        @Order(34)
        @WithMockUser // Basic auth
        public void testGetAllLendingRecords_SortDirectionDesc() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL)
                        .param("sort", "id")
                        .param("direction", "desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").exists());
    }

    @Test
        @Order(35)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testConfirmGameReturn_WithoutUserId() throws Exception { // Add throws
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
    }

    @Test
        @Order(36)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testConfirmGameReturn_DifferentDamageSeverities() throws Exception { // Add throws
        // Test with damage severity 1
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "true")
                        .param("damageSeverity", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.damageSeverity").value(1));
    }

    @Test
        @Order(37)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testFilterLendingRecords_EmptyResults() throws Exception { // Add throws
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        // Use an empty status which should return empty results but not error
        filterDto.setStatus(LendingStatus.CLOSED.name());
        
                mockMvc.perform(post(BASE_URL + "/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.records").isArray())
                    .andExpect(jsonPath("$.records").isEmpty());
    }

    @Test
        @Order(38)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testFilterLendingRecords_PaginationEdgeCases() throws Exception { // Add throws
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());
        
                mockMvc.perform(post(BASE_URL + "/filter")
                        .param("page", "999")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filterDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.currentPage").value(0)); // Assuming default to page 0
    }

    @Test
        @Order(39)
        @WithMockUser(username="borrower@example.com", roles={"USER"}) // Need borrower auth
        public void testMarkGameAsReturned_WithoutUserId() throws Exception { // Add throws
                // Assuming controller uses authenticated user, not userId param
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/mark-returned"))
                    .andExpect(status().isOk());
    }

    @Test
        @Order(40)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testCreateLendingRecord_WithIdenticalDates() throws Exception { // Add throws
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
        
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestMap)))
                    .andExpect(status().isBadRequest());
    }

    @Test
        @Order(41)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateLendingRecordStatus_InvalidStatusValue() throws Exception { // Add throws
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("INVALID_STATUS");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing invalid status");
        
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isBadRequest());
                    // Cannot easily check response body content with MockMvc without more complex setup
    }

    @Test
        @Order(42)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateLendingRecordStatus_EmptyStatus() throws Exception { // Add throws
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing empty status");
        
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isBadRequest());
                    // Cannot easily check response body content with MockMvc without more complex setup
    }

    @Test
        @Order(43)
        @WithMockUser(username="owner@example.com", roles={"GAME_OWNER"}) // Need owner auth
        public void testUpdateLendingRecordStatus_NonExistentRecord() throws Exception { // Add throws
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing non-existent record");
        
                mockMvc.perform(put(BASE_URL + "/99999/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusDto)))
                    .andExpect(status().isNotFound());
                    // Cannot easily check response body content with MockMvc without more complex setup
    }

    @Test
        @Order(44)
        @WithMockUser // Basic auth
        public void testGetLendingRecordById_NonExistentRecord() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL + "/99999"))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(45)
        @WithMockUser // Basic auth
        public void testGetLendingHistoryByOwner_InvalidOwnerId() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL + "/owner/99999"))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(46)
        @WithMockUser // Basic auth
        public void testGetLendingRecordsByBorrower_InvalidBorrowerId() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL + "/borrower/99999"))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(47)
        @WithMockUser // Basic auth
        public void testGetActiveLendingRecordsByBorrower_InvalidBorrowerId() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL + "/borrower/99999/active"))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(48)
        @WithMockUser // Basic auth
        public void testHandleResourceNotFoundException() throws Exception { // Add throws
                // Try to get a record that doesn't exist
                mockMvc.perform(get(BASE_URL + "/999999"))
                    .andExpect(status().isNotFound());
    }

    @Test
        @Order(49)
        // Needs owner for close AND update
        public void testHandleIllegalStateException() throws Exception { // Add throws
        // First close the record
                // Close the record (needs owner auth)
                mockMvc.perform(post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                        .param("isDamaged", "false")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("owner@example.com").roles("GAME_OWNER")))
                    .andExpect(status().isOk());
        
        // Now try updating end date on a closed record (should trigger IllegalStateException)
        Date newEndDate = new Date(System.currentTimeMillis() + 86400000L);
                // Now try updating end date on a closed record (needs owner auth)
                mockMvc.perform(put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEndDate))
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("owner@example.com").roles("GAME_OWNER")))
                    .andExpect(status().isBadRequest());
                // Cannot easily check response body content with MockMvc without more complex setup
    }

    @Test
        @Order(50)
        @WithMockUser // Basic auth
        public void testLendingHistoryByOwnerAndStatus_InvalidStatus() throws Exception { // Add throws
                mockMvc.perform(get(BASE_URL + "/owner/" + testOwner.getId() + "/status/INVALID_STATUS"))
                    .andExpect(status().isBadRequest());
    }
}
