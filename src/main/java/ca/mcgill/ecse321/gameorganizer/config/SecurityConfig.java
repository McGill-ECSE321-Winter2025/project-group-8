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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import java.util.Arrays;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import ca.mcgill.ecse321.gameorganizer.security.JwtAuthenticationFilter;

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

    // Main security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**") // Match all requests
            .authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access for auth endpoints and account creation
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/account").permitAll()
                // Require authentication for account GET requests
                .requestMatchers(HttpMethod.GET, "/account/**").authenticated()
                // API users endpoints
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()
                // Allow GET operations for browsing content
                .requestMatchers(HttpMethod.GET, "/games/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/users/*/games").permitAll()
                .requestMatchers(HttpMethod.GET, "/lending-records/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/borrowrequests/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/borrowrequests/**").authenticated()
                // Account management
                .requestMatchers(HttpMethod.PUT, "/account/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/account/**").authenticated()
                // Game operations - Assume only game owners or admins can modify
                // Note: Adjust "GAME_OWNER", "ADMIN" to actual role names used in UserDetailsServiceImpl
                .requestMatchers(HttpMethod.POST, "/games/**").hasRole("GAME_OWNER") // Changed from hasAnyRole("GAME_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/games/**").hasRole("GAME_OWNER") // Changed from hasAnyRole("GAME_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/games/**").hasRole("GAME_OWNER") // Changed from hasAnyRole("GAME_OWNER", "ADMIN")
                // Event operations - Assume only authenticated users can create/modify for now, refine if needed
                .requestMatchers(HttpMethod.POST, "/events/**").authenticated() // Or apply role checks if needed
                .requestMatchers(HttpMethod.PUT, "/events/**").authenticated()   // Or apply role checks if needed
                .requestMatchers(HttpMethod.DELETE, "/events/**").authenticated() // Or apply role checks if needed
                // Borrow Requests - Actions likely performed by authenticated users
                .requestMatchers(HttpMethod.POST, "/borrowrequests/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/borrowrequests/**").authenticated() // e.g., Accept/Reject might need owner role? Check logic.
                .requestMatchers(HttpMethod.DELETE, "/borrowrequests/**").authenticated() // Who can delete?
                .requestMatchers(HttpMethod.POST, "/api/borrowrequests/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/borrowrequests/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/borrowrequests/**").authenticated()
                // Lending Records - Actions likely performed by authenticated users, potentially game owners
                .requestMatchers(HttpMethod.POST, "/lending-records/**").authenticated() // e.g., Confirm pickup/return
                .requestMatchers(HttpMethod.PUT, "/lending-records/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/lending-records/**").hasRole("GAME_OWNER") // Changed from hasAnyRole("GAME_OWNER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/lending-records/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/lending-records/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/api/lending-records/**").hasRole("GAME_OWNER")
                // Reviews
                .requestMatchers("/reviews/**").authenticated()
                .requestMatchers("/api/reviews/**").authenticated()
                // Registrations
                .requestMatchers("/registrations/**").authenticated()
                .requestMatchers("/api/registrations/**").authenticated()
                // Default deny? Or should anyRequest() be authenticated()? Let's assume authenticated for now.
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Completely disable anonymous authentication to prevent it from overriding JWT auth
            .anonymous(anonymous -> anonymous.disable())
            
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // Removed the second filter chain bean

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Can't use * with allowCredentials=true, so specify the frontend origin
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token", "Authorization"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));
        configuration.setAllowCredentials(true); // Allow credentials
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
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
