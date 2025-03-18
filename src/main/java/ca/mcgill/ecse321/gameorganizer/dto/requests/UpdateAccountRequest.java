package ca.mcgill.ecse321.gameorganizer.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public class UpdateAccountRequest {
    @NotEmpty(message = "Email address is required")
    @Email(message = "Email address is not in a valid format")
    private String email;

    @NotEmpty(message = "Username is required")
    private String username;

    @NotEmpty(message = "Password is required")
    private String password;

    @NotEmpty(message = "New password is required")
    private String newPassword;

}
