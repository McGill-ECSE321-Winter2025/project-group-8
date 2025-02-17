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

    private String category;

    @ManyToOne
    private GameOwner owner;

    public Game(String aName, int aMinPlayers, int aMaxPlayers, String aImage, Date aDateAdded) {
        name = aName;
        minPlayers = aMinPlayers;
        maxPlayers = aMaxPlayers;
        image = aImage;
        dateAdded = aDateAdded;
        this.category = "Uncategorized"; // Default category
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
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; 
        }
        if (!(obj instanceof Game)) {
            return false; 
        }
        Game game = (Game) obj;
        
        return this.id == game.id &&
               this.minPlayers == game.minPlayers &&
               this.maxPlayers == game.maxPlayers &&
               (this.name != null ? this.name.equals(game.name) : game.name == null) &&
               (this.image != null ? this.image.equals(game.image) : game.image == null) &&
               (this.dateAdded != null ? this.dateAdded.equals(game.dateAdded) : game.dateAdded == null);
    }
    

};
