package ca.mcgill.ecse321.gameorganizer.middleware;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import ca.mcgill.ecse321.gameorganizer.exceptions.UnauthedException;
import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.models.Event;
import ca.mcgill.ecse321.gameorganizer.models.GameOwner;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor to handle user authentication and authorization.
 * Ensures that only authenticated users can access certain endpoints,
 * and that only users with specific roles can perform certain actions.
 *
 * @author Shayan
 */
// @Component // Removed annotation
public class UserAuthInterceptor implements HandlerInterceptor {

    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final UserContext userContext;

    // Flag to bypass authentication in test mode
    private boolean testMode = false;

    /**
     * Constructs a UserAuthInterceptor with the specified account repository, event repository, and user context.
     *
     * @param accountRepository the account repository
     * @param eventRepository the event repository
     * @param userContext the user context
     */
    @Autowired
    public UserAuthInterceptor(AccountRepository accountRepository, EventRepository eventRepository, UserContext userContext) {
        this.accountRepository = accountRepository;
        this.eventRepository = eventRepository;
        this.userContext = userContext;
    }

    /**
     * Sets whether the interceptor is in test mode.
     * In test mode, authentication checks are bypassed.
     *
     * @param testMode true if in test mode, false otherwise
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
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
        // Add logging as suggested
        System.out.println("==== UserAuthInterceptor ====");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Method: " + request.getMethod());
        System.out.println("User-Id header: " + request.getHeader("User-Id"));
        System.out.println("Cookie header: " + request.getHeader("Cookie"));
        System.out.println("testMode: " + testMode);

        // // Add testMode check at the beginning
        // if (testMode) {
        //     System.out.println("Test mode enabled, bypassing authentication");
        //     return true; // Skip all authentication in test mode
        // }

        // Original logic starts here
        System.out.println("Intercepting request: " + request.getRequestURI()); // Keep original logging too? Or remove? Let's keep for now.
        String userIdHeader = request.getHeader("User-Id");
        System.out.println("User-Id header: " + userIdHeader); // Keep original logging too?

        // Removed the specific check for /api/v1/borrowrequests - This will be handled by SecurityConfig

        if (request.getRequestURI().startsWith("/api/v1/account") && request.getMethod().equals("DELETE")) {
            // Allow authenticated users to delete their own account
            if (userIdHeader == null) {
                throw new UnauthedException("No User-Id header provided");
            }

            Integer userId = Integer.parseInt(userIdHeader);
            Account user = accountRepository.findById(userId).orElseThrow(() -> new UnauthedException("User not found"));

            // Ensure the user is deleting their own account
            String email = request.getRequestURI().split("/")[4]; // Extract email from URI
            if (!user.getEmail().equals(email)) {
                throw new UnauthedException("Access denied: Users can only delete their own account");
            }
        }

        if (!(handler instanceof HandlerMethod)) {
            return true; // Allow non-handler requests (e.g., static resources)
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequireUser requireUser = handlerMethod.getMethodAnnotation(RequireUser.class);

        if (requireUser == null) {
            requireUser = handlerMethod.getBeanType().getAnnotation(RequireUser.class);
        }

        if (requireUser != null) {
<<<<<<< HEAD
            // The user should already be set in the UserContext by the JwtAuthenticationFilter
            Account user = userContext.getCurrentUser();

            if (user == null) {
                throw new UnauthedException("User not authenticated");
            }

            // Check user role and permissions
            String requestURI = request.getRequestURI();
            if (requestURI.startsWith("/events")) {
                String eventIdParam = request.getParameter("eventId");
                if (eventIdParam != null) {
                    try {
                        UUID eventId = UUID.fromString(eventIdParam); // Convert to UUID
                        Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new UnauthedException("Event not found"));
                        if (!event.getHost().equals(user)) {
                            throw new UnauthedException("Access denied: Only event hosts can manage events");
=======
            if (userIdHeader == null) {
                throw new UnauthedException("No User-Id header provided");
            }

            try {
                Integer userId = Integer.parseInt(userIdHeader);
                Account user = accountRepository.findById(userId).orElseThrow(() -> new UnauthedException("User not found"));
                userContext.setCurrentUser(user);

                // Check user role and permissions
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith("/api/v1/events")) {
                    // Ensure only organizers can update/delete events
                    if (request.getMethod().equals("PUT") || request.getMethod().equals("DELETE")) {
                        String eventIdParam = request.getParameter("eventId");
                        if (eventIdParam != null) {
                            UUID eventId = UUID.fromString(eventIdParam);
                            Event event = eventRepository.findById(eventId)
                                    .orElseThrow(() -> new UnauthedException("Event not found"));
                            if (!event.getHost().equals(user)) {
                                throw new UnauthedException("Access denied: Only event organizers can manage events");
                            }
>>>>>>> 4c31ae232e79f47cf53101a71bb334ebc9d1792d
                        }
                    } catch (IllegalArgumentException e) {
                        throw new UnauthedException("Invalid event ID format");
                    }
<<<<<<< HEAD
=======
                } else if (requestURI.startsWith("/api/v1/borrowrequests")) {
                    // Ensure only GameOwners can manage borrow requests
                    if (!(user instanceof GameOwner)) {
                        throw new UnauthedException("Access denied: Only GameOwners can manage borrow requests");
                    }
>>>>>>> 4c31ae232e79f47cf53101a71bb334ebc9d1792d
                }
            } else if (requestURI.startsWith("/borrowRequests") && !(user instanceof GameOwner)) {
                throw new UnauthedException("Access denied: Only GameOwners can manage borrow requests");
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
