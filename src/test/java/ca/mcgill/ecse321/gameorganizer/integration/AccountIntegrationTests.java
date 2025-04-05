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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc; // Add MockMvc import
import com.fasterxml.jackson.databind.ObjectMapper; // Add ObjectMapper import
import org.springframework.http.MediaType; // Add MediaType import
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Add AutoConfigureMockMvc import
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*; // Add static import for request builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Add static imports for MockMvc matchers
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*; // Add static imports for security post processors
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

// Import test configurations
import ca.mcgill.ecse321.gameorganizer.TestJwtConfig;

import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Use MOCK environment
@ActiveProfiles("test")
@AutoConfigureMockMvc // Add this annotation
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = TestJwtConfig.Initializer.class) // Import test configurations
public class AccountIntegrationTests {

    // @LocalServerPort // Not needed with MockMvc
    // private int port;

    @Autowired
    private MockMvc mockMvc; // Inject MockMvc
    @Autowired
    private ObjectMapper objectMapper; // Inject ObjectMapper

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Account testAccount;
    private static final String BASE_URL = "/account";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        accountRepository.deleteAll();
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD));
        testAccount = accountRepository.save(testAccount);
    }

    @AfterEach
    public void cleanup() {
        accountRepository.deleteAll();
    }

    // Removed createURLWithPort and createAuthHeaders methods

    // ----- CREATE tests -----

    @Test
    @Order(1)
    public void testCreateAccountSuccess() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);

        mockMvc.perform(post(BASE_URL) // Use static import
                .with(anonymous()) // Assuming create account is public
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated()); // Expect 201 CREATED

        assertTrue(accountRepository.findByEmail("new@example.com").isPresent());
    }

    @Test
    @Order(2)
    public void testCreateGameOwnerSuccess() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("owner@example.com");
        request.setUsername("gameowner");
        request.setPassword("ownerpass123");
        request.setGameOwner(true);

        mockMvc.perform(post(BASE_URL) // Use static import
                .with(anonymous()) // Assuming create account is public
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated()); // Expect 201 CREATED

        Account created = accountRepository.findByEmail("owner@example.com").orElse(null);
        assertNotNull(created);
        assertTrue(created instanceof GameOwner);
    }

    @Test
    @Order(3)
    public void testCreateAccountWithDuplicateEmail() throws Exception {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail(VALID_EMAIL);  // Same as the existing testAccount
        request.setUsername("different");
        request.setPassword("different123");
        request.setGameOwner(false);

        mockMvc.perform(post(BASE_URL) // Use static import
                .with(anonymous()) // Assuming create account is public
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Expect 400 BAD_REQUEST
    }

    @Test
    @Order(4)
    public void testCreateAccountWithInvalidData() throws Exception {
        // Missing email
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);

        mockMvc.perform(post(BASE_URL) // Use static import
                .with(anonymous()) // Assuming create account is public
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest()); // Expect 400 BAD_REQUEST
    }

    // ----- UPDATE tests -----

    @Test
    @Order(5)
    public void testUpdateAccountSuccess() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD); // Current password for verification
        request.setNewPassword("newpassword123"); // New password

        // Simulate request as the authenticated user being updated
        mockMvc.perform(put(BASE_URL) // Use static import
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER")) // Simulate authenticated user
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk()); // Expect 200 OK

        Account updated = accountRepository.findByEmail(VALID_EMAIL).orElse(null);
        assertNotNull(updated);
        assertEquals("updateduser", updated.getName());
    }

    @Test
    @Order(6)
    public void testUpdateAccountWithWrongPassword() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword("wrongpassword"); // Incorrect current password
        request.setNewPassword("newpassword123");

        // Simulate request as the authenticated user providing wrong current password
        mockMvc.perform(put(BASE_URL) // Use static import
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER")) // Authenticate with correct password
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))) // But send wrong password in body
            .andExpect(status().isBadRequest()); // Expect 400 BAD_REQUEST
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentAccount() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("nonexistent@example.com"); // Non-existent email
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD); // Password doesn't matter here
        request.setNewPassword("newpassword123");

        // Simulate request as *some* authenticated user (e.g., the test user)
        // The authorization will reject the request because the email in the request 
        // doesn't match the authenticated user's email
        mockMvc.perform(put(BASE_URL) // Use static import
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER")) // Authenticate as the test user
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Expect 403 FORBIDDEN due to PreAuthorize check
    }


    // ----- Security: 401 Unauthorized Tests -----

    @Test
    @Order(10)
    public void testUpdateAccountUnauthenticated() throws Exception {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD);
        request.setNewPassword("newpassword123");

        mockMvc.perform(put(BASE_URL)
                .with(anonymous()) // Attempt without authentication
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized()); // Expect 401 UNAUTHORIZED
    }

    @Test
    @Order(11)
    public void testDeleteAccountUnauthenticated() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/" + VALID_EMAIL)
                .with(anonymous())) // Attempt without authentication
            .andExpect(status().isUnauthorized()); // Expect 401 UNAUTHORIZED
    }

    @Test
    @Order(12)
    public void testGetAccountUnauthenticated() throws Exception {
        // Assuming GET /account/{email} requires authentication based on SecurityConfig
        mockMvc.perform(get(BASE_URL + "/" + VALID_EMAIL)
                .with(anonymous())) // Attempt without authentication
            .andExpect(status().isUnauthorized()); // Expect 401 UNAUTHORIZED
    }

    // ----- Security: 403 Forbidden Tests -----

    @Test
    @Order(13)
    public void testUpdateAnotherUserAccount() throws Exception {
        // Create a second user
        Account anotherUser = accountRepository.save(new Account("anotheruser", "another@example.com", passwordEncoder.encode("anotherpass")));

        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(anotherUser.getEmail()); // Target the other user
        request.setUsername("updatedbyattacker");
        request.setPassword("anotherpass"); // Correct password for the *other* user
        request.setNewPassword("newpassword123");

        // Authenticate as the *first* user (testAccount)
        mockMvc.perform(put(BASE_URL)
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER")) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden()); // Expect 403 FORBIDDEN (due to @PreAuthorize likely)
    }

    @Test
    @Order(14)
    public void testDeleteAnotherUserAccount() throws Exception {
        // Create a second user
        Account anotherUser = accountRepository.save(new Account("anotheruser", "another@example.com", passwordEncoder.encode("anotherpass")));

        // Authenticate as the *first* user (testAccount) trying to delete the second user
        mockMvc.perform(delete(BASE_URL + "/" + anotherUser.getEmail())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER"))) 
            .andExpect(status().isForbidden()); // Expect 403 FORBIDDEN
    }

    @Test
    @Order(15)
    public void testGetAnotherUserAccount() throws Exception {
        // Create a second user
        Account anotherUser = accountRepository.save(new Account("anotheruser", "another@example.com", passwordEncoder.encode("anotherpass")));

        // Authenticate as the *first* user (testAccount) trying to get the second user
        mockMvc.perform(get(BASE_URL + "/" + anotherUser.getEmail())
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER"))) 
            .andExpect(status().isForbidden()); // Expect 403 FORBIDDEN (assuming GET is protected)
    }

    // ----- DELETE tests -----

    @Test
    @Order(8)
    public void testDeleteAccountSuccess() throws Exception {
        // Simulate request as the authenticated user being deleted
        mockMvc.perform(delete(BASE_URL + "/" + VALID_EMAIL) // Use static import
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER"))) // Authenticate as the user to be deleted
            .andExpect(status().isOk()); // Expect 200 OK
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentAccount() throws Exception {
        // Simulate request as *some* authenticated user (e.g., the test user)
        // The authorization will reject the request because the email in the path
        // doesn't match the authenticated user's email
        mockMvc.perform(delete(BASE_URL + "/nonexistent@example.com") // Use static import
                .with(user(VALID_EMAIL).password(VALID_PASSWORD).roles("USER"))) // Authenticate as the test user
            .andExpect(status().isForbidden()); // Expect 403 FORBIDDEN due to PreAuthorize check
    }
}
