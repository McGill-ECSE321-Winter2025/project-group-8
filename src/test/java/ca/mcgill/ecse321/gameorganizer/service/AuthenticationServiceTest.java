package ca.mcgill.ecse321.gameorganizer.service;

import java.util.Optional;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import ca.mcgill.ecse321.gameorganizer.dto.AuthenticationDTO;
import ca.mcgill.ecse321.gameorganizer.exceptions.InvalidCredentialsException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.AuthenticationService;

public class AuthenticationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess() {
        AuthenticationDTO authDTO = new AuthenticationDTO("test@example.com", "password");
        Account account = new Account();
        account.setEmail("test@example.com");
        account.setPassword("encodedPassword");

        when(accountRepository.findByEmail(authDTO.getEmail())).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(authDTO.getPassword(), account.getPassword())).thenReturn(true);

        Account result = authenticationService.login(authDTO, session);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    public void testLoginFailureInvalidCredentials() {
        AuthenticationDTO authDTO = new AuthenticationDTO("test@example.com", "wrongPassword");

        when(accountRepository.findByEmail(authDTO.getEmail())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.login(authDTO, session);
        });
    }

    @Test
    public void testLogout() {
        String result = authenticationService.logout(session);

        verify(session, times(1)).invalidate();
        assertEquals("Successfully logged out", result);
    }

    @Test
    public void testResetPasswordSuccess() {
        String email = "test@example.com";
        String newPassword = "newPassword";
        Account account = new Account();
        account.setEmail(email);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        String result = authenticationService.resetPassword(email, newPassword);

        verify(accountRepository, times(1)).save(account);
        assertEquals("Password updated successfully", result);
    }

    @Test
    public void testResetPasswordFailureEmailNotFound() {
        String email = "test@example.com";
        String newPassword = "newPassword";

        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        String result = authenticationService.resetPassword(email, newPassword);

        assertEquals("Email not found", result);
    }
}
