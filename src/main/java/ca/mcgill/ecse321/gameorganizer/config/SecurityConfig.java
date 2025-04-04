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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher; // Added import

import ca.mcgill.ecse321.gameorganizer.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    // Chain for public endpoints (authentication, registration)
    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher( // Apply this chain ONLY to the combined public paths
                 new OrRequestMatcher(
                    new AntPathRequestMatcher("/api/v1/auth/**"),
                    new AntPathRequestMatcher("/api/v1/account", HttpMethod.POST.toString())
                 )
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
            .securityContext(context -> context
                .securityContextRepository(new RequestAttributeSecurityContextRepository()) // Use stateless context repo
            )
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
