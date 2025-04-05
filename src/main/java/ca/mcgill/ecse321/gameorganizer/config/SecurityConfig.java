package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;

import ca.mcgill.ecse321.gameorganizer.security.JwtAuthenticationFilter; // Added import

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    static {
        // Use InheritableThreadLocal to ensure SecurityContext propagation across threads
        System.setProperty(SecurityContextHolder.SYSTEM_PROPERTY, SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Chain for public endpoints (authentication, registration)
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Profile check removed - Production security rules always applied here.
        // Test security is handled by TestSecurityConfig.

        // Apply security constraints (copied directly from original 'else' block)
        http.authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access for auth endpoints and account creation (POST)
                .requestMatchers("/auth/**").permitAll() // Changed path to /auth/**
                .requestMatchers(HttpMethod.POST, "/account").permitAll()
                // Require authentication for account updates (PUT)
                .requestMatchers(HttpMethod.PUT, "/account").authenticated()
                // Note: Other /api/v1/account/** endpoints (GET, DELETE) will fall under anyRequest().authenticated() below
                // Borrow Requests:
                // - Allow any authenticated user (USER or GAME_OWNER) to create (POST)
                .requestMatchers(HttpMethod.POST, "/borrowrequests").hasRole("USER")
                // - Allow only GAME_OWNER to update (PUT) or delete (DELETE) specific requests
                .requestMatchers(HttpMethod.PUT, "/borrowrequests/**").hasRole("GAME_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/borrowrequests/**").hasRole("GAME_OWNER")
                // - Allow any authenticated user to GET requests (adjust if needed)
                .requestMatchers(HttpMethod.GET, "/borrowrequests/**").hasRole("USER")
                // Require authentication for all other endpoints
                .anyRequest().authenticated()
            )
            .authorizeHttpRequests(authz -> authz
                .anyRequest().permitAll() // Permit all requests matching this chain
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .cors(Customizer.withDefaults()) // Enable CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)); // Stateless

        return http.build();
    }

    // Chain for protected API endpoints
    @Bean
    @Order(2)
    public SecurityFilterChain protectedApiFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/v1/**") // Apply this chain to all other /api/v1 paths
            .authorizeHttpRequests(authz -> authz
                // Specific rules for borrow requests (example)
                .requestMatchers(HttpMethod.POST, "/api/v1/borrowrequests").hasRole("USER")
                .requestMatchers(HttpMethod.PUT, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/api/v1/borrowrequests/**").hasRole("GAME_OWNER")
                .requestMatchers(HttpMethod.GET, "/api/v1/borrowrequests/**").hasRole("USER")
                // Default rule: require authentication for any other matched request
                .anyRequest().authenticated()
            )
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) // Return 401 on auth failure
            )
            .csrf(csrf -> csrf.disable()) // Disable CSRF
            .cors(Customizer.withDefaults()) // Enable CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless
            // Remove explicit SecurityContextRepository - rely on default behavior with SecurityContextHolder
            // .securityContext(context -> context
            //     .securityContextRepository(new RequestAttributeSecurityContextRepository())
            // )
            // Add JWT filter for this chain
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    // --- Authentication Provider Beans (remain the same) ---

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
