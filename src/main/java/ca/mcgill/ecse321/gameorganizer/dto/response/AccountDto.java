package ca.mcgill.ecse321.gameorganizer.dto.response;

import ca.mcgill.ecse321.gameorganizer.models.Account;

public class AccountDto {
    private int id;
    private String name;
    private String email;

    public AccountDto() {}

    public AccountDto(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.email = account.getEmail();
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
} 