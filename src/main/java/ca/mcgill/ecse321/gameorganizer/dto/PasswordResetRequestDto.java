package ca.mcgill.ecse321.gameorganizer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PasswordResetRequestDto {
    private String email;
}