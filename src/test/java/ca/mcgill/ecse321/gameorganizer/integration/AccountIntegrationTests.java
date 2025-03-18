package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import ca.mcgill.ecse321.gameorganizer.config.TestConfig;

import ca.mcgill.ecse321.gameorganizer.dto.AccountResponse;
import ca.mcgill.ecse321.gameorganizer.dto.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.dto.UpdateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AccountIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;
    private static final String BASE_URL = "/account";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        // Clean up before each test
        accountRepository.deleteAll();
        
        // Create test account
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
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
        headers.set("User-Id", String.valueOf(testAccount.getId()));
        return headers;
    }

    @Test
    @Order(1)
    public void testCreateAccountSuccess() {
        // Create request
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("new@example.com");
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);

        // Send request - no auth needed for account creation
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(accountRepository.findByEmail("new@example.com").isPresent());
    }

    @Test
    @Order(2)
    public void testCreateGameOwnerSuccess() {
        // Create request
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail("owner@example.com");
        request.setUsername("gameowner");
        request.setPassword("ownerpass123");
        request.setGameOwner(true);

        // Send request - no auth needed for account creation
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Account created = accountRepository.findByEmail("owner@example.com").orElse(null);
        assertNotNull(created);
        assertTrue(created instanceof GameOwner);
    }

    @Test
    @Order(3)
    public void testCreateAccountWithDuplicateEmail() {
        // Create request with existing email
        CreateAccountRequest request = new CreateAccountRequest();
        request.setEmail(VALID_EMAIL);  // Same as testAccount
        request.setUsername("different");
        request.setPassword("different123");
        request.setGameOwner(false);

        // Send request - no auth needed for account creation
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testCreateAccountWithInvalidData() {
        // Create request with missing email
        CreateAccountRequest request = new CreateAccountRequest();
        request.setUsername("newuser");
        request.setPassword("newpass123");
        request.setGameOwner(false);

        // Send request - no auth needed for account creation
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(5)
    public void testUpdateAccountSuccess() {
        // Create update request
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD);
        request.setNewPassword("newpassword123");

        // Send request with auth
        HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Account updated = accountRepository.findByEmail(VALID_EMAIL).orElse(null);
        assertNotNull(updated);
        assertEquals("updateduser", updated.getName());
    }

    @Test
    @Order(6)
    public void testUpdateAccountWithWrongPassword() {
        // Create update request with wrong password
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail(VALID_EMAIL);
        request.setUsername("updateduser");
        request.setPassword("wrongpassword");
        request.setNewPassword("newpassword123");

        // Send request with auth
        HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testUpdateNonExistentAccount() {
        // Create update request for non-existent account
        UpdateAccountRequest request = new UpdateAccountRequest();
        request.setEmail("nonexistent@example.com");
        request.setUsername("updateduser");
        request.setPassword(VALID_PASSWORD);
        request.setNewPassword("newpassword123");

        // Send request with auth
        HttpEntity<UpdateAccountRequest> requestEntity = new HttpEntity<>(request, createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(8)
    public void testDeleteAccountSuccess() {
        // Send delete request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + VALID_EMAIL),
            HttpMethod.DELETE,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(accountRepository.findByEmail(VALID_EMAIL).isPresent());
    }

    @Test
    @Order(9)
    public void testDeleteNonExistentAccount() {
        // Send delete request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/nonexistent@example.com"),
            HttpMethod.DELETE,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void testUpgradeToGameOwnerSuccess() {
        // Send upgrade request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + VALID_EMAIL),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        Account upgraded = accountRepository.findByEmail(VALID_EMAIL).orElse(null);
        assertNotNull(upgraded);
        assertTrue(upgraded instanceof GameOwner);
    }

    @Test
    @Order(11)
    public void testUpgradeNonExistentAccount() {
        // Send upgrade request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/nonexistent@example.com"),
            HttpMethod.PUT,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(12)
    public void testGetAccountSuccess() {
        // Send get request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<AccountResponse> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + VALID_EMAIL),
            HttpMethod.GET,
            requestEntity,
            AccountResponse.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_USERNAME, response.getBody().getUsername());
        assertFalse(response.getBody().isGameOwner());
    }

    @Test
    @Order(13)
    public void testGetNonExistentAccount() {
        // Send get request with auth
        HttpEntity<?> requestEntity = new HttpEntity<>(createAuthHeaders());
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/nonexistent@example.com"),
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(14)
    public void testUnauthorizedAccess() {
        // Send request without auth header
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/" + VALID_EMAIL),
            HttpMethod.GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
