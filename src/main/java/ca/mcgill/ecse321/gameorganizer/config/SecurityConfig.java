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
        // Check if we're running in test profile
        boolean isTestProfile = false;
        for (String profile : environment.getActiveProfiles()) {
            if ("test".equals(profile)) {
                isTestProfile = true;
                break;
            }
        }
        
        if (isTestProfile) {
            // For test profile, disable security constraints
            http.authorizeHttpRequests(authz -> authz
                    .anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        } else {
            // For non-test profiles, apply security constraints
            http.authorizeHttpRequests(authz -> authz
                    // Allow unauthenticated access for account creation and auth endpoints
                    .requestMatchers("/api/v1/account", "/auth/**").permitAll()
                    .anyRequest().authenticated()
                )
                // Disable CSRF for REST APIs (or configure accordingly)
                .csrf(csrf -> csrf.disable());
        }
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
