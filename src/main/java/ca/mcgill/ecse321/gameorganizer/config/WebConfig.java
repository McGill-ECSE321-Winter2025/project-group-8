package ca.mcgill.ecse321.gameorganizer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import ca.mcgill.ecse321.gameorganizer.middleware.UserAuthInterceptor;

@Configuration
@Profile("!test")
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private UserAuthInterceptor userAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userAuthInterceptor)
<<<<<<< HEAD
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/reset-password", "/register", "/api/v1/account/**", "/api/v1/auth/**");
=======
                .addPathPatterns("/**");
                //.excludePathPatterns("/login", "/reset-password", "/register", "/account");
>>>>>>> 4c31ae232e79f47cf53101a71bb334ebc9d1792d
    }
}
