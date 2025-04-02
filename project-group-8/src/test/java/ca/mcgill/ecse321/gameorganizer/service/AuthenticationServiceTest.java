package ca.mcgill.ecse321.gameorganizer.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.exceptions.EmailNotFoundException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidPasswordException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.AuthenticationService;
import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthenticationService authenticationService;

    // Test constants
    private static final String VALID_EMAIL = "test@example.com";
    private static final String VALID_PASSWORD = "password123";
    private static final String ENCODED_PASSWORD = "encodedPassword123";
    private static final int VALID_USER_ID = 1;

    @Test
    public void testLoginSuccess() {
        // Setup
        AuthenticationDTO authDTO = new AuthenticationDTO(VALID_EMAIL, VALID_PASSWORD);
        Account account = new Account();
        account.setId(VALID_USER_ID);
        account.setEmail(VALID_EMAIL);
        account.setPassword(ENCODED_PASSWORD);

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(VALID_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);

        // Test
        Account result = authenticationService.login(authDTO, session);

        // Verify
        assertNotNull(result);
        assertEquals(VALID_EMAIL, result.getEmail());
        verify(session).setAttribute("userId", VALID_USER_ID);
        verify(accountRepository).findByEmail(VALID_EMAIL);
        verify(passwordEncoder).matches(VALID_PASSWORD, ENCODED_PASSWORD);
    }

    @Test
    public void testLoginFailureInvalidEmail() {
        // Setup
        AuthenticationDTO authDTO = new AuthenticationDTO("wrong@example.com", VALID_PASSWORD);
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(authDTO, session);
        });
        verify(accountRepository).findByEmail("wrong@example.com");
        verify(session, times(0)).setAttribute(any(), any());
    }

    @Test
    public void testLoginFailureInvalidPassword() {
        // Setup
        AuthenticationDTO authDTO = new AuthenticationDTO(VALID_EMAIL, "wrongpassword");
        Account account = new Account();
        account.setEmail(VALID_EMAIL);
        account.setPassword(ENCODED_PASSWORD);

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Test & Verify
        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(authDTO, session);
        });
        verify(accountRepository).findByEmail(VALID_EMAIL);
        verify(session, times(0)).setAttribute(any(), any());
    }

    @Test
    public void testLoginWithNullCredentials() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.login(null, session);
        });
        verify(accountRepository, times(0)).findByEmail(any());
        verify(session, times(0)).setAttribute(any(), any());
    }

    @Test
    public void testLogoutSuccess() {
        // Test
        String result = authenticationService.logout(session);

        // Verify
        assertEquals("Successfully logged out", result);
        verify(session).invalidate();
    }

    @Test
    public void testLogoutWithNullSession() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.logout(null);
        });
    }

    @Test
    public void testResetPasswordSuccess() {
        // Setup
        Account account = new Account();
        account.setEmail(VALID_EMAIL);
        account.setPassword(ENCODED_PASSWORD);

        when(accountRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        // Test
        String result = authenticationService.resetPassword(VALID_EMAIL, VALID_PASSWORD);

        // Verify
        assertEquals("Password updated successfully", result);
        verify(accountRepository).findByEmail(VALID_EMAIL);
        verify(accountRepository).save(account);
        verify(passwordEncoder).encode(VALID_PASSWORD);
    }

    @Test
    public void testResetPasswordEmailNotFound() {
        // Setup
        when(accountRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Test & Verify
        assertThrows(EmailNotFoundException.class, () -> {
            authenticationService.resetPassword("nonexistent@example.com", VALID_PASSWORD);
        });
        verify(accountRepository).findByEmail("nonexistent@example.com");
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    public void testResetPasswordTooShort() {
        // Test & Verify
        assertThrows(InvalidPasswordException.class, () -> {
            authenticationService.resetPassword(VALID_EMAIL, "short");
        });
        verify(accountRepository, times(0)).findByEmail(any());
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    public void testResetPasswordWithNullPassword() {
        // Test & Verify
        assertThrows(InvalidPasswordException.class, () -> {
            authenticationService.resetPassword(VALID_EMAIL, null);
        });
        verify(accountRepository, times(0)).findByEmail(any());
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    public void testResetPasswordWithEmptyPassword() {
        // Test & Verify
        assertThrows(InvalidPasswordException.class, () -> {
            authenticationService.resetPassword(VALID_EMAIL, "");
        });
        verify(accountRepository, times(0)).findByEmail(any());
        verify(accountRepository, times(0)).save(any());
    }

    @Test
    public void testResetPasswordWithNullEmail() {
        // Test & Verify
        assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.resetPassword(null, VALID_PASSWORD);
        });
        verify(accountRepository, times(0)).findByEmail(any());
        verify(accountRepository, times(0)).save(any());
    }
}
