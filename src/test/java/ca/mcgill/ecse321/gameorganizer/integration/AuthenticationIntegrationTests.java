package ca.mcgill.ecse321.gameorganizer.integration;

import com.fasterxml.jackson.databind.ObjectMapper; // For JSON conversion
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // Import MockMvc config
import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.web.client.TestRestTemplate; // Remove TestRestTemplate
// import org.springframework.boot.test.web.server.LocalServerPort; // Remove LocalServerPort
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType; // Import MediaType
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc; // Import MockMvc
import ca.mcgill.ecse321.gameorganizer.dto.JwtAuthenticationResponse; // Import the correct DTO
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders; // Import request builders
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*; // Import result matchers
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext; // Keep this import for now
// Remove TestPropertySource import
import ca.mcgill.ecse321.gameorganizer.GameorganizerApplication; // Add main app import
// Remove SecurityConfig import
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestSecurityConfig; // Add TestSecurityConfig import
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

// Apply standard configuration
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = {GameorganizerApplication.class, TestConfig.class, TestSecurityConfig.class}
)
@ActiveProfiles("test") // Add ActiveProfiles
@AutoConfigureMockMvc // Keep this
// Remove @Import
// Remove @TestPropertySource
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Keep DirtiesContext for now
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuthenticationIntegrationTests {
// @LocalServerPort // Remove port
// private int port;

@Autowired
private MockMvc mockMvc; // Inject MockMvc

@Autowired
private ObjectMapper objectMapper; // Inject ObjectMapper for JSON conversion
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private Account testAccount;
    private static final String BASE_URL = "/api/v1/auth";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        accountRepository.deleteAll();
        // Save a test account with an encoded password so that login succeeds
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, passwordEncoder.encode(VALID_PASSWORD));
        testAccount = accountRepository.save(testAccount);
    }

    @AfterEach
    public void cleanup() {
        accountRepository.deleteAll();
    }

    // Remove createURLWithPort helper method

    // ----- CREATE METHODS (Login) - 4 tests -----

    @Test
    @Order(1)
    public void testLoginSuccess() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                .andExpect(jsonPath("$.userId").exists())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword("wrongpassword");

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    public void testLoginWithNonExistentAccount() throws Exception {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail("nonexistent@example.com");
        request.setPassword(VALID_PASSWORD);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    public void testLoginWithMissingFields() throws Exception {
        // Missing password should result in a BAD_REQUEST (handled by controller validation)
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        // Password not provided

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest()); // Now expect 400 Bad Request
    }

    // ----- DELETE METHODS (Logout) - 3 tests -----

    @Test
    @Order(5)
    public void testLogoutSuccess() throws Exception {
        // For stateless JWT, logout is often just a confirmation endpoint.
        // We don't strictly need to login first unless the endpoint requires authentication,
        // but the controller logic suggests it's public.
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }

    @Test
    @Order(6)
    public void testLogoutWithoutLogin() throws Exception {
        // Endpoint should be accessible and return OK even without prior login/token
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/logout"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }

    @Test
    @Order(7)
    public void testLogoutWithInvalidToken() throws Exception {
        // Test with an invalid Authorization header (JWT specific)
        // The endpoint should still return OK as it doesn't validate the token for logout.
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/logout")
                .header("Authorization", "Bearer invalidToken"))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully logged out"));
    }

    // ----- UPDATE METHODS (Reset Password) - 3 tests -----

    @Test
    @Order(8)
    public void testResetPasswordSuccess() throws Exception {
        String newPassword = "newpassword123";

        // Perform the reset password request
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/reset-password")
                .param("email", VALID_EMAIL)
                .param("newPassword", newPassword))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully")); // Assuming this is the success message

        // Verify that logging in with the new password succeeds
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(newPassword);

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists()); // Check if login is successful
    }

    @Test
    @Order(9)
    public void testResetPasswordForNonExistentAccount() throws Exception {
        String newPassword = "newpassword123";

        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/reset-password")
                .param("email", "nonexistent@example.com")
                .param("newPassword", newPassword))
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }

    @Test
    @Order(10)
    public void testResetPasswordWithMissingNewPassword() throws Exception {
        // Attempt to reset password without providing newPassword parameter
        mockMvc.perform(MockMvcRequestBuilders.post(BASE_URL + "/reset-password")
                .param("email", VALID_EMAIL)) // Missing newPassword parameter
                .andExpect(status().isBadRequest()); // Expect 400 Bad Request
    }
}
