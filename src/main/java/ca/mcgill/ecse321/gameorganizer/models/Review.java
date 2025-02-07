package ca.mcgill.ecse321.gameorganizer.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    private int rating;

    private String comment;

    private Date dateSubmitted;

    public Review(int aRating, String aComment, Date aDateSubmitted) {
        rating = aRating;
        comment = aComment;
        dateSubmitted = aDateSubmitted;
    }

    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "rating" + ":" + getRating() + "," +
                "comment" + ":" + getComment() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateSubmitted" + "=" + (getDateSubmitted() != null ? !getDateSubmitted().equals(this) ? getDateSubmitted().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator");
    }
}
