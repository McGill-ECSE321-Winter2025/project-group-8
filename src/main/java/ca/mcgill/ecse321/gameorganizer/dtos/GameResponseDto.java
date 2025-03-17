package ca.mcgill.ecse321.gameorganizer.dtos;

import java.util.Date;
import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;

/**
 * Data Transfer Object for Game responses in the API
 */
public class GameResponseDto {
    private int id;
    private String name;
    private int minPlayers;
    private int maxPlayers;
    private String image;
    private Date dateAdded;
    private String category;
    private AccountDto owner;

    /**
     * Nested DTO for Account information
     */
    public static class AccountDto {
        private int id;
        private String name;
        private String email;

        public AccountDto(GameOwner owner) {
            this.id = owner.getId();
            this.name = owner.getName();
            this.email = owner.getEmail();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }
    }

    /**
     * Constructs a GameResponseDto from a Game entity
     *
     * @param game The game entity to convert to DTO
     */
    public GameResponseDto(Game game) {
        this.id = game.getId();
        this.name = game.getName();
        this.minPlayers = game.getMinPlayers();
        this.maxPlayers = game.getMaxPlayers();
        this.image = game.getImage();
        this.dateAdded = game.getDateAdded();
        this.category = game.getCategory();

        if (game.getOwner() != null) {
            this.owner = new AccountDto(game.getOwner());
        }
    }

    // Getters

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public String getImage() {
        return image;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public String getCategory() {
        return category;
    }

    public AccountDto getOwner() {
        return owner;
    }
}