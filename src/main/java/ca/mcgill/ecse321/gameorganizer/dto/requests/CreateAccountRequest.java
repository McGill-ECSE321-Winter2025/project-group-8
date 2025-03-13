package ca.mcgill.ecse321.gameorganizer.dto.requests;

import lombok.Data;

@Data
public class CreateAccountRequest {

    private String username;

    private String email;

    private String password;

    private boolean isGameOwner;

}
