package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.core.Ordered;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/**
 * This filter intercepts requests with time manipulation headers sent from the frontend
 * testing utility, allowing testers to modify server time perception for testing.
 * 
 * The filter looks for:
 * - X-Test-Time-Offset: Time offset in milliseconds
 * - X-Test-Current-Time: ISO-8601 string of the simulated current time
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TimeManipulationFilter extends OncePerRequestFilter {

    // Store the default system clock
    private static final Clock DEFAULT_SYSTEM_CLOCK = Clock.systemDefaultZone();
    
    // Thread-local storage for manipulated clock
    private static final ThreadLocal<Clock> THREAD_LOCAL_CLOCK = new ThreadLocal<>();
    
    // Thread-local flag to indicate if time manipulation is active
    private static final ThreadLocal<Boolean> TIME_MANIPULATION_ACTIVE = new ThreadLocal<>();
    
    // Thread-local storage for time offset (useful for calculations)
    private static final ThreadLocal<Long> TIME_OFFSET_MS = new ThreadLocal<>();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Default to not active
            TIME_MANIPULATION_ACTIVE.set(false);
            TIME_OFFSET_MS.set(0L);
            
            // Check for our test headers
            String timeOffsetHeader = request.getHeader("X-Test-Time-Offset");
            String currentTimeHeader = request.getHeader("X-Test-Current-Time");
            
            // If we have a time offset or current time header, set up the thread-local clock
            if (timeOffsetHeader != null || currentTimeHeader != null) {
                Clock adjustedClock = null;
                long offsetMs = 0;
                
                if (currentTimeHeader != null) {
                    // Parse ISO time string directly
                    Instant fixedTime = Instant.parse(currentTimeHeader);
                    adjustedClock = Clock.fixed(fixedTime, ZoneId.systemDefault());
                    
                    // Calculate the offset for reference
                    offsetMs = fixedTime.toEpochMilli() - Instant.now().toEpochMilli();
                } 
                else if (timeOffsetHeader != null) {
                    // Apply offset from current time
                    offsetMs = Long.parseLong(timeOffsetHeader);
                    Instant adjustedTime = Instant.now().plusMillis(offsetMs);
                    adjustedClock = Clock.fixed(adjustedTime, ZoneId.systemDefault());
                }
                
                if (adjustedClock != null) {
                    // Set the thread-local clock for this request
                    THREAD_LOCAL_CLOCK.set(adjustedClock);
                    TIME_MANIPULATION_ACTIVE.set(true);
                    TIME_OFFSET_MS.set(offsetMs);
                    
                    // Add a response header to indicate time manipulation is active
                    response.setHeader("X-Test-Time-Processed", "true");
                    
                    // Log for debugging
                    logger.info("Time manipulation active: " + 
                               (timeOffsetHeader != null ? "Offset: " + timeOffsetHeader + "ms" : "") +
                               (currentTimeHeader != null ? " Time: " + currentTimeHeader : ""));
                }
            }
            
            // Continue the filter chain
            filterChain.doFilter(request, response);
        } finally {
            // Always clean up the thread-local to prevent memory leaks
            THREAD_LOCAL_CLOCK.remove();
            TIME_MANIPULATION_ACTIVE.remove();
            TIME_OFFSET_MS.remove();
        }
    }
    
    /**
     * Get the current clock, either the manipulated one for this thread or the system default.
     * This should be used by services that need to be aware of time manipulation.
     * @return The current Clock to use
     */
    public static Clock getCurrentClock() {
        Clock clock = THREAD_LOCAL_CLOCK.get();
        return clock != null ? clock : DEFAULT_SYSTEM_CLOCK;
    }
    
    /**
     * Get the current instant, either from the manipulated clock or system time.
     * This is a convenience method for getting the current time.
     * @return The current Instant to use
     */
    public static Instant now() {
        return getCurrentClock().instant();
    }
    
    /**
     * Check if time manipulation is active for the current thread.
     * This can be used by authentication services to bypass expiry checks during testing.
     * @return true if time manipulation is active
     */
    public static boolean isTimeManipulationActive() {
        Boolean active = TIME_MANIPULATION_ACTIVE.get();
        return active != null && active;
    }
    
    /**
     * Get the current time offset in milliseconds.
     * @return The current time offset, or 0 if no manipulation is active
     */
    public static long getCurrentTimeOffsetMs() {
        Long offset = TIME_OFFSET_MS.get();
        return offset != null ? offset : 0L;
    }
    
    /**
     * Utility method to adjust a timestamp by the current time offset.
     * This is useful for token validation where you want to adjust a stored timestamp
     * by the current time offset to determine if it would be valid in real time.
     * 
     * @param timestamp The timestamp to adjust
     * @return The adjusted timestamp
     */
    public static long adjustTimestampForValidation(long timestamp) {
        if (isTimeManipulationActive()) {
            return timestamp + getCurrentTimeOffsetMs();
        }
        return timestamp;
    }
} 