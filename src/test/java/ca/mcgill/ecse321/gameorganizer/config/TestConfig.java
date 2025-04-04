package ca.mcgill.ecse321.gameorganizer.config;

import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired; // Import
import org.springframework.boot.test.context.TestConfiguration; // Import
import org.springframework.boot.test.web.client.TestRestTemplate; // Import
import org.springframework.boot.web.client.RestTemplateBuilder; // Import
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory; // Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;

@Configuration
@TestConfiguration
public class TestConfig implements WebMvcConfigurer {
    
    // Define default test user credentials (matches EventIntegrationTests setup)
    private static final String TEST_USER_EMAIL = "host@example.com";
    private static final String TEST_USER_PASSWORD = "password123";

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private EventRepository eventRepository;
    

    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configure TestRestTemplate to manage cookies using Apache HttpClient
    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        // Using default cookie store management provided by HttpClient
        
        // Build the request factory
        HttpComponentsClientHttpRequestFactory requestFactory = 
            new HttpComponentsClientHttpRequestFactory(httpClientBuilder.build());

        // Configure RestTemplateBuilder to use the custom request factory
        RestTemplateBuilder configuredBuilder = builder.requestFactory(() -> requestFactory);
        
        // Create TestRestTemplate with the configured builder
        // Use rootUri to ensure relative paths work correctly
        return new TestRestTemplate(configuredBuilder.rootUri("http://localhost")); 
    }


    // Define a TestRestTemplate bean configured with basic auth
    // This will be the bean injected into tests
    @Bean
    public TestRestTemplate testRestTemplate(RestTemplateBuilder builder) {
        // Configure the builder used by TestRestTemplate
        RestTemplateBuilder configuredBuilder = builder
            .basicAuthentication(TEST_USER_EMAIL, TEST_USER_PASSWORD);
        // Use the configured builder to create the TestRestTemplate
        return new TestRestTemplate(configuredBuilder);
    }
}
