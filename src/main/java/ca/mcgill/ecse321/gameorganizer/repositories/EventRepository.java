package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findEventById(int id);
}
