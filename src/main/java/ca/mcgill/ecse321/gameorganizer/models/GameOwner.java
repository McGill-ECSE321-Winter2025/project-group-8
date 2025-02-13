package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class GameOwner extends Account {

    public GameOwner(String aName, String aEmail, String aPassword) {
        super(aName, aEmail, aPassword);
    }

    public void delete() {
        super.delete();
    }

}