package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // Import HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // Import
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Import
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    
    private final Environment environment;
    
    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
                // // Allow unauthenticated access to authentication and account creation endpoints
                // .requestMatchers("/api/v1/auth/**", "/api/v1/account/**", "/api/v1/account").permitAll()
                // // Borrow Requests:
                // // - Allow any authenticated user (USER or GAME_OWNER) to create (POST)
                // .requestMatchers(HttpMethod.POST, "/api/v1/borrowrequests").hasRole("USER") 
                // // - Allow only GAME_OWNER to update (PUT) or delete (DELETE) specific requests
                // .requestMatchers(HttpMethod.PUT, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                // .requestMatchers(HttpMethod.DELETE, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                // // - Allow any authenticated user to GET requests (adjust if needed)
                // .requestMatchers(HttpMethod.GET, "/api/v1/borrowrequests/**").hasRole("USER") 
                // // Require authentication for all other endpoints (that haven't been matched yet)
                // .anyRequest().authenticated()
                .anyRequest().permitAll() // For testing purposes, allow all requests
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity in REST APIs

        return http.build();
    }
    
    // Expose AuthenticationManager as a Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
