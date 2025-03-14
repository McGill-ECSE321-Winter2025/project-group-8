package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {


    private final AccountService accountService;


    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }



}
