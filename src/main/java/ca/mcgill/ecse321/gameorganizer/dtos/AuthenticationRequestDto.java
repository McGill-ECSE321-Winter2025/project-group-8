package ca.mcgill.ecse321.gameorganizer.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

/**
 * DTO for authentication requests
 */
public class AuthenticationRequestDto {

    @NotEmpty(message = "Email address is required")
    @Email(message = "Email address is not in a valid format")
    private String email;

    @NotEmpty(message = "Password is required")
    private String password;

    // Default constructor
    public AuthenticationRequestDto() {
    }

    // Constructor with all fields
    public AuthenticationRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}