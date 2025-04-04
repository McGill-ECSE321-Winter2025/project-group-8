package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager; // Re-add import
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Re-add import
import static org.mockito.Mockito.mock; // Re-add import
import org.springframework.http.HttpMethod; // Import HttpMethod
import org.springframework.core.annotation.Order; // Import Order

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("test") // Only active when the 'test' profile is active
@EnableMethodSecurity(prePostEnabled = true) // Enable method security for tests
// Remove @EnableAutoConfiguration exclusion
public class TestSecurityConfig {

    @Bean
    @Order(0) // Give this highest precedence to override default filter chain
            public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable security constraints entirely for tests
        http
            // Use standard authentication mechanisms which @WithMockUser hooks into
            // Rely on @PreAuthorize for authorization checks
            .authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access for auth endpoints and account creation
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/account").permitAll()
                // Require authentication for all other requests
                .anyRequest().authenticated()
            )
                                    .csrf(csrf -> csrf.disable()); // Disable CSRF for tests
                        
                                return http.build();
    }
// Re-add the mock AuthenticationManager bean definition
@Bean
public AuthenticationManager authenticationManager() {
    return mock(AuthenticationManager.class);
}
}