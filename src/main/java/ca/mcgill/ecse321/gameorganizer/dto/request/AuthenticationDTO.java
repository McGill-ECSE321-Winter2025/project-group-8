package ca.mcgill.ecse321.gameorganizer.dto.request;

/**
 * Data Transfer Object for authentication.
 * Contains email and password fields.
 * 
 * @author Shayan
 */
public class AuthenticationDTO {

    private String email;
    private String password;

    /**
     * Default constructor.
     */
    public AuthenticationDTO() {
    }

    /**
     * Constructs an AuthenticationDTO with the specified email and password.
     * 
     * @param email the email address
     * @param password the password
     */
    public AuthenticationDTO(String email, String password) {
        this.email = email;
        this.password = password;
    }

    /**
     * Returns the email address.
     * 
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address.
     * 
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the password.
     * 
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     * 
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}