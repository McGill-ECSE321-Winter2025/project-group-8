package ca.mcgill.ecse321.gameorganizer.integration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class AuthenticationIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private Account testAccount;
    private static final String BASE_URL = "/auth";
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_USERNAME = "testuser";
    private static final String VALID_PASSWORD = "password123";

    @BeforeEach
    public void setup() {
        testAccount = new Account(VALID_USERNAME, VALID_EMAIL, VALID_PASSWORD);
        testAccount = accountRepository.save(testAccount);
    }

    @AfterEach
    public void cleanup() {
        accountRepository.deleteAll();
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + "/api" + uri;
    }

    @Test
    public void testLoginSuccess() {
        // Create login request
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        // Send request
        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            LoginResponse.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_EMAIL, response.getBody().getEmail());
        assertNotNull(response.getBody().getUserId());
    }

    @Test
    public void testLoginWithInvalidCredentials() {
        // Create login request with wrong password
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword("wrongpassword");

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testLoginWithNonExistentAccount() {
        // Create login request with non-existent email
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail("nonexistent@example.com");
        request.setPassword(VALID_PASSWORD);

        // Send request
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testLogoutSuccess() {
        // First login to get session
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(VALID_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            loginRequest,
            LoginResponse.class
        );

        // Get session cookie from login response
        String sessionId = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionId);

        // Send logout request with session
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/logout"),
            HttpMethod.POST,
            new HttpEntity<>(headers),
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testLogoutWithoutLogin() {
        // Send logout request without session
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/logout"),
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testResetPasswordSuccess() {
        // Send reset password request
        String newPassword = "newpassword123";
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/reset-password?email=" + VALID_EMAIL + "&newPassword=" + newPassword),
            HttpMethod.POST,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Try logging in with new password
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(newPassword);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            loginRequest,
            LoginResponse.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
    }

    @Test
    public void testResetPasswordForNonExistentAccount() {
        // Send reset password request for non-existent account
        String newPassword = "newpassword123";
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/reset-password?email=nonexistent@example.com&newPassword=" + newPassword),
            HttpMethod.POST,
            null,
            String.class
        );

        // Verify
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
