package ca.mcgill.ecse321.gameorganizer.dtos;

/**
 * DTO for game creation and update requests
 */
public class GameCreationDto {
    private String name;
    private int minPlayers;
    private int maxPlayers;
    private String image;
    private String category;
    private String ownerId;

    // Default constructor
    public GameCreationDto() {
    }

    // Constructor with all fields
    public GameCreationDto(String name, int minPlayers, int maxPlayers, String image, String category, String ownerId) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.image = image;
        this.category = category;
        this.ownerId = ownerId;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}