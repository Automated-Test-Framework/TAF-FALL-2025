// SQLConfig.java
package ca.etsmtl.taf.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "ca.etsmtl.taf.repository"   // <-- adapte !
)
public class SQLConfig { }
