package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.List;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;

/**
 * Data Transfer Object for Account responses in the API
 */
public class AccountResponseDto {
    private int id;
    private String name;
    private String email;
    private boolean isGameOwner;
    private List<EventResponseDto> registeredEvents;

    /**
     * Constructs a basic AccountResponseDto from an Account entity
     *
     * @param account The account entity to convert to DTO
     */
    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.name = account.getName();
        this.email = account.getEmail();
        this.isGameOwner = account instanceof GameOwner;
    }

    /**
     * Constructs an AccountResponseDto with registered events
     *
     * @param account The account entity to convert to DTO
     * @param events List of events the account is registered for
     */
    public AccountResponseDto(Account account, List<EventResponseDto> events) {
        this(account);
        this.registeredEvents = events;
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public boolean isGameOwner() {
        return isGameOwner;
    }

    public List<EventResponseDto> getRegisteredEvents() {
        return registeredEvents;
    }
}