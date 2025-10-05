package ca.etsmtl.taf.exportimport.config;

import com.gurock.testrail.APIClient;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TestRailConfig {

    public static APIClient createClient() {

        String url = System.getenv("TESTRAIL_URL");
        String user = System.getenv("TESTRAIL_USER");
        String apiKey = System.getenv("TESTRAIL_APIKEY");

        if (url == null || user == null || apiKey == null) {
            throw new IllegalStateException("Missing TestRail environment variables.");
        }

        APIClient client = new APIClient(url);
        client.setUser(user);
        client.setPassword(apiKey);
        return client;
    }
}
