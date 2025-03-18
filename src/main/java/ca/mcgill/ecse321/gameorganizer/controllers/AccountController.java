package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.dto.requests.CreateAccountRequest;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
public class AccountController {


    private final AccountService accountService;


    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping()
    public ResponseEntity<String> createAnAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @PostMapping
    public ResponseEntity<String> updateAccount(@RequestBody) {
        return acco
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAccount() {

    }

    @PostMapping
    public ResponseEntity<String> upgradeAccountToGameOwner() {

    }

    @GetMapping
    public ResponseEntity<?> getAccount() {

    }

}
