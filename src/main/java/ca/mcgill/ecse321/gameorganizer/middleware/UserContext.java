package ca.mcgill.ecse321.gameorganizer.middleware;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import ca.mcgill.ecse321.gameorganizer.models.Account;

/**
 * UserContext inspired from the running example
 * @author Shayan
 */

@Component
@RequestScope
public class UserContext {
    private Account currentUser;

    public Account getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Account currentUser) {
        this.currentUser = currentUser;
    }

    public void clear() {
        this.currentUser = null;
    }
}