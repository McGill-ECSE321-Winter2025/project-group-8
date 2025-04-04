package ca.mcgill.ecse321.gameorganizer.integration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Add MockMvc auto-config
import org.springframework.boot.test.context.SpringBootTest;
// TestRestTemplate removed
import org.springframework.boot.test.web.server.LocalServerPort;
// HttpEntity, HttpHeaders, HttpMethod removed (will use MockMvc builders)
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; // Add MediaType
// ResponseEntity removed (will use MockMvc matchers)
import org.springframework.test.web.servlet.MockMvc; // Add MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; // Add MockMvc builders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers; // Add MockMvc matchers
import org.springframework.security.test.context.support.WithMockUser; // Add WithMockUser
import com.fasterxml.jackson.databind.ObjectMapper; // Add ObjectMapper
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

// SecurityConfig import removed from here
import ca.mcgill.ecse321.gameorganizer.GameorganizerApplication; // Import main application
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestSecurityConfig; // Import test security config
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

// Explicitly load only the main application, test config, and test security config
// This prevents the main SecurityConfig from being loaded via component scan
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {GameorganizerApplication.class, TestConfig.class, TestSecurityConfig.class}
)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc // Enable MockMvc
public class AccountIntegrationTests {

    @LocalServerPort
    private int port;

    // TestRestTemplate removed
    @Autowired
    private MockMvc mockMvc; // Inject MockMvc

    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Account testAccount;
    private static final String BASE_URL = "/api/v1/account";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";
    
    @BeforeEach
    public void setup() {
        accountRepository.deleteAll();
        // Create a test account (useful for update and delete tests)
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD));
        testAccount = accountRepository.save(testAccount);
    }
    
    @AfterEach
    public void cleanup() {
        accountRepository.deleteAll();
    }
    
    private String createURLWithPort(String uri) {
        return String.format("http://localhost:%d%s", port, uri);
    }
    
    // createAuthHeaders method removed, authentication will be handled by @WithMockUser

    
    // ----- CREATE tests -----
    
    @Test
    @Order(1)
    public void testCreateAccountSuccess() throws Exception { // Add throws Exception
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.post(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated()); // Check status using MockMvc matchers
        assertTrue(accountRepository.findByEmail("new@example.com").isPresent());
    }
    
    @Test
    @Order(2)
    public void testCreateGameOwnerSuccess() throws Exception { // Add throws Exception
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("owner@example.com");
        request.setUsername("gameowner");
        request.setPassword("ownerpass123");
        request.setGameOwner(true);
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.post(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isCreated()); // Check status using MockMvc matchers
        
        Account created = accountRepository.findByEmail("owner@example.com").orElse(null);
        assertNotNull(created);
        assertTrue(created instanceof GameOwner);
    }
    
    @Test
    @Order(3)
    public void testCreateAccountWithDuplicateEmail() throws Exception { // Add throws Exception
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail(VALID_EMAIL);  // Same as the existing testAccount
        request.setUsername("different");
        request.setPassword("different123");
        request.setGameOwner(false);
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.post(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Check status using MockMvc matchers
    }
    
    @Test
    @Order(4)
    public void testCreateAccountWithInvalidData() throws Exception { // Add throws Exception
        // Missing email
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.post(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Check status using MockMvc matchers
    }
    
    // ----- UPDATE tests -----
    
    @Test
@Order(5)
@WithMockUser(username = VALID_EMAIL) // Add mock user for authentication
public void testUpdateAccountSuccess() throws Exception { // Add throws Exception
    UpdateAccountRequest request = new UpdateAccountRequest();
    request.setEmail(VALID_EMAIL);
    request.setUsername("updateduser");
    // Provide the current plain text password for verification by the service
    request.setPassword(VALID_PASSWORD);
    // For new password, you might choose to send plain text if your service simply replaces it,
    // or send the encoded one if that's what your service expects. Here we'll send plain text.
    request.setNewPassword("newpassword123");
    
    // Use MockMvc
    mockMvc.perform(MockMvcRequestBuilders.put(createURLWithPort(BASE_URL))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(MockMvcResultMatchers.status().isOk()); // Check status
    Account updated = accountRepository.findByEmail(VALID_EMAIL).orElse(null);
    assertNotNull(updated);
    assertEquals("updateduser", updated.getName());
}
    
    @Test
    @Order(6)
    @WithMockUser(username = VALID_EMAIL) // Add mock user
    public void testUpdateAccountWithWrongPassword() throws Exception { // Add throws Exception
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword("wrongpassword");
        request.setNewPassword("newpassword123");
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.put(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Check status
    }
    
    @Test
    @Order(7)
    @WithMockUser(username = VALID_EMAIL) // Add mock user (needed to authenticate the PUT request itself)
    public void testUpdateNonExistentAccount() throws Exception { // Add throws Exception
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("nonexistent@example.com");
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD);
        request.setNewPassword("newpassword123");
        
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.put(createURLWithPort(BASE_URL))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Check status
    }
    
    // ----- DELETE tests -----
    
    @Test
    @Order(8)
    @WithMockUser(username = VALID_EMAIL) // Add mock user
    public void testDeleteAccountSuccess() throws Exception { // Add throws Exception
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.delete(createURLWithPort(BASE_URL + "/" + VALID_EMAIL)))
                .andExpect(MockMvcResultMatchers.status().isOk()); // Check status
    }
    
    @Test
    @Order(9)
    @WithMockUser(username = VALID_EMAIL) // Add mock user (needed to authenticate the DELETE request itself)
    public void testDeleteNonExistentAccount() throws Exception { // Add throws Exception
        // Use MockMvc
        mockMvc.perform(MockMvcRequestBuilders.delete(createURLWithPort(BASE_URL + "/nonexistent@example.com")))
                .andExpect(MockMvcResultMatchers.status().isBadRequest()); // Check status
    }
}
