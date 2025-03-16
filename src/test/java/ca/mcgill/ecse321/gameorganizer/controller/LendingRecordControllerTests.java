package ca.mcgill.ecse321.gameorganizer.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.mcgill.ecse321.gameorganizer.controllers.LendingRecordController;
import ca.mcgill.ecse321.gameorganizer.dtos.LendingHistoryFilterDto;
import ca.mcgill.ecse321.gameorganizer.dtos.UpdateLendingRecordStatusDto;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.BorrowRequest;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord;
import ca.mcgill.ecse321.gameorganizer.models.LendingRecord.LendingStatus;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import ca.mcgill.ecse321.gameorganizer.services.LendingRecordService;
import ca.mcgill.ecse321.gameorganizer.services.BorrowRequestService;

/**
 * Test class for the LendingRecordController.
 * Tests REST API endpoints using MockMvc.
 * 
 * @author @YoussGm3o8
 */
@ExtendWith(MockitoExtension.class)
public class LendingRecordControllerTests {

    private MockMvc mockMvc;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Mock
    private LendingRecordService lendingRecordService;
    
    @Mock
    private AccountService accountService;
    
    @Mock
    private BorrowRequestService borrowRequestService;
    
    @InjectMocks
    private LendingRecordController lendingRecordController;
    
    private LendingRecord testRecord;
    private GameOwner testOwner;
    private Account testBorrower;
    private BorrowRequest testRequest;
    private Game testGame;
    
    @BeforeEach
    public void setUp() {
        // Setup MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(lendingRecordController).build();
        
        // Setup test data
        Date startDate = new Date(System.currentTimeMillis() + 86400000); // tomorrow
        Date endDate = new Date(System.currentTimeMillis() + 86400000 * 7); // next week
        
        testOwner = new GameOwner();
        testOwner.setId(1);
        testOwner.setName("testOwner");
        testOwner.setEmail("owner@test.com");
        
        testBorrower = new Account();
        testBorrower.setId(2);
        testBorrower.setName("testBorrower");
        testBorrower.setEmail("borrower@test.com");
        
        testGame = new Game();
        testGame.setId(1);
        testGame.setName("Test Game");
        testGame.setCategory("Test Genre");
        testGame.setOwner(testOwner);
        
        testRequest = new BorrowRequest();
        testRequest.setId(1);
        testRequest.setRequester(testBorrower);
        testRequest.setRequestedGame(testGame);
        
        testRecord = new LendingRecord(startDate, endDate, LendingStatus.ACTIVE, testRequest, testOwner);
        testRecord.setId(1);
    }
    
    /**
     * Test retrieving a lending record by ID.
     */
    @Test
    public void testGetLendingRecordById() throws Exception {
        when(lendingRecordService.getLendingRecordById(any(Integer.class))).thenReturn(testRecord);
        
        mockMvc.perform(get("/api/lending-records/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id", is(1)))
               .andExpect(jsonPath("$.status", is("ACTIVE")))
               .andExpect(jsonPath("$.game.name", is("Test Game")))
               .andExpect(jsonPath("$.borrower.name", is("testBorrower")))
               .andExpect(jsonPath("$.owner.name", is("testOwner")));
    }
    
    /**
     * Test retrieving lending records by owner.
     */
    @Test
    public void testGetLendingHistoryByOwner() throws Exception {
        List<LendingRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(accountService.getAccountById(any(Integer.class))).thenReturn(testOwner);
        when(lendingRecordService.getLendingRecordsByOwner(testOwner)).thenReturn(records);
        
        mockMvc.perform(get("/api/lending-records/owner/1"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
    
    /**
     * Test retrieving lending records by borrower.
     */
    @Test
    public void testGetLendingRecordsByBorrower() throws Exception {
        List<LendingRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(accountService.getAccountById(any(Integer.class))).thenReturn(testBorrower);
        when(lendingRecordService.getLendingRecordsByBorrower(testBorrower)).thenReturn(records);
        
        mockMvc.perform(get("/api/lending-records/borrower/2"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
    
    /**
     * Test retrieving active lending records for a borrower.
     */
    @Test
    public void testGetActiveLendingRecordsByBorrower() throws Exception {
        List<LendingRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(accountService.getAccountById(any(Integer.class))).thenReturn(testBorrower);
        when(lendingRecordService.getLendingRecordsByBorrower(testBorrower)).thenReturn(records);
        
        mockMvc.perform(get("/api/lending-records/borrower/2/active"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$", hasSize(1)))
               .andExpect(jsonPath("$[0].id", is(1)))
               .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
    
    /**
     * Test marking a game as returned.
     */
    @Test
    public void testMarkGameAsReturned() throws Exception {
        when(lendingRecordService.updateStatus(any(Integer.class), eq(LendingStatus.OVERDUE), any(), anyString()))
                .thenReturn(ResponseEntity.ok("Game marked as returned"));
        
        mockMvc.perform(post("/api/lending-records/1/mark-returned"))
               .andExpect(status().isOk())
               .andExpect(content().string("Game marked as returned"));
    }
    
    /**
     * Test confirming game return.
     */
    @Test
    public void testConfirmGameReturn() throws Exception {
        Map<String, Object> expectedResponse = new HashMap<>();
        expectedResponse.put("success", true);
        expectedResponse.put("message", "Lending record closed successfully");
        expectedResponse.put("recordId", 1);
        expectedResponse.put("returnTime", "any");
        expectedResponse.put("isDamaged", false);
        
        when(lendingRecordService.closeLendingRecordWithDamageAssessment(
                any(Integer.class), eq(false), any(), eq(0), any(), eq("Confirmed return of game")))
                .thenReturn(ResponseEntity.ok("Lending record closed successfully"));
        
        mockMvc.perform(post("/api/lending-records/1/confirm-return"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.success", is(true)))
               .andExpect(jsonPath("$.message", is("Lending record closed successfully")))
               .andExpect(jsonPath("$.recordId", is(1)))
               .andExpect(jsonPath("$.isDamaged", is(false)));
    }
    
    /**
     * Test filtering lending records with date range.
     */
    @Test
    public void testFilterLendingRecords() throws Exception {
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setFromDate(new Date());
        filterDto.setToDate(new Date(System.currentTimeMillis() + 86400000 * 14)); // 2 weeks
        
        List<LendingRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(lendingRecordService.filterLendingRecords(any(LendingHistoryFilterDto.class)))
                .thenReturn(records);
        
        mockMvc.perform(post("/api/lending-records/filter")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(filterDto)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.records", hasSize(1)))
               .andExpect(jsonPath("$.records[0].id", is(1)))
               .andExpect(jsonPath("$.records[0].status", is("ACTIVE")))
               .andExpect(jsonPath("$.currentPage", is(0)))
               .andExpect(jsonPath("$.totalItems", is(1)))
               .andExpect(jsonPath("$.totalPages", is(1)));
    }
    
    /**
     * Test updating end date of a lending record.
     */
    @Test
    public void testUpdateEndDate() throws Exception {
        Date newEndDate = new Date(System.currentTimeMillis() + 86400000 * 10); // 10 days
        
        when(lendingRecordService.updateEndDate(any(Integer.class), any(Date.class)))
                .thenReturn(ResponseEntity.ok("End date updated successfully"));
        
        mockMvc.perform(put("/api/lending-records/1/end-date")
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(objectMapper.writeValueAsString(newEndDate)))
               .andExpect(status().isOk())
               .andExpect(content().string("End date updated successfully"));
    }
    
    /**
     * Test creating a new lending record.
     */
    @Test
    public void testCreateLendingRecord() throws Exception {
        // Setup test data
        Map<String, Object> requestDetails = new HashMap<>();
        requestDetails.put("startDate", System.currentTimeMillis() + 86400000L); // tomorrow
        requestDetails.put("endDate", System.currentTimeMillis() + 86400000L * 7); // next week
        requestDetails.put("requestId", 1);
        requestDetails.put("ownerId", 1);
        
        // Mock the convertToResponseDto method indirectly by returning a populated record
        when(accountService.getAccountById(any(Integer.class))).thenReturn(testOwner);
        when(lendingRecordService.createLendingRecordFromRequestId(any(Date.class), any(Date.class), any(Integer.class), eq(testOwner)))
                .thenReturn(ResponseEntity.ok("Lending record created successfully"));
        when(lendingRecordService.getLendingRecordsByOwner(eq(testOwner)))
                .thenReturn(List.of(testRecord));
        
        mockMvc.perform(post("/api/lending-records")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }
    
    /**
     * Test updating the status of a lending record.
     */
    @Test
    public void testUpdateLendingRecordStatus() throws Exception {
        // Setup test data
        UpdateLendingRecordStatusDto statusDto = new UpdateLendingRecordStatusDto();
        statusDto.setNewStatus("CLOSED");
        statusDto.setUserId(1);
        statusDto.setReason("Game returned in good condition");
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Status updated successfully");
        response.put("recordId", 1);
        
        when(lendingRecordService.updateStatus(any(Integer.class), eq(LendingRecord.LendingStatus.CLOSED), any(Integer.class), eq("Game returned in good condition")))
                .thenReturn(ResponseEntity.ok("Status updated successfully"));
        
        mockMvc.perform(put("/api/lending-records/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Status updated successfully")))
                .andExpect(jsonPath("$.recordId", is(1)));
    }
    
    /**
     * Test retrieving overdue records.
     */
    @Test
    public void testGetOverdueRecords() throws Exception {
        // Setup test data
        List<LendingRecord> overdueRecords = new ArrayList<>();
        
        // Create an overdue lending record
        Date pastStart = new Date(System.currentTimeMillis() - 14 * 86400000); // 14 days ago
        Date pastEnd = new Date(System.currentTimeMillis() - 7 * 86400000); // 7 days ago
        
        LendingRecord overdueRecord = new LendingRecord(pastStart, pastEnd, LendingStatus.ACTIVE, testRequest, testOwner);
        overdueRecord.setId(2);
        overdueRecords.add(overdueRecord);
        
        when(lendingRecordService.findOverdueRecords()).thenReturn(overdueRecords);
        
        mockMvc.perform(get("/api/lending-records/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
    
    /**
     * Test retrieving overdue records by owner.
     */
    @Test
    public void testGetOverdueRecordsByOwner() throws Exception {
        // Setup test data
        List<LendingRecord> overdueRecords = new ArrayList<>();
        
        // Create an overdue lending record
        Date pastStart = new Date(System.currentTimeMillis() - 14 * 86400000); // 14 days ago
        Date pastEnd = new Date(System.currentTimeMillis() - 7 * 86400000); // 7 days ago
        
        LendingRecord overdueRecord = new LendingRecord(pastStart, pastEnd, LendingStatus.ACTIVE, testRequest, testOwner);
        overdueRecord.setId(2);
        overdueRecords.add(overdueRecord);
        
        when(accountService.getAccountById(any(Integer.class))).thenReturn(testOwner);
        when(lendingRecordService.getLendingRecordsByOwner(testOwner)).thenReturn(overdueRecords);
        
        mockMvc.perform(get("/api/lending-records/owner/1/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(2)))
                .andExpect(jsonPath("$[0].status", is("ACTIVE")));
    }
    
    /**
     * Test filtering lending records with complex criteria.
     */
    @Test
    public void testFilterLendingRecordsWithComplexCriteria() throws Exception {
        // Setup test data
        LendingHistoryFilterDto filterDto = new LendingHistoryFilterDto();
        filterDto.setStatus("ACTIVE");
        filterDto.setGameId(1);
        filterDto.setBorrowerId(2);
        filterDto.setFromDate(new Date(System.currentTimeMillis() - 86400000 * 14)); // 2 weeks ago
        filterDto.setToDate(new Date(System.currentTimeMillis() + 86400000 * 14)); // 2 weeks ahead
        
        List<LendingRecord> records = new ArrayList<>();
        records.add(testRecord);
        
        when(lendingRecordService.filterLendingRecords(any(LendingHistoryFilterDto.class)))
                .thenReturn(records);
        
        mockMvc.perform(post("/api/lending-records/filter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(filterDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.records", hasSize(1)))
                .andExpect(jsonPath("$.records[0].id", is(1)))
                .andExpect(jsonPath("$.records[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.currentPage", is(0)))
                .andExpect(jsonPath("$.totalItems", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }
} 