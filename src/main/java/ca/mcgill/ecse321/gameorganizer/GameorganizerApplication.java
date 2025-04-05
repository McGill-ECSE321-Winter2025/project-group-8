package ca.mcgill.ecse321.gameorganizer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class GameorganizerApplication {

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
