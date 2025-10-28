package ca.etsmtl.taf.exportimport.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROJECTS_CACHE = "projects";
    public static final String TEST_SUITES_CACHE = "testSuites";
    public static final String TEST_RUNS_CACHE = "testRuns";
    public static final String TEST_RESULTS_CACHE = "testResults";
    public static final String TEST_CASES_CACHE = "testCases";

    // Swap CacheManager implementation to use another caching strategy
    // For now, we use Caffeine for in-memory caching
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                PROJECTS_CACHE,
                TEST_SUITES_CACHE,
                TEST_RUNS_CACHE,
                TEST_RESULTS_CACHE,
                TEST_CASES_CACHE
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5_000)
                .expireAfterAccess(Duration.ofMinutes(15)));
        return cacheManager;
    }
}
