package ca.mcgill.ecse321.gameorganizer.middleware;

import ca.mcgill.ecse321.gameorganizer.models.Account;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserContext userContext;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
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
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No User-Id header provided");
                return false;
            }

            try {
                Integer userId = Integer.parseInt(userIdHeader);
                Account user = accountRepository.findById(userId).orElseThrow(() -> new Exception("User not found"));
                userContext.setCurrentUser(user);

                // Check user role and permissions
                String requestURI = request.getRequestURI();
                if (requestURI.startsWith("/events") && !user.getRole().equals("Organizer")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only organizers can manage events");
                    return false;
                } else if (requestURI.startsWith("/borrowRequests") && !user.getRole().equals("GameOwner")) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: Only GameOwners can manage borrow requests");
                    return false;
                }
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid User-Id format");
                return false;
            }
        }

        return true;
    }
}