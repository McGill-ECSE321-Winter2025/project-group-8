package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a user account in the game organization system.
 * This is the base class for all types of user accounts.
 *
 * @author @dyune
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // Methods

    /**
     * Creates a new Account with the specified details.
     *
     * @param aName     The display name of the account holder
     * @param aEmail    The email address associated with the account (unique identifier)
     * @param aPassword The password for account authentication
     */
    public Account(String aName, String aEmail, String aPassword) {
        name = aName;
        email = aEmail;
        password = aPassword;
    }

    /**
     * Performs cleanup operations when deleting the account.
     */
    public void delete() {
    }

    /**
     * Returns a string representation of the Account.
     *
     * @return A string containing the account's ID, name, email, and password
     */
    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "name" + ":" + getName() + "," +
                "email" + ":" + getEmail() + "," +
                "password" + ":" + getPassword() + "]";
    }
}
