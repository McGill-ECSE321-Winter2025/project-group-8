package ca.mcgill.ecse321.gameorganizer.repository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;

public class AccountRepositoryTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Account testAccount;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testAccount = new Account("TestUser", "test@example.com", "password123");
    }

    @Test
    public void testCreateAccountSuccess() {
        when(accountRepository.findByEmail(any(String.class))).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        ResponseEntity<String> response = accountService.createAccount(testAccount);

        assertEquals("Account created", response.getBody());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testCreateAccountFailure() {
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.createAccount(testAccount);
        });

        assertEquals("Account with email test@example.com already exists", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testGetAccountSuccess() {
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        Account foundAccount = accountService.getAccountByEmail(testAccount.getEmail());

        assertNotNull(foundAccount);
        assertEquals("TestUser", foundAccount.getName());
        assertEquals("test@example.com", foundAccount.getEmail());
        verify(accountRepository, times(1)).findByEmail(any(String.class));
    }

    @Test
    public void testGetAccountFailure() {
        when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.getAccountByEmail("nonexistent@example.com");
        });

        assertEquals("Account with email nonexistent@example.com does not exist", exception.getMessage());
    }

    @Test
    public void testUpdateAccountSuccess() {
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        ResponseEntity<String> response = accountService.updateAccountByEmail(
                testAccount.getEmail(), "UpdatedUser", "newpassword123");

        assertEquals("Account updated successfully", response.getBody());
        assertEquals("UpdatedUser", testAccount.getName());
        assertEquals("newpassword123", testAccount.getPassword());

        verify(accountRepository, times(1)).findByEmail(testAccount.getEmail());
    }

    @Test
    public void testUpdateAccountFailure() {
        when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.updateAccountByEmail("nonexistent@example.com", "NewUser", "newpassword");
        });

        assertEquals("Account with email nonexistent@example.com does not exist", exception.getMessage());
    }

    @Test
    public void testDeleteAccountSuccess() {
        when(accountRepository.findByEmail(testAccount.getEmail())).thenReturn(Optional.of(testAccount));

        ResponseEntity<String> response = accountService.deleteAccountByEmail(testAccount.getEmail());

        assertEquals("Account with email test@example.com has been deleted", response.getBody());
        verify(accountRepository, times(1)).delete(testAccount);
    }

    @Test
    public void testDeleteAccountFailure() {
        when(accountRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            accountService.deleteAccountByEmail("nonexistent@example.com");
        });

        assertEquals("Account with email nonexistent@example.com does not exist", exception.getMessage());
        verify(accountRepository, never()).delete(any(Account.class));
    }
}
