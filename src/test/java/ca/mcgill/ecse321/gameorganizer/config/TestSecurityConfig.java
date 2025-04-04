package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // Use Configuration
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// No PasswordEncoder needed here, should be provided by main config or TestConfig

@Configuration // Use @Configuration
@Profile("test") // Only active when the 'test' profile is active
@EnableMethodSecurity(prePostEnabled = false, securedEnabled = false, jsr250Enabled = false) // Keep this to disable method security if needed
public class TestSecurityConfig {

    @Bean
    @Primary // Ensure this bean overrides the main SecurityFilterChain bean during tests
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable security constraints entirely for tests by permitting all requests
        // and disabling CSRF.
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow ALL requests without any authentication/authorization
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for tests

        // Explicitly DO NOT add the JwtAuthenticationFilter here for the test profile

        return http.build();
    }

    // Do NOT define other beans like PasswordEncoder or AuthenticationManager here
    // if they are intended to be provided by the main SecurityConfig or TestConfig.
}