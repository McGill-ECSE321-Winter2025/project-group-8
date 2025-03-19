package ca.mcgill.ecse321.gameorganizer.integration;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer; // if you need ordering
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
import ca.mcgill.ecse321.gameorganizer.dto.LoginResponse;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestConfig.class, SecurityTestConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)

public class AuthenticationIntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;
    
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

    // Helper method to build the URL
    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    // ----- CREATE METHODS (Login) - 4 tests -----

    @Test
    @Order(1)
    public void testLoginSuccess() {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword(VALID_PASSWORD);

        ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            LoginResponse.class
        );

        // Expect 200 OK with proper body details
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(VALID_EMAIL, response.getBody().getEmail());
        assertNotNull(response.getBody().getUserId());
    }

    @Test
    @Order(2)
    public void testLoginWithInvalidCredentials() {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        request.setPassword("wrongpassword");

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            String.class
        );

        // Expect 401 UNAUTHORIZED when password is wrong
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void testLoginWithNonExistentAccount() {
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail("nonexistent@example.com");
        request.setPassword(VALID_PASSWORD);

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            String.class
        );

        // Expect 401 UNAUTHORIZED for a non-existent account
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @Order(4)
    public void testLoginWithMissingFields() {
        // For example, missing password should result in a BAD_REQUEST
        AuthenticationDTO request = new AuthenticationDTO();
        request.setEmail(VALID_EMAIL);
        // Password not provided

        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            request,
            String.class
        );

        // Expect 400 BAD_REQUEST due to missing required field
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ----- DELETE METHODS (Logout) - 3 tests -----

    @Test
    @Order(5)
    public void testLogoutSuccess() {
        // First login to obtain a session cookie
        AuthenticationDTO loginRequest = new AuthenticationDTO();
        loginRequest.setEmail(VALID_EMAIL);
        loginRequest.setPassword(VALID_PASSWORD);

        ResponseEntity<LoginResponse> loginResponse = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/login"),
            loginRequest,
            LoginResponse.class
        );

        String sessionId = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, sessionId);

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/logout"),
            HttpMethod.POST,
            new HttpEntity<>(headers),
            String.class
        );

        // Expect 200 OK on logout
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(6)
    public void testLogoutWithoutLogin() {
        // Without a session, logout should be handled gracefully
        ResponseEntity<String> response = restTemplate.postForEntity(
            createURLWithPort(BASE_URL + "/logout"),
            null,
            String.class
        );

        // Expect 200 OK even without a session
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @Order(7)
    public void testLogoutWithInvalidSession() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "invalidSessionCookie");

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/logout"),
            HttpMethod.POST,
            new HttpEntity<>(headers),
            String.class
        );

        // Depending on implementation, an invalid session might still return OK.
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    // ----- UPDATE METHODS (Reset Password) - 3 tests -----

    @Test
    @Order(8)
    public void testResetPasswordSuccess() {
        String newPassword = "newpassword123";

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/reset-password?email=" + VALID_EMAIL + "&newPassword=" + newPassword),
            HttpMethod.POST,
            null,
            String.class
        );

        // Expect password reset to be successful (200 OK)
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify that logging in with the new password succeeds
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
    @Order(9)
    public void testResetPasswordForNonExistentAccount() {
        String newPassword = "newpassword123";

        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/reset-password?email=nonexistent@example.com&newPassword=" + newPassword),
            HttpMethod.POST,
            null,
            String.class
        );

        // Expect 400 BAD_REQUEST for non-existent account
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @Order(10)
    public void testResetPasswordWithMissingNewPassword() {
        // Attempt to reset password without providing newPassword parameter
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort(BASE_URL + "/reset-password?email=" + VALID_EMAIL),
            HttpMethod.POST,
            null,
            String.class
        );

        // Expect 400 BAD_REQUEST due to missing newPassword
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
