package ca.etsmtl.taf.exportimport.config;

import com.gurock.testrail.APIClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class TestRailConfig {

    @Value("${testrail.url}")
    private String url;

    @Value("${testrail.user}")
    private String user;

    @Value("${testrail.apikey}")
    private String apiKey;

    @Bean
    public APIClient createClient() {
        APIClient client = new APIClient(url);
        client.setUser(user);
        client.setPassword(apiKey);
        return client;
    }
}
