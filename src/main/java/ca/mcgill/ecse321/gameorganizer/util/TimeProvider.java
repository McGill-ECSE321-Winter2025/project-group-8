package ca.mcgill.ecse321.gameorganizer.util;

import ca.mcgill.ecse321.gameorganizer.config.TimeManipulationFilter;
import org.springframework.stereotype.Component;

import java.time.*;
import java.util.Date;

/**
 * Provides time information that respects time manipulation settings.
 * Services should use this instead of directly using System.currentTimeMillis(),
 * new Date(), Instant.now(), LocalDateTime.now(), etc. to ensure time manipulation works.
 */
@Component
public class TimeProvider {

    /**
     * Get the current timestamp as a java.util.Date instance
     * @return Current Date, possibly manipulated for testing
     */
    public Date getCurrentDate() {
        return Date.from(getCurrentInstant());
    }
    
    /**
     * Get the current timestamp as an Instant
     * @return Current Instant, possibly manipulated for testing
     */
    public Instant getCurrentInstant() {
        return TimeManipulationFilter.now();
    }
    
    /**
     * Get the current timestamp as a LocalDateTime
     * @return Current LocalDateTime, possibly manipulated for testing
     */
    public LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.ofInstant(getCurrentInstant(), ZoneId.systemDefault());
    }
    
    /**
     * Get the current timestamp as a LocalDate
     * @return Current LocalDate, possibly manipulated for testing
     */
    public LocalDate getCurrentLocalDate() {
        return getCurrentLocalDateTime().toLocalDate();
    }
    
    /**
     * Get the current timestamp as a LocalTime
     * @return Current LocalTime, possibly manipulated for testing
     */
    public LocalTime getCurrentLocalTime() {
        return getCurrentLocalDateTime().toLocalTime();
    }
    
    /**
     * Get the current timestamp as milliseconds since epoch
     * @return Current time in milliseconds, possibly manipulated for testing
     */
    public long getCurrentTimeMillis() {
        return getCurrentInstant().toEpochMilli();
    }
    
    /**
     * Get the current clock instance
     * @return Current Clock, possibly manipulated for testing
     */
    public Clock getClock() {
        return TimeManipulationFilter.getCurrentClock();
    }
    
    /**
     * Get the current ZonedDateTime in the system default timezone
     * @return Current ZonedDateTime, possibly manipulated for testing
     */
    public ZonedDateTime getCurrentZonedDateTime() {
        return ZonedDateTime.now(getClock());
    }
    
    /**
     * Get the current ZonedDateTime in the specified timezone
     * @param zoneId Timezone to use
     * @return Current ZonedDateTime in the specified timezone, possibly manipulated for testing
     */
    public ZonedDateTime getCurrentZonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(getClock().withZone(zoneId));
    }
} 