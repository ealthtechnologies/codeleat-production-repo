package com.ealth.codeleat;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeleatApplication {
	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure()
				.directory(".") // ensures project root
				.ignoreIfMalformed()
				.ignoreIfMissing()
				.load(); // loads .env automatically
		System.setProperty("DB_URL", dotenv.get("DB_URL"));
		System.setProperty("DB_USERNAME", dotenv.get("DB_USERNAME"));
		System.setProperty("DB_PASSWORD", dotenv.get("DB_PASSWORD"));
		System.setProperty("DB_DRIVER", dotenv.get("DB_DRIVER"));
		System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
		System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
		System.setProperty("JWT_EXPIRATION", dotenv.get("JWT_EXPIRATION"));
		SpringApplication.run(CodeleatApplication.class, args);
	}

}
