package ca.mcgill.ecse321.gameorganizer.repositories;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Account entities.
 * Provides CRUD operations and custom queries for user accounts.
 * 
 * @author @dyune
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    /**
     * Finds an account by its email address.
     *
     * @param email The email address to search for
     * @return Optional containing the account if found, empty otherwise
     */
    Optional<Account> findByEmail(String email);

}
