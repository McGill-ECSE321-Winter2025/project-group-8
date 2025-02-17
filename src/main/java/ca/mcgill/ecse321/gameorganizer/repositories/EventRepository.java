package ca.mcgill.ecse321.gameorganizer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.sql.Date;
import java.util.List;
import java.util.Optional;

import ca.mcgill.ecse321.gameorganizer.models.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {
    Optional<Event> findEventById(int id);
    List<Event> findEventByTitle(String title);
    List<Event> findEventByTitleContaining(String title);
    List<Event> findEventByDateTime(Date dateTime);
    List<Event> findEventByLocation(String location);
    List<Event> findEventByLocationContaining(String location);
    List<Event> findEventByDescription(String description);
    List<Event> findEventByMaxParticipants(int maxParticipants);
    List<Event> findByFeaturedGameMinPlayers(int minPlayers);
    //List<Event> findByFeaturedGameMinPlayersGreaterThanEqualAndMaxParticipantsLessThanEqual(int minPlayers, int maxParticipants);
    List<Event> findEventByFeaturedGameId(int featuredGameId);
    List<Event> findEventByFeaturedGameName(String featuredGameName);
    List<Event> findEventByHostId(int hostId);
    List<Event> findEventByHostName(String hostUsername);
}
