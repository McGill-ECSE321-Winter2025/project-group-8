package ca.mcgill.ecse321.gameorganizer.dtos;

import ca.mcgill.ecse321.gameorganizer.models.Review;
import java.util.Date;

/**
 * DTO for representing review data in API responses.
 * Contains review details and associated game and reviewer information.
 *
 * @Author Alexander
 */
public class ReviewResponseDto {
    private int id;
    private int rating;
    private String comment;
    private Date dateSubmitted;
    private Integer gameId;
    private String gameTitle;
    private String reviewerId;
    private String reviewerName;

    /**
     * Default constructor
     */
    public ReviewResponseDto() {
    }

    /**
     * Constructs a ResponseDto from a Review entity
     *
     * @param review The review entity to convert
     */
    public ReviewResponseDto(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.dateSubmitted = review.getDateSubmitted();

        if (review.getGameReviewed() != null) {
            this.gameId = review.getGameReviewed().getId();
            this.gameTitle = review.getGameReviewed().getName();
        }

        if (review.getReviewer() != null) {
            this.reviewerId = review.getReviewer().getEmail();
            this.reviewerName = review.getReviewer().getName();
        }
    }

    /**
     * Gets the review ID
     *
     * @return The unique identifier for the review
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the review ID
     *
     * @param id The unique identifier for the review
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the review rating
     *
     * @return The numerical rating (1-5) given to the game
     */
    public int getRating() {
        return rating;
    }

    /**
     * Sets the review rating
     *
     * @param rating The numerical rating (1-5) given to the game
     */
    public void setRating(int rating) {
        this.rating = rating;
    }

    /**
     * Gets the review comment
     *
     * @return The textual feedback about the game
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the review comment
     *
     * @param comment The textual feedback about the game
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the date when the review was submitted
     *
     * @return The submission date of the review
     */
    public Date getDateSubmitted() {
        return dateSubmitted;
    }

    /**
     * Sets the date when the review was submitted
     *
     * @param dateSubmitted The submission date of the review
     */
    public void setDateSubmitted(Date dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    /**
     * Gets the ID of the game being reviewed
     *
     * @return The game ID, or null if game reference is not available
     */
    public Integer getGameId() {
        return gameId;
    }

    /**
     * Sets the ID of the game being reviewed
     *
     * @param gameId The game ID
     */
    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    /**
     * Gets the title of the game being reviewed
     *
     * @return The game title, or null if game reference is not available
     */
    public String getGameTitle() {
        return gameTitle;
    }

    /**
     * Sets the title of the game being reviewed
     *
     * @param gameTitle The game title
     */
    public void setGameTitle(String gameTitle) {
        this.gameTitle = gameTitle;
    }

    /**
     * Gets the email of the reviewer
     *
     * @return The reviewer's email, or null if reviewer reference is not available
     */
    public String getReviewerId() {
        return reviewerId;
    }

    /**
     * Sets the email of the reviewer
     *
     * @param reviewerId The reviewer's email
     */
    public void setReviewerId(String reviewerId) {
        this.reviewerId = reviewerId;
    }

    /**
     * Gets the name of the reviewer
     *
     * @return The reviewer's name, or null if reviewer reference is not available
     */
    public String getReviewerName() {
        return reviewerName;
    }

    /**
     * Sets the name of the reviewer
     *
     * @param reviewerName The reviewer's name
     */
    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    /**
     * Returns a string representation of the ReviewResponseDto
     *
     * @return A string containing all DTO fields
     */
    @Override
    public String toString() {
        return "ReviewResponseDto{" +
                "id=" + id +
                ", rating=" + rating +
                ", comment='" + comment + '\'' +
                ", dateSubmitted=" + dateSubmitted +
                ", gameId=" + gameId +
                ", gameTitle='" + gameTitle + '\'' +
                ", reviewerId='" + reviewerId + '\'' +
                ", reviewerName='" + reviewerName + '\'' +
                '}';
    }
}