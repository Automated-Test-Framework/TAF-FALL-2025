package ca.etsmtl.taf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;

@SpringBootApplication
// @EnableMongoAuditing moved to ca.etsmtl.taf.config.MongoAuditingConfig
public class TestAutomationFrameworkApplication implements CommandLineRunner {

	public void run(String... args) throws Exception {
		System.out.println("Team 2 Services is Running!");
	}

	public static void main(String[] args) {
		SpringApplication.run(TestAutomationFrameworkApplication.class, args);
	}

}
