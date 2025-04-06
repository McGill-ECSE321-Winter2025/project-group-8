package ca.mcgill.ecse321.gameorganizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
public class GameorganizerApplication {


	static {
		// Set strategy to allow context propagation to child threads
		// This might help if async operations or thread switches are causing context loss
		SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	}

	public static void main(String[] args) {
		// Configure and load .env file
		Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
		
		// Set environment variables from .env file as system properties
		dotenv.entries().forEach(entry -> {
			System.setProperty(entry.getKey(), entry.getValue());
		});
		
		// Log successful loading of JWT_SECRET (without revealing the secret)
		if (System.getProperty("JWT_SECRET") != null) {
			System.out.println("JWT_SECRET loaded successfully from .env file");
		}
		
		SpringApplication.run(GameorganizerApplication.class, args);
		System.out.println("My First Java Program.");
	}




}
