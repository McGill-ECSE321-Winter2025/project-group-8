package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.List;

import lombok.Data;

@Data
public class AccountResponse {

    private String username;

    private List<EventResponse> events;

    private boolean isGameOwner;

    public AccountResponse(
            String username,
            List<EventResponse> events,
            boolean isGameOwner)
    {}

}