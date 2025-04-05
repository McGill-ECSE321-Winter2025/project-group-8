package ca.mcgill.ecse321.gameorganizer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for setting up test JWT environment.
 * This is designed to be included in test classes with 
 * @ContextConfiguration(initializers = TestJwtConfig.Initializer.class)
 */
public class TestJwtConfig {

    /**
     * Application context initializer that sets the JWT_SECRET environment variable.
     */
    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            ConfigurableEnvironment environment = applicationContext.getEnvironment();
            
            // Create a longer secret that meets the 512-bit (64-byte) requirement for HS512
            String testJwtSecret = "tG8qcqi6M2XZ1s73QTdIHHGhBEzZARBOlDvcxkp4iAoCPU5f8OeYXFmNOkjr9XgJvTwfF2aH9yPoiWnBuYTa0qZxLcMk7DlN";
            
            Map<String, Object> props = new HashMap<>();
            props.put("JWT_SECRET", testJwtSecret);
            
            MapPropertySource testProperties = new MapPropertySource("testJwtProperties", props);
            environment.getPropertySources().addFirst(testProperties);
            
            // Also set as a system property for methods that check there
            System.setProperty("JWT_SECRET", testJwtSecret);
            
            System.out.println("JWT_SECRET environment variable set for testing");
        }
    }
} 