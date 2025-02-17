package ca.mcgill.ecse321.gameorganizer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.mcgill.ecse321.gameorganizer.models.Registration;

/**
 * Repository interface for managing Registration entities.
 * Provides CRUD operations and custom queries for event registrations.
 * 
 * @author @Shine111111
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Integer> {
    /**
     * Finds a registration by its unique identifier.
     *
     * @param id the ID of the registration to find
     * @return Optional containing the registration if found, empty otherwise
     */
    Optional<Registration> findRegistrationById(int id);
}
