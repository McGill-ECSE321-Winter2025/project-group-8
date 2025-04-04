package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import org.springframework.context.annotation.Configuration;
// Environment import is no longer needed
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;

import ca.mcgill.ecse321.gameorganizer.security.JwtAuthenticationFilter;

@Profile("!test") // Do not load this config when 'test' profile is active

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Constructor updated to remove Environment
    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Profile check removed - Production security rules always applied here.
        // Test security is handled by TestSecurityConfig.

        // Apply security constraints (copied directly from original 'else' block)
        http.authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access for auth endpoints and account creation (POST)
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/account").permitAll()
                // Require authentication for account updates (PUT)
                .requestMatchers(HttpMethod.PUT, "/api/v1/account").authenticated()
                // Note: Other /api/v1/account/** endpoints (GET, DELETE) will fall under anyRequest().authenticated() below
                // Borrow Requests:
                // - Allow any authenticated user (USER or GAME_OWNER) to create (POST)
                .requestMatchers(HttpMethod.POST, "/api/v1/borrowrequests").hasRole("USER")
                // - Allow only GAME_OWNER to update (PUT) or delete (DELETE) specific requests
                .requestMatchers(HttpMethod.PUT, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                // - Allow any authenticated user to GET requests (adjust if needed)
                .requestMatchers(HttpMethod.GET, "/api/v1/borrowrequests/**").hasRole("USER")
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            // Handle authentication exceptions by returning 401 Unauthorized
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // Disable CSRF for REST APIs (or configure accordingly)
            .csrf(csrf -> csrf.disable())
            // Enable CORS
            .cors(cors -> cors.configure(http)) // Assuming this custom configurer is intended
            // Use stateless session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Explicitly set the SecurityContextRepository
            .securityContext(context -> context
                .securityContextRepository(new RequestAttributeSecurityContextRepository())
            )
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        // The 'else {' and closing '}' are removed

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService, @Lazy PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder); // passwordEncoder is lazily resolved
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
