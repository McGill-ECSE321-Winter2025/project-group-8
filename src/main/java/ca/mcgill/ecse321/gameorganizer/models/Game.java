package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    private int minPlayers;

    private int maxPlayers;

    private String image;

    private Date dateAdded;

    @OneToMany
    private List<Review> reviews;

    public Game(String aName, int aMinPlayers, int aMaxPlayers, String aImage, Date aDateAdded) {
        name = aName;
        minPlayers = aMinPlayers;
        maxPlayers = aMaxPlayers;
        image = aImage;
        dateAdded = aDateAdded;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "name" + ":" + getName() + "," +
                "minPlayers" + ":" + getMinPlayers() + "," +
                "maxPlayers" + ":" + getMaxPlayers() + "," +
                "image" + ":" + getImage() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateAdded" + "=" + (getDateAdded() != null ? !getDateAdded().equals(this) ? getDateAdded().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator");
    }
};
