package ca.mcgill.ecse321.gameorganizer.services;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {


    private final AccountRepository accountRepository;


    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @Transactional
    public ResponseEntity<String> createAccount(Account aNewAccount) {
        String email = aNewAccount.getEmail();

        if (accountRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Account with email " + email + " already exists");
        }

        if (aNewAccount instanceof GameOwner) {
            accountRepository.save((GameOwner) aNewAccount);
        } else {
            accountRepository.save(aNewAccount);
        }

        return ResponseEntity.ok("Account created");
    }


    @Transactional
    public Account getAccountByEmail(String email) {
        return accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
    }


    // Password is unprotected, needs to change later!
    @Transactional
    public ResponseEntity<String> updateAccountByEmail(String email, String newName, String newPassword) {
        Account account = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
        account.setName(newName);
        account.setPassword(newPassword);
        return ResponseEntity.ok("Account updated successfully");
    }


    @Transactional
    public ResponseEntity<String> deleteAccountByEmail(String email) {
        Account accountToDelete = accountRepository.findByEmail(email).orElseThrow(
                () -> new IllegalArgumentException("Account with email " + email + " does not exist")
        );
        accountRepository.delete(accountToDelete);
        return ResponseEntity.ok("Account with email " + email + " has been deleted");
    }
}
