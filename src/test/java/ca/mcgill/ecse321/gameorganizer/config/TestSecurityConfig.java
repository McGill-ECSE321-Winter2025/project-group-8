package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test") // Only active when the 'test' profile is active
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable security constraints entirely for tests
        http
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Allow all requests without authentication
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for tests

        return http.build();
    }
}