package ca.mcgill.ecse321.gameorganizer.integration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import ca.mcgill.ecse321.gameorganizer.config.SecurityTestConfig;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;
import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityTestConfig.class})
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
    
    private HttpHeaders createAuthHeaders() {
        // Login to get the session cookie
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(VALID_PASSWORD);
        
        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort("/auth/login"),
            loginRequest,
            LoginResponse.class
        );
        
        String sessionId = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionId);
        headers.set("User-Id", String.valueOf(testAccount.getId()));
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
    // Instead of using VALID_PASSWORD directly, use the encoded password from testAccount
    request.setPassword(testAccount.getPassword());
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
        assertFalse(accountRepository.findByEmail(VALID_EMAIL).isPresent());
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
