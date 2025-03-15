package ca.mcgill.ecse321.gameorganizer.requests;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;

    private String password;
}
