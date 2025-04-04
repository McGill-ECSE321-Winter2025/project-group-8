package ca.mcgill.ecse321.gameorganizer.integration;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.mcgill.ecse321.gameorganizer.config.SecurityConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
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


@SpringBootTest
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
public class BorrowRequestIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private GameOwner testOwner;
    private Account testRequester; // This user will have ROLE_USER
    private Game testGame;
    private BorrowRequest testRequest;
    private static final String BASE_URL = "/api/v1/borrowrequests";

    @BeforeEach
    public void setup() {
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();

        testOwner = new GameOwner("owner", "owner@example.com", passwordEncoder.encode("password123"));
        testOwner = accountRepository.save(testOwner);

        testRequester = new Account("requester", "requester@example.com", passwordEncoder.encode("password123"));
        testRequester = accountRepository.save(testRequester);

        testGame = new Game("Test Game", 2, 4, "test.jpg", java.util.Date.from(Instant.now()));
        testGame.setOwner(testOwner);
        testGame = gameRepository.save(testGame);

        Date startDate = new Date(Instant.now().plus(1, ChronoUnit.DAYS).toEpochMilli());
        Date endDate = new Date(Instant.now().plus(2, ChronoUnit.DAYS).toEpochMilli());
        testRequest = new BorrowRequest(startDate, endDate, BorrowRequestStatus.PENDING, java.util.Date.from(Instant.now()), testGame);
        testRequest.setRequester(testRequester); // Link request to the regular user
        testRequest = borrowRequestRepository.save(testRequest);
    }

    @AfterEach
    public void cleanup() {
        borrowRequestRepository.deleteAll();
        gameRepository.deleteAll();
        accountRepository.deleteAll();
    }

    // ----- CREATE Tests -----

    @Test
    @Order(1)
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // Run as regular user
    public void testCreateBorrowRequestSuccessAsUser() throws Exception {
        Date startDate = new Date(Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli());
        Date endDate = new Date(Instant.now().plus(4, ChronoUnit.DAYS).toEpochMilli());
        // Use the ID of the user created in setup
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(), 
            testGame.getId(),
            startDate,
            endDate
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()) // Regular user can create
                .andExpect(jsonPath("$.requesterId").value(testRequester.getId()))
                .andExpect(jsonPath("$.requestedGameId").value(testGame.getId()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
    
    // Removed testCreateBorrowRequestSuccessAsOwner as it tests an invalid scenario
    // where the owner tries to borrow their own game, which is disallowed by the service.


    @Test
    @Order(2) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // Need auth even for bad requests
    public void testCreateBorrowRequestWithInvalidGame() throws Exception {
        Date startDate = new Date(Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli());
        Date endDate = new Date(Instant.now().plus(4, ChronoUnit.DAYS).toEpochMilli());
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            999,  // invalid game id
            startDate,
            endDate
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    @Order(3) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // Need auth even for bad requests
    public void testCreateBorrowRequestWithInvalidRequester() throws Exception {
        Date startDate = new Date(Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli());
        Date endDate = new Date(Instant.now().plus(4, ChronoUnit.DAYS).toEpochMilli());
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            999,  // invalid requester id
            testGame.getId(),
            startDate,
            endDate
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }

    @Test
    @Order(4) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // Need auth even for bad requests
    public void testCreateBorrowRequestWithInvalidDates() throws Exception {
        Date startDate = new Date(Instant.now().plus(4, ChronoUnit.DAYS).toEpochMilli()); // Start after end
        Date endDate = new Date(Instant.now().plus(3, ChronoUnit.DAYS).toEpochMilli());
        CreateBorrowRequestDto request = new CreateBorrowRequestDto(
            testRequester.getId(),
            testGame.getId(),
            startDate,
            endDate
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); 
    }
    
    // ----- UPDATE Tests -----

    @Test
    @Order(5) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Only Owner can update
    public void testUpdateBorrowRequestStatusSuccess() throws Exception {
        BorrowRequestDto updateDto = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED", // Update status
            testRequest.getRequestDate()
        );

        mockMvc.perform(put(BASE_URL + "/" + testRequest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
    
    @Test
    @Order(6) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User cannot update
    public void testUpdateBorrowRequestStatusForbidden() throws Exception {
        BorrowRequestDto updateDto = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED", 
            testRequest.getRequestDate()
        );

        mockMvc.perform(put(BASE_URL + "/" + testRequest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden
    }


    @Test
    @Order(7) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Owner can update
    public void testUpdateBorrowRequestWithInvalidStatus() throws Exception {
        BorrowRequestDto updateDto = new BorrowRequestDto(
            testRequest.getId(),
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "INVALID_STATUS", // Invalid status
            testRequest.getRequestDate()
        );

        mockMvc.perform(put(BASE_URL + "/" + testRequest.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound()); // Assuming service/controller returns 404 for invalid status enum conversion
    }

    @Test
    @Order(8) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Owner can update
    public void testUpdateNonExistentBorrowRequest() throws Exception {
         BorrowRequestDto updateDto = new BorrowRequestDto(
            999, // Non-existent ID
            testRequester.getId(),
            testGame.getId(),
            testRequest.getStartDate(),
            testRequest.getEndDate(),
            "APPROVED",
            testRequest.getRequestDate()
        );

         mockMvc.perform(put(BASE_URL + "/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    // ----- DELETE Tests -----

    @Test
    @Order(9) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Only Owner can delete
    public void testDeleteBorrowRequestSuccess() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testRequest.getId()))
                .andExpect(status().isOk());

        assertFalse(borrowRequestRepository.findById(testRequest.getId()).isPresent());
    }
    
    @Test
    @Order(10) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User cannot delete
    public void testDeleteBorrowRequestForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + testRequest.getId()))
                .andExpect(status().isForbidden()); // Expect 403 Forbidden
    }


    @Test
    @Order(11) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Owner can delete
    public void testDeleteNonExistentBorrowRequest() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(12) // Re-numbering order after removal
    @WithMockUser(username = "owner@example.com", authorities = "ROLE_GAME_OWNER") // Owner can delete
    public void testDeleteBorrowRequestTwice() throws Exception {
        // First delete
        mockMvc.perform(delete(BASE_URL + "/" + testRequest.getId()))
                .andExpect(status().isOk());

        // Second delete
        mockMvc.perform(delete(BASE_URL + "/" + testRequest.getId()))
                .andExpect(status().isNotFound());
    }

    // ----- GET Tests -----

    @Test
    @Order(13) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetBorrowRequestByIdSuccess() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + testRequest.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testRequest.getId()))
                .andExpect(jsonPath("$.requesterId").value(testRequester.getId()))
                .andExpect(jsonPath("$.requestedGameId").value(testGame.getId()));
    }

    @Test
    @Order(14) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetNonExistentBorrowRequestById() throws Exception {
         mockMvc.perform(get(BASE_URL + "/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(15) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetAllBorrowRequests() throws Exception {
        // Note: This gets ALL requests. Depending on requirements, might need filtering.
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) 
                .andExpect(jsonPath("$[0].id").value(testRequest.getId())); 
    }

    @Test
    @Order(16) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetBorrowRequestsByStatusSuccess() throws Exception {
         mockMvc.perform(get(BASE_URL + "/status/PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    @Order(17) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetBorrowRequestsByStatusNoResults() throws Exception {
        mockMvc.perform(get(BASE_URL + "/status/APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); 
    }

    @Test
    @Order(18) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetBorrowRequestsByRequesterSuccess() throws Exception {
        // Note: This gets requests for a specific requester ID.
        mockMvc.perform(get(BASE_URL + "/requester/" + testRequester.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].requesterId").value(testRequester.getId()));
    }

    @Test
    @Order(19) // Re-numbering order after removal
    @WithMockUser(username = "requester@example.com", authorities = "ROLE_USER") // User can GET
    public void testGetBorrowRequestsByRequesterNoResults() throws Exception {
        mockMvc.perform(get(BASE_URL + "/requester/999")) // Non-existent user ID
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0)); 
    }
}