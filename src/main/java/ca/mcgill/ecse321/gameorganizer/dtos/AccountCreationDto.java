package ca.mcgill.ecse321.gameorganizer.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for account creation and update requests
 */
public class AccountCreationDto {

    @NotEmpty(message = "Email address is required")
    @Email(message = "Email address is not in a valid format")
    private String email;

    @NotEmpty(message = "Username is required")
    private String username;

    @NotEmpty(message = "Password is required")
    private String password;

    private boolean gameOwner;

    // Default constructor
    public AccountCreationDto() {
    }

    // Constructor with all fields
    public AccountCreationDto(String email, String username, String password, boolean gameOwner) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.gameOwner = gameOwner;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isGameOwner() {
        return gameOwner;
    }

    public void setGameOwner(boolean gameOwner) {
        this.gameOwner = gameOwner;
    }
}