package ca.mcgill.ecse321.gameorganizer.repositories;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.mcgill.ecse321.gameorganizer.models.Review;

/**
 * Repository interface for managing Review entities.
 * Provides CRUD operations and custom queries for reviews.
 * 
 * @author @jiwoong0815
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    /**
     * Finds a review by its unique identifier.
     *
     * @param id the ID of the review to find
     * @return Optional containing the review if found, empty otherwise
     */
    Optional<Review> findReviewById(int id);

    /**
     * Finds review(s) by the name of its reviewer
     *
     * @param username the name of the reviewer
     * @return Optional containing the review if found, empty otherwise
     */
    List<Review> findReviewsByReviewerName(String username);
}
