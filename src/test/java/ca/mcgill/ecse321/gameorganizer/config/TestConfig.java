package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.client.RestTemplateBuilder; // Import RestTemplateBuilder
import org.springframework.boot.test.web.client.TestRestTemplate; // Import TestRestTemplate
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.mcgill.ecse321.gameorganizer.middleware.UserAuthInterceptor;
import ca.mcgill.ecse321.gameorganizer.middleware.UserContext;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;

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
    public UserContext userContext() {
        return new UserContext();
    }

    @Bean
    public UserAuthInterceptor userAuthInterceptor() {
        UserAuthInterceptor interceptor = new UserAuthInterceptor(accountRepository, eventRepository, userContext());
        interceptor.setTestMode(true);
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor())
                .addPathPatterns("/**");
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
