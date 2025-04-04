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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
// @Import annotation is no longer needed as classes are specified in @SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
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
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        // Attempt to login and get a valid JWT token for the test user
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(VALID_PASSWORD);

        ResponseEntity<JwtAuthenticationResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort("/api/v1/auth/login"),
            loginRequest,
            JwtAuthenticationResponse.class
        );

        // Ensure login was successful and we received a token
        if (loginResponse.getStatusCode() == HttpStatus.OK && loginResponse.getBody() != null && loginResponse.getBody().getToken() != null) {
            headers.setBearerAuth(loginResponse.getBody().getToken());
        } else {
            // If authentication fails here, something is wrong with the test setup or login endpoint.
            // Let the test fail clearly rather than proceeding with invalid/mock headers.
            throw new IllegalStateException("Failed to authenticate test user '" + VALID_EMAIL + "' for integration test. Status: " + loginResponse.getStatusCode());
        }
        
        // The User-Id header is no longer needed with JWT authentication via SecurityContextHolder
        return headers;
    }

    
    // ----- CREATE tests -----
    
    @Test
    @Order(1)
    public void testCreateAccountSuccess() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(accountRepository.findByEmail("new@example.com").isPresent());
    }
    
    @Test
    @Order(2)
    public void testCreateGameOwnerSuccess() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("owner@example.com");
        request.setUsername("gameowner");
        request.setPassword("ownerpass123");
        request.setGameOwner(true);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        
        Account created = accountRepository.findByEmail("owner@example.com").orElse(null);
        assertNotNull(created);
        assertTrue(created instanceof GameOwner);
    }
    
    @Test
    @Order(3)
    public void testCreateAccountWithDuplicateEmail() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail(VALID_EMAIL);  // Same as the existing testAccount
        request.setUsername("different");
        request.setPassword("different123");
        request.setGameOwner(false);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(4)
    public void testCreateAccountWithInvalidData() {
        // Missing email
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);
        
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ----- UPDATE tests -----
    
    @Test
@Order(5)
public void testUpdateAccountSuccess() {
    UpdateAccountRequest request = new UpdateAccountRequest();
    request.setEmail(VALID_EMAIL);
    request.setUsername("updateduser");
    // Provide the current plain text password for verification by the service
    request.setPassword(VALID_PASSWORD);
    // For new password, you might choose to send plain text if your service simply replaces it,
    // or send the encoded one if that's what your service expects. Here we'll send plain text.
    request.setNewPassword("newpassword123");
    
    HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
    ResponseEntity<String> response = restTemplate.exchange(
        createURLWithPort(BASE_URL),
        HttpMethod.PUT,
        requestEntity,
        String.class
    );
    
    assertEquals(HttpStatus.OK, response.getStatusCode());
    Account updated = accountRepository.findByEmail(VALID_EMAIL).orElse(null);
    assertNotNull(updated);
    assertEquals("updateduser", updated.getName());
}
    
    @Test
    @Order(6)
    public void testUpdateAccountWithWrongPassword() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword("wrongpassword");
        request.setNewPassword("newpassword123");
        
        HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    @Test
    @Order(7)
    public void testUpdateNonExistentAccount() {
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("nonexistent@example.com");
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD);
        request.setNewPassword("newpassword123");
        
        HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
    
    // ----- DELETE tests -----
    
    @Test
    @Order(8)
    public void testDeleteAccountSuccess() {
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + VALID_EMAIL),
            HttpMethod.DELETE,
            requestEntity,
            String.class
        );
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
    
    @Test
    @Order(9)
    public void testDeleteNonExistentAccount() {
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/nonexistent@example.com"),
            HttpMethod.DELETE,
            requestEntity,
            String.class
        );
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
