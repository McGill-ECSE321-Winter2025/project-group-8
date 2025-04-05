package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.beans.factory.annotation.Autowired; // Import
import org.springframework.boot.test.context.TestConfiguration; // Import
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.web.client.RestTemplate; // Import RestTemplate
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;

@Configuration
@TestConfiguration
public class TestConfig implements WebMvcConfigurer {
    public static String currentTestToken = null; // Static field to hold token

    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private EventRepository eventRepository;
    

    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public RestTemplateCustomizer restTemplateCustomizer() {
        return (RestTemplate restTemplate) -> { // Customize the RestTemplate
            restTemplate.getInterceptors().clear(); // Clear existing interceptors
            restTemplate.getInterceptors().add((ClientHttpRequestInterceptor) (request, body, execution) -> {
                // Add more detailed logging for debugging
                System.out.println("\n==== HTTP Request Details ====");
                System.out.println("URL: " + request.getURI());
                System.out.println("Method: " + request.getMethod());
                
                // Add the Authorization header to all non-auth requests when token is available
                if (currentTestToken != null && !request.getURI().getPath().contains("/auth/")) {
                    request.getHeaders().setBearerAuth(currentTestToken);
                    // Enhanced debugging
                    System.out.println("Authentication: ADDED Bearer token");
                    System.out.println("Token prefix: " + currentTestToken.substring(0, Math.min(20, currentTestToken.length())) + "...");
                    System.out.println("Authorization header: " + request.getHeaders().getFirst("Authorization"));
                } else {
                    System.out.println("Authentication: NONE" + 
                                      (currentTestToken == null ? " (token is null)" : 
                                       " (auth endpoint, no token needed)"));
                }
                
                // Always ensure Content-Type is set for requests with a body
                if (body != null && body.length > 0 && request.getHeaders().getContentType() == null) {
                    request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    System.out.println("Content-Type: Set to APPLICATION_JSON");
                } else if (request.getHeaders().getContentType() != null) {
                    System.out.println("Content-Type: " + request.getHeaders().getContentType());
                } else {
                    System.out.println("Content-Type: NONE (no body or already set)");
                }
                
                System.out.println("All Headers: " + request.getHeaders());
                System.out.println("==== End Request Details ====\n");
                
                return execution.execute(request, body);
            });
        };
    }

}