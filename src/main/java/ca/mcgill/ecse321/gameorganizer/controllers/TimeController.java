package ca.mcgill.ecse321.gameorganizer.controllers;

import ca.mcgill.ecse321.gameorganizer.util.TimeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for time-related operations, primarily used for testing time manipulation
 */
@RestController
@RequestMapping("/api")
public class TimeController {

    @Autowired
    private TimeProvider timeProvider;

    /**
     * Get the current server time, reflecting any time manipulation settings
     * This endpoint is primarily for testing the time manipulation feature
     * @param request The HTTP request
     * @param response The HTTP response
     * @return A map containing the current server time
     */
    @GetMapping("/server-time")
    public ResponseEntity<Map<String, Object>> getServerTime(
            HttpServletRequest request, 
            HttpServletResponse response) {
        
        // Get the current time from our time provider (respects time manipulation)
        ZonedDateTime currentTime = timeProvider.getCurrentZonedDateTime();
        
        // Format the time in ISO-8601 format
        String formattedTime = currentTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        
        // Check if time manipulation is active
        String timeOffsetHeader = request.getHeader("X-Test-Time-Offset");
        String currentTimeHeader = request.getHeader("X-Test-Current-Time");
        
        // If either header is present, add a response header to confirm time manipulation
        if (timeOffsetHeader != null || currentTimeHeader != null) {
            response.setHeader("X-Test-Time-Processed", "true");
        }
        
        // Build the response
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("serverTime", formattedTime);
        responseMap.put("timeManipulationActive", (timeOffsetHeader != null || currentTimeHeader != null));
        
        // Include additional debug info if time manipulation is active
        if (timeOffsetHeader != null || currentTimeHeader != null) {
            responseMap.put("requestedOffset", timeOffsetHeader);
            responseMap.put("requestedTime", currentTimeHeader);
        }
        
        return ResponseEntity.ok(responseMap);
    }
} 