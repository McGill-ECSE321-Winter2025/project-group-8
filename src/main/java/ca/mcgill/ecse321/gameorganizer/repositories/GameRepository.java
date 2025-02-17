package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.Game;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Integer> {
    Game findGameById(int id);

    List<Game> findByName(String name);


    List<Game> findByNameContaining(String namePart);
    List<Game> findByMinPlayersLessThanEqual(int players);
    List<Game> findByMaxPlayersGreaterThanEqual(int players);
    List<Game> findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(int minPlayers, int maxPlayers);
    List<Game> findByDateAddedBefore(Date date);
    List<Game> findByDateAddedAfter(Date date);
    List<Game> findByDateAddedBetween(Date startDate, Date endDate);
    List<Game> findByOwner(GameOwner owner);
    List<Game> findByOwnerAndNameContaining(GameOwner owner, String namePart);
}
