package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.core.userdetails.User; // Keep User import

// DTOs and Models
import ca.mcgill.ecse321.gameorganizer.dto.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.dto.LendingRecordResponseDto; // Keep if needed for response mapping
import ca.mcgill.ecse321.gameorganizer.dto.UpdateLendingRecordStatusDto;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
// Repositories
import ca.mcgill.ecse321.gameorganizer.repositories.LendingRecordRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.BorrowRequestRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.GameRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.ReviewRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Use MOCK environment
@ActiveProfiles("test")
@AutoConfigureMockMvc // Configure MockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LendingRecordIntegrationTests {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

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
    private Game dummyGame;
    private BorrowRequest dummyRequest;
    private LendingRecord testRecord;

    private static final String BASE_URL = "/lending-records";

    @BeforeEach
    public void setup() {
        reviewRepository.deleteAll();
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        testOwner = new GameOwner("Owner", "owner@example.com", passwordEncoder.encode("pass"));
        testOwner = (GameOwner) accountRepository.save(testOwner);

        testBorrower = new Account("Borrower", "borrower@example.com", passwordEncoder.encode("pass"));
        testBorrower = accountRepository.save(testBorrower);

        dummyGame = new Game();
        dummyGame.setName("Dummy Game");
        dummyGame.setOwner(testOwner);
        dummyGame = gameRepository.save(dummyGame);

        dummyRequest = new BorrowRequest();
        dummyRequest.setRequestedGame(dummyGame);
        dummyRequest.setRequester(testBorrower);
        dummyRequest = borrowRequestRepository.save(dummyRequest);

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
        reviewRepository.deleteAll();
        lendingRecordRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        eventRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // ============================================================
    // CREATE Tests
    // ============================================================
    @Test
    @Order(1)
    public void testCreateLendingRecordSuccess() throws Exception {
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        long startDate = now + 10800000L;
        long endDate = now + 86400000L;
        requestMap.put("startDate", startDate);
        requestMap.put("endDate", endDate);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isOk())
            // .andExpect(content().string(org.hamcrest.Matchers.containsString("Lending record created successfully"))); // Old assertion
            .andExpect(jsonPath("$.id").exists()); // Check for ID field in JSON response
    }

    @Test
    @Order(2)
    public void testCreateLendingRecordMissingRequestId() throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(3)
    public void testCreateLendingRecordWithInvalidDates() throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 86400000L);
        requestMap.put("endDate", now);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(4)
    public void testCreateLendingRecordWithNonExistentOwner() throws Exception {
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", dummyRequest.getId());
        requestMap.put("ownerId", 99999);
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest());
    }

    // ============================================================
    // UPDATE Tests
    // ============================================================
    @Test
    @Order(5)
    public void testUpdateEndDateSuccess() throws Exception {
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEndDate)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("End date updated successfully")));
    }

    @Test
    @Order(6)
    public void testUpdateEndDateWithInvalidDate() throws Exception {
        Date invalidEndDate = new Date(System.currentTimeMillis() - 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEndDate)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentLendingRecord() throws Exception {
        Date newEndDate = new Date(System.currentTimeMillis() + 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/99999/end-date")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEndDate)))
            .andExpect(status().isNotFound());
    }

    // ============================================================
    // DELETE Tests
    // ============================================================
    @Test
    @Order(8)
    public void testDeleteLendingRecordSuccess() throws Exception {
        // First, close the record
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // Now, delete the CLOSED record
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testRecord.getId())
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        assertFalse(lendingRecordRepository.findById(testRecord.getId()).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentLendingRecord() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/99999")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(10)
    public void testDeleteLendingRecordTwice() throws Exception {
        // First, close the record
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // First deletion attempt
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testRecord.getId())
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // Second deletion attempt
        mockMvc.perform(MockMvcRequestBuilders.delete(BASE_URL + "/" + testRecord.getId())
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isNotFound());
    }

    // ============================================================
    // GET Tests
    // ============================================================
    @Test
    @Order(11)
    public void testGetAllLendingRecords() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").exists())
            .andExpect(jsonPath("$.totalItems").exists())
            .andExpect(jsonPath("$.currentPage").exists());
    }

    @Test
    @Order(12)
    public void testGetLendingRecordById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/" + testRecord.getId())
                 .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(testRecord.getId()));
    }

    @Test
    @Order(13)
    public void testGetLendingRecordsByOwner() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value(testRecord.getId()));
    }

    @Test
    @Order(14)
    public void testGetLendingRecordsByOwnerAndStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/status/ACTIVE")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @Order(15)
    public void testGetLendingRecordsByBorrower() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/borrower/" + testBorrower.getId())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].borrower.id").value(testBorrower.getId()));
    }

    @Test
    @Order(16)
    public void testGetActiveLendingRecordsByBorrower() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/borrower/" + testBorrower.getId() + "/active")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    @Order(17)
    public void testFilterLendingRecords() throws Exception {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/filter")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").exists());
    }

    @Test
    @Order(18)
    public void testGetOverdueRecords() throws Exception {
        LendingRecord overdueRecord = new LendingRecord();
        overdueRecord.setStartDate(new Date(System.currentTimeMillis() - 2 * 86400000L));
        overdueRecord.setEndDate(new Date(System.currentTimeMillis() - 86400000L));
        overdueRecord.setStatus(LendingStatus.ACTIVE);
        overdueRecord.setRecordOwner(testOwner);
        BorrowRequest overdueRequest = new BorrowRequest();
        overdueRequest.setRequestedGame(dummyGame);
        overdueRequest.setRequester(testBorrower);
        overdueRequest = borrowRequestRepository.save(overdueRequest);
        overdueRecord.setRequest(overdueRequest);
        overdueRecord = lendingRecordRepository.save(overdueRecord);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/overdue")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.id == %d)]", overdueRecord.getId()).exists());
    }

    @Test
    @Order(19)
    public void testGetOverdueRecordsByOwner() throws Exception {
        LendingRecord overdueRecord = new LendingRecord();
        overdueRecord.setStartDate(new Date(System.currentTimeMillis() - 2 * 86400000L));
        overdueRecord.setEndDate(new Date(System.currentTimeMillis() - 86400000L));
        overdueRecord.setStatus(LendingStatus.ACTIVE);
        overdueRecord.setRecordOwner(testOwner);
        BorrowRequest overdueRequest = new BorrowRequest();
        overdueRequest.setRequestedGame(dummyGame);
        overdueRequest.setRequester(testBorrower);
        overdueRequest = borrowRequestRepository.save(overdueRequest);
        overdueRecord.setRequest(overdueRequest);
        overdueRecord = lendingRecordRepository.save(overdueRecord);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/overdue")
                 .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.id == %d)]", overdueRecord.getId()).exists());
    }

    // ============================================================
    // UPDATE Status Tests
    // ============================================================
    @Test
    @Order(20)
    public void testUpdateLendingRecordStatus() throws Exception {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Game is overdue");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/status")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.newStatus").value(LendingStatus.OVERDUE.name()));
    }

    @Test
    @Order(21)
    public void testInvalidStatusTransition() throws Exception {
        // First, close the record
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // Now try to change from CLOSED to ACTIVE
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.ACTIVE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Trying invalid transition");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/status")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(22)
    public void testMarkGameAsReturned() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/mark-returned")
                .param("userId", String.valueOf(testBorrower.getId()))
                .with(user(testBorrower.getEmail()).password("pass").roles("USER")))
            .andExpect(status().isOk());

        LendingRecord updatedRecord = lendingRecordRepository.findById(testRecord.getId()).orElse(null);
        assertNotNull(updatedRecord);
        assertEquals(LendingStatus.OVERDUE, updatedRecord.getStatus()); // Assuming service sets to OVERDUE
    }

    @Test
    @Order(23)
    public void testConfirmGameReturn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "true")
                .param("damageNotes", "The game box is damaged")
                .param("damageSeverity", "2")
                .param("userId", String.valueOf(testOwner.getId()))
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.isDamaged").value(true))
            .andExpect(jsonPath("$.damageSeverity").value(2));

        LendingRecord updatedRecord = lendingRecordRepository.findById(testRecord.getId()).orElse(null);
        assertNotNull(updatedRecord);
        assertEquals(LendingStatus.CLOSED, updatedRecord.getStatus());
    }

    // ============================================================
    // Additional Tests for getLendingHistoryByOwnerAndDateRange
    // ============================================================
    @Test
    @Order(24)
    public void testGetLendingHistoryByOwnerAndDateRange_Success() throws Exception {
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L);
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                .param("startDate", startDate.toInstant().toString())
                .param("endDate", endDate.toInstant().toString())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].id").value(testRecord.getId()));
    }

    @Test
    @Order(25)
    public void testGetLendingHistoryByOwnerAndDateRange_InvalidDateRange() throws Exception {
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                .param("startDate", startDate.toInstant().toString())
                .param("endDate", endDate.toInstant().toString())
                .with(anonymous()))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(26)
    public void testGetLendingHistoryByOwnerAndDateRange_NoRecordsInRange() throws Exception {
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() + 30 * 86400000L);
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 60 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/date-range")
                .param("startDate", startDate.toInstant().toString())
                .param("endDate", endDate.toInstant().toString())
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @Order(27)
    public void testGetLendingHistoryByOwnerAndDateRange_InvalidOwnerId() throws Exception {
        java.util.Date startDate = new java.util.Date(System.currentTimeMillis() - 2 * 86400000L);
        java.util.Date endDate = new java.util.Date(System.currentTimeMillis() + 2 * 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/99999/date-range")
                .param("startDate", startDate.toInstant().toString())
                .param("endDate", endDate.toInstant().toString())
                .with(anonymous()))
            .andExpect(status().isBadRequest()); // Assuming service/controller returns 400 for invalid ID path variable
    }

    // ============================================================
    // Additional Tests for createLendingRecord
    // ============================================================
    @Test
    @Order(28)
    public void testCreateLendingRecord_DuplicateRecord() throws Exception {
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);
        LendingRecord newRecord = new LendingRecord();
        newRecord.setStartDate(new Date(System.currentTimeMillis()));
        newRecord.setEndDate(new Date(System.currentTimeMillis() + 86400000L));
        newRecord.setStatus(LendingStatus.ACTIVE);
        newRecord.setRecordOwner(testOwner);
        newRecord.setRequest(newRequest);
        newRecord = lendingRecordRepository.save(newRecord);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("already has a lending record")));
    }

    @Test
    @Order(29)
    public void testCreateLendingRecord_InvalidOwnerType() throws Exception {
        Account regularAccount = new Account("Regular", "regular@example.com", passwordEncoder.encode("pass"));
        regularAccount = accountRepository.save(regularAccount);
        BorrowRequest validRequest = new BorrowRequest();
        validRequest.setRequestedGame(dummyGame);
        validRequest.setRequester(testBorrower);
        validRequest = borrowRequestRepository.save(validRequest);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", validRequest.getId());
        requestMap.put("ownerId", regularAccount.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now + 10800000L);
        requestMap.put("endDate", now + 86400000L);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest());
    }

    // ============================================================
    // Test for handleInvalidOperationException
    // ============================================================
    @Test
    @Order(30)
    public void testHandleInvalidOperationException() throws Exception {
        // First close the record
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // Now try to mark it as returned
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/mark-returned")
                 .with(user(testBorrower.getEmail()).password("pass").roles("USER")))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(31)
    public void testValidateDamageSeverity_Invalid() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "true")
                .param("damageSeverity", "4")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(32)
    public void testGetAllLendingRecords_EmptyResults() throws Exception {
        lendingRecordRepository.deleteAll();

        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("page", "0")
                .param("size", "10")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isArray())
            .andExpect(jsonPath("$.records", org.hamcrest.Matchers.hasSize(0)))
            .andExpect(jsonPath("$.totalItems").value(0));
    }

    @Test
    @Order(33)
    public void testGetAllLendingRecords_InvalidPage() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("page", "999")
                .param("size", "10")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isArray())
            .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @Order(34)
    public void testGetAllLendingRecords_SortDirectionDesc() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL)
                .param("sort", "id")
                .param("direction", "desc")
                .with(anonymous()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").exists());
    }

    @Test
    @Order(35)
    public void testConfirmGameReturn_WithoutUserId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk())
             .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(36)
    public void testConfirmGameReturn_DifferentDamageSeverities() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "true")
                .param("damageSeverity", "1")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.damageSeverity").value(1));
    }

    @Test
    @Order(37)
    public void testFilterLendingRecords_EmptyResults() throws Exception {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.CLOSED.name());

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/filter")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.records").isArray())
            .andExpect(jsonPath("$.records", org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    @Order(38)
    public void testFilterLendingRecords_PaginationEdgeCases() throws Exception {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus(LendingStatus.ACTIVE.name());

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/filter")
                .param("page", "999")
                .param("size", "10")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.currentPage").value(0));
    }

    @Test
    @Order(39)
    public void testMarkGameAsReturned_WithoutUserId() throws Exception {
         mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/mark-returned")
                .with(user(testBorrower.getEmail()).password("pass").roles("USER")))
            .andExpect(status().isOk());
    }

    @Test
    @Order(40)
    public void testCreateLendingRecord_WithIdenticalDates() throws Exception {
        BorrowRequest newRequest = new BorrowRequest();
        newRequest.setRequestedGame(dummyGame);
        newRequest.setRequester(testBorrower);
        newRequest = borrowRequestRepository.save(newRequest);

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestId", newRequest.getId());
        requestMap.put("ownerId", testOwner.getId());
        long now = System.currentTimeMillis();
        requestMap.put("startDate", now);
        requestMap.put("endDate", now);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL)
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestMap)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(41)
    public void testUpdateLendingRecordStatus_InvalidStatusValue() throws Exception {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("INVALID_STATUS");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing invalid status");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/status")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(42)
    public void testUpdateLendingRecordStatus_EmptyStatus() throws Exception {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("");
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing empty status");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/status")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Order(43)
    public void testUpdateLendingRecordStatus_NonExistentRecord() throws Exception {
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus(LendingStatus.OVERDUE.name());
        statusDto.setUserId(testOwner.getId());
        statusDto.setReason("Testing non-existent record");

        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/99999/status")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(44)
    public void testGetLendingRecordById_NonExistentRecord() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/99999")
                .with(anonymous()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(45)
    public void testGetLendingHistoryByOwner_InvalidOwnerId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/99999")
                .with(anonymous()))
            .andExpect(status().isNotFound()); // Expect NOT_FOUND if owner doesn't exist
    }

    @Test
    @Order(46)
    public void testGetLendingRecordsByBorrower_InvalidBorrowerId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/borrower/99999")
                .with(anonymous()))
            .andExpect(status().isNotFound()); // Expect NOT_FOUND if borrower doesn't exist
    }

    @Test
    @Order(47)
    public void testGetActiveLendingRecordsByBorrower_InvalidBorrowerId() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/borrower/99999/active")
                .with(anonymous()))
            .andExpect(status().isNotFound()); // Expect NOT_FOUND if borrower doesn't exist
    }

    @Test
    @Order(48)
    public void testHandleResourceNotFoundException() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/999999")
                .with(anonymous()))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(49)
    public void testHandleIllegalStateException() throws Exception {
        // First close the record
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/" + testRecord.getId() + "/confirm-return")
                .param("isDamaged", "false")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER")))
            .andExpect(status().isOk());

        // Now try updating end date on a closed record
        Date newEndDate = new Date(System.currentTimeMillis() + 86400000L);
        mockMvc.perform(MockMvcRequestBuilders.put(BASE_URL + "/" + testRecord.getId() + "/end-date")
                .with(user(testOwner.getEmail()).password("pass").roles("GAME_OWNER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newEndDate)))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(org.hamcrest.Matchers.containsString("closed")));
    }

    @Test
    @Order(50)
    public void testLendingHistoryByOwnerAndStatus_InvalidStatus() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get(BASE_URL + "/owner/" + testOwner.getId() + "/status/INVALID_STATUS")
                .with(anonymous()))
            .andExpect(status().isBadRequest());
    }
}
