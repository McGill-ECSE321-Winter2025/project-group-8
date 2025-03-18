package ca.mcgill.ecse321.gameorganizer.repository;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @AfterEach
    public void clearDatabase() {
        accountRepository.deleteAll();
    }

    @Test
    public void testPersistAndLoadAccount() {
        // Create test account
        String name = "TestUser";
        String email = "test@example.com";
        String password = "password123";

        Account account = new Account(name, email, password);

        // Save to database
        account = accountRepository.save(account);
        int id = account.getId();

        // Retrieve from database
        Account accountFromDb = accountRepository.findById(id).orElse(null);

        // Assert
        assertNotNull(accountFromDb);
        assertEquals(name, accountFromDb.getName());
        assertEquals(email, accountFromDb.getEmail());
        assertEquals(password, accountFromDb.getPassword());
    }

    @Test
    public void testFindByEmail() {
        // Create test accounts
        Account account1 = new Account("User1", "user1@example.com", "password1");
        Account account2 = new Account("User2", "user2@example.com", "password2");

        accountRepository.save(account1);
        accountRepository.save(account2);

        // Find by email
        Optional<Account> foundAccount = accountRepository.findByEmail("user1@example.com");

        // Assert
        assertTrue(foundAccount.isPresent());
        assertEquals("User1", foundAccount.get().getName());
        assertEquals("user1@example.com", foundAccount.get().getEmail());
    }

    @Test
    public void testFindByEmailNotFound() {
        // Create test account
        Account account = new Account("User", "user@example.com", "password");
        accountRepository.save(account);

        // Find by non-existent email
        Optional<Account> foundAccount = accountRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(foundAccount.isPresent());
    }

    @Test
    public void testUpdateAccount() {
        // Create test account
        Account account = new Account("InitialUser", "update@example.com", "initialPassword");
        account = accountRepository.save(account);
        int id = account.getId();

        // Update the account
        account.setName("UpdatedUser");
        account.setPassword("updatedPassword");
        accountRepository.save(account);

        // Retrieve updated account
        Account updatedAccount = accountRepository.findById(id).orElse(null);

        // Assert
        assertNotNull(updatedAccount);
        assertEquals("UpdatedUser", updatedAccount.getName());
        assertEquals("update@example.com", updatedAccount.getEmail());
        assertEquals("updatedPassword", updatedAccount.getPassword());
    }

    @Test
    public void testDeleteAccount() {
        // Create test account
        Account account = new Account("DeleteUser", "delete@example.com", "password");
        account = accountRepository.save(account);
        int id = account.getId();

        // Verify account exists
        assertTrue(accountRepository.findById(id).isPresent());

        // Delete the account
        accountRepository.delete(account);

        // Verify account no longer exists
        assertFalse(accountRepository.findById(id).isPresent());
    }

    @Test
    public void testSaveMultipleAccounts() {
        // Create test accounts
        Account account1 = new Account("User1", "multi1@example.com", "password1");
        Account account2 = new Account("User2", "multi2@example.com", "password2");
        Account account3 = new Account("User3", "multi3@example.com", "password3");

        // Save all accounts
        accountRepository.save(account1);
        accountRepository.save(account2);
        accountRepository.save(account3);

        // Count total accounts
        long count = accountRepository.count();

        // Assert
        assertEquals(3, count);
    }

    @Test
    public void testUniqueEmailConstraint() {
        // Create and save first account
        Account account1 = new Account("FirstUser", "duplicate@example.com", "password1");
        accountRepository.save(account1);

        // Create second account with same email
        Account account2 = new Account("SecondUser", "duplicate@example.com", "password2");

        // With unique=true on email field, attempting to save a duplicate email
        // should throw an exception
        assertThrows(Exception.class, () -> {
            accountRepository.save(account2);
            accountRepository.flush(); // Force the persistence context to be flushed
        });

        // Verify only the first account with this email exists
        Optional<Account> foundAccount = accountRepository.findByEmail("duplicate@example.com");
        assertTrue(foundAccount.isPresent());
        assertEquals("FirstUser", foundAccount.get().getName());
    }
}