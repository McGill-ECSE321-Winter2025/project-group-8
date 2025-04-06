package ca.mcgill.ecse321.gameorganizer.dto.response;

public class UserSummaryDto {
    private int id; // Changed type from Long to int
    private String name; // Changed field from username to name
    private String email; // Added email field
    // Consider adding 'name' if it exists in Account and is needed

    // Default constructor for frameworks
    public UserSummaryDto() {
    }

    public UserSummaryDto(int id, String name) { // Updated constructor signature
        this.id = id;
        this.name = name; // Updated assignment
    }
    
    // New constructor with email
    public UserSummaryDto(int id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters
    public int getId() { // Changed return type
        return id;
    }

    public String getName() { // Changed method name and getter target
        return name;
    }
    
    public String getEmail() {
        return email;
    }

    // Setters (optional, depending on usage)
    public void setId(int id) { // Changed parameter type
        this.id = id;
    }

    public void setName(String name) { // Changed method name and parameter
        this.name = name; // Updated assignment
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}