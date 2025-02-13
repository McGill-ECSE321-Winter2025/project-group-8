package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Optional<Game> findGameById(int id);
}
