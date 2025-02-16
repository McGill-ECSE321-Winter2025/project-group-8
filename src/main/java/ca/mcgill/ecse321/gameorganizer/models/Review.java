package ca.mcgill.ecse321.gameorganizer.models;

import java.util.Date;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne
    @JoinColumn(name = "game_reviewed_id", nullable = true) // make sure column is nullable
    @OnDelete(action = OnDeleteAction.SET_NULL) // instructs Hibernate to set this FK to null on delete
    private Game gameReviewed;

    @ManyToOne
    private Account reviewer;

    public Review(int aRating, String aComment, Date aDateSubmitted) {
        rating = aRating;
        comment = aComment;
        dateSubmitted = aDateSubmitted;
    }

    @Override
    public String toString() {
        return super.toString() + "[" +
                "id" + ":" + getId() + "," +
                "rating" + ":" + getRating() + "," +
                "comment" + ":" + getComment() + "]" + System.getProperties().getProperty("line.separator") +
                "  " + "dateSubmitted" + "=" + (getDateSubmitted() != null ? !getDateSubmitted().equals(this) ? getDateSubmitted().toString().replaceAll("  ", "    ") : "this" : "null") + System.getProperties().getProperty("line.separator");
    }
}
