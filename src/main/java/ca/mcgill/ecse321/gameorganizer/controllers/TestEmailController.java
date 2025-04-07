package ca.mcgill.ecse321.gameorganizer.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.mcgill.ecse321.gameorganizer.services.EmailService;
import ca.mcgill.ecse321.gameorganizer.services.GmailApiService;
import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller with test endpoints for email functionality.
 * Only available in development mode.
 */
@RestController
@RequestMapping("/dev/email")
@Profile("dev") // Only active in dev profile
public class TestEmailController {

    private static final Logger log = LoggerFactory.getLogger(TestEmailController.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private GmailApiService gmailApiService;

    /**
     * Test endpoint to send a test email.
     * 
     * @param email The recipient email
     * @return A response indicating success or failure
     */
    @PostMapping("/test")
    public ResponseEntity<String> testEmail(@RequestParam String email) {
        try {
            // Generate a fake token for testing
            String testToken = "test-token-" + System.currentTimeMillis();
            
            // Send test email
            emailService.sendPasswordResetEmail(email, testToken, "Test User");
            
            return ResponseEntity.ok("Test email sent successfully to " + email);
        } catch (MessagingException e) {
            return ResponseEntity.badRequest()
                    .body("Failed to send test email: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("An error occurred: " + e.getMessage());
        }
    }

    /**
     * Test endpoint to diagnose email configuration.
     * 
     * @return A response with the current email configuration
     */
    @PostMapping("/diagnose")
    public ResponseEntity<String> diagnoseEmailConfig() {
        StringBuilder report = new StringBuilder();
        report.append("Email Configuration Diagnostics:\n");
        
        try {
            // Add environment variables (but hide sensitive data)
            String mailPassword = System.getenv("MAIL_PASSWORD");
            report.append("MAIL_PASSWORD environment variable: ")
                  .append(mailPassword != null ? "[CONFIGURED]" : "[NOT CONFIGURED]")
                  .append("\n");
            
            // Add more diagnostic info as needed
            
            return ResponseEntity.ok(report.toString());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to generate diagnostic report: " + e.getMessage());
        }
    }

    /**
     * Test endpoint to send a test email using Gmail API.
     * 
     * @param email The recipient email
     * @return A response indicating success or failure
     */
    @PostMapping("/test-gmail-api")
    public ResponseEntity<String> testGmailApi(@RequestParam String email) {
        try {
            // Generate a fake token for testing
            String testToken = "test-token-" + System.currentTimeMillis();
            
            // Send test email using Gmail API
            gmailApiService.sendEmail(email, "BoardGameConnect - Gmail API Test", 
                "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>Gmail API Test</h1>" +
                "<p>This email was sent using the Gmail API with OAuth2 authentication.</p>" +
                "<p>Test token: " + testToken + "</p>" +
                "</div>");
            
            return ResponseEntity.ok("Test email sent successfully using Gmail API to " + email);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send email via Gmail API: " + e.getMessage());
        }
    }
} 