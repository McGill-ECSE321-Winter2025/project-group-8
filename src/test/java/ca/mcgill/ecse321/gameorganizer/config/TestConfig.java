package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.mcgill.ecse321.gameorganizer.middleware.UserAuthInterceptor;
import ca.mcgill.ecse321.gameorganizer.middleware.UserContext;
import ca.mcgill.ecse321.gameorganizer.repositories.AccountRepository;
import ca.mcgill.ecse321.gameorganizer.repositories.EventRepository;

@Configuration
@TestConfiguration
public class TestConfig implements WebMvcConfigurer {
    
    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Bean
    public UserContext userContext() {
        return new UserContext();
    }

    @Bean
    public UserAuthInterceptor userAuthInterceptor() {
        UserAuthInterceptor interceptor = new UserAuthInterceptor(accountRepository, eventRepository, userContext());
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor())
                .addPathPatterns("/**");
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
