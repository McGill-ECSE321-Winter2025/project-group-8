package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
                // Allow unauthenticated access to authentication and account creation endpoints
                .requestMatchers("/api/v1/auth/**", "/api/v1/account").permitAll()
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()); // Disable CSRF for simplicity in REST APIs

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
