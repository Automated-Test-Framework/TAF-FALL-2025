// MongoConfig.java
package ca.etsmtl.taf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories(
        basePackages = "ca.etsmtl.taf.repository.mongo" // <-- adapte si tu utilises des MongoRepository
)
public class MongoConfig { }
