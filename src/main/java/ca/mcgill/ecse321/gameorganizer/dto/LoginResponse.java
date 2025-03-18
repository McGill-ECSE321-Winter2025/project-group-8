package ca.mcgill.ecse321.gameorganizer.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private final Integer userId; //didn't use uuid because it has to match what is in ./models/Account

    private final String email;
}
