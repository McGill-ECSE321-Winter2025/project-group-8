package ca.mcgill.ecse321.gameorganizer.middleware;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// TODO: Implement auth logic here to restrict application access to users only
// TODO: Implement auth logic to ensure events can only be updated/deleted by organizers only
// TODO: Implement auth logic to ensure borrow requests can be managed only by GameOwner

// Consult tutorial section on Middleware and Spring docs
// https://github.com/McGill-ECSE321-Winter2025/running-example


/**
 * Interceptor to handle user authentication and authorization.
 * Ensures that only authenticated users can access certain endpoints,
 * and that only users with specific roles can perform certain actions.
 * 
 * @author Shayan
 */
@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    private final AccountRepository accountRepository;
    private final UserContext userContext;

    /**
     * Constructs a UserAuthInterceptor with the specified account repository and user context.
     * 
     * @param accountRepository the account repository
     * @param userContext the user context
     */
    @Autowired
    public UserAuthInterceptor(AccountRepository accountRepository, UserContext userContext) {
        this.accountRepository = accountRepository;
        this.userContext = userContext;
    }

    /**
     * Pre-handle method to check user authentication and authorization.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler
     * @return true if the request should proceed, false otherwise
     * @throws UnauthedException if the user is not authenticated or authorized
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws UnauthedException {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireUser requireUser = handlerMethod.getMethodAnnotation(RequireUser.class);

        if (requireUser == null) {
            requireUser = handlerMethod.getBeanType().getAnnotation(RequireUser.class);
        }

        if (requireUser != null) {
            String userIdHeader = request.getHeader("User-Id");

            if (userIdHeader == null) {
                throw new UnauthedException("No User-Id header provided");
            }

            try {
                Integer userId = Integer.parseInt(userIdHeader);
                Account user = accountRepository.findById(userId).orElseThrow(() -> new UnauthedException("User not found"));
                userContext.setCurrentUser(user);

                // Check user role and permissions
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith("/events") && !user.getRole().equals("Organizer")) {
                    throw new UnauthedException("Access denied: Only organizers can manage events");
                } else if (requestURI.startsWith("/borrowRequests") && !user.getRole().equals("GameOwner")) {
                    throw new UnauthedException("Access denied: Only GameOwners can manage borrow requests");
                }
            } catch (IllegalArgumentException e) {
                throw new UnauthedException("Invalid User-Id format");
            }
        }

        return true;
    }

    /**
     * After-completion method to clear the user context.
     * 
     * @param request the HTTP request
     * @param response the HTTP response
     * @param handler the handler
     * @param ex the exception, if any
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        userContext.clear();
    }
}