package ca.etsmtl.taf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Separate MongoDB auditing configuration.
 * Extracted from the main application class so that @WebMvcTest slices
 * don't try to create MongoDB beans.
 */
@Configuration
@EnableMongoAuditing
public class MongoAuditingConfig {
}
