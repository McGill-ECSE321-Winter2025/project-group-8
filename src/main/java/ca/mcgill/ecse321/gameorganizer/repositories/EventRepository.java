package ca.mcgill.ecse321.gameorganizer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.mcgill.ecse321.gameorganizer.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findEventById(int id);
}
