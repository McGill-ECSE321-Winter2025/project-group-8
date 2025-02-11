package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
}
