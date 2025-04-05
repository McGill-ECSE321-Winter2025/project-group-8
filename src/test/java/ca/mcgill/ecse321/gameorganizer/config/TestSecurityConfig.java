package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import ca.mcgill.ecse321.gameorganizer.security.JwtAuthenticationFilter;

@Configuration
@Profile("test")
@EnableMethodSecurity
public class TestSecurityConfig {

    static {
        // Use InheritableThreadLocal to ensure SecurityContext propagation across threads
        System.setProperty(SecurityContextHolder.SYSTEM_PROPERTY, SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    private static final Logger log = LoggerFactory.getLogger(TestSecurityConfig.class);
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public TestSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Configure security similar to main config but more permissive for tests
        log.info("Configuring test security filter chain");
        
        http.authorizeHttpRequests(authz -> authz
                // Allow unauthenticated access for auth endpoints and account creation (POST)
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/account").permitAll()
                // For testing, require authentication for protected endpoints
                .requestMatchers(HttpMethod.POST, "/events/**").authenticated()
                .requestMatchers(HttpMethod.PUT, "/events/**").authenticated()
                .requestMatchers(HttpMethod.DELETE, "/events/**").authenticated()
                // For testing, ensure we enforce role-based security on these endpoints
                .requestMatchers(HttpMethod.PUT, "/borrowrequests/**").hasRole("GAME_OWNER")
                .requestMatchers(HttpMethod.DELETE, "/borrowrequests/**").hasRole("GAME_OWNER")
                // But be more permissive on other endpoints
                .anyRequest().permitAll()
            )
            // Handle authentication exceptions by returning 401 Unauthorized
            .exceptionHandling(handling -> handling
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            // Disable CSRF for REST APIs tests
            .csrf(csrf -> csrf.disable())
            // Enable CORS
            .cors(cors -> cors.configure(http))
            // Use stateless session management
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Explicitly set the SecurityContextRepository
            .securityContext(context -> context
                .securityContextRepository(new RequestAttributeSecurityContextRepository())
            )
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}