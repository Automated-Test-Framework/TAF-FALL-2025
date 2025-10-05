package ca.etsmtl.taf.exportimport;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ExportImportApplication {

    public static void main(String[] args) throws APIException, IOException {

        String url = System.getenv("TESTRAIL_URL");
        String user = System.getenv("TESTRAIL_USER");
        String apiKey = System.getenv("TESTRAIL_APIKEY");

        APIClient client = new APIClient(url);
        client.setUser(user);
        client.setPassword(apiKey);

        int caseId = 1;

        try {
            // Perform GET request
            JSONObject caseData = (JSONObject) client.sendGet("get_case/" + caseId);
            System.out.println("Title: " + caseData.get("title"));
            System.out.println("Section ID: " + caseData.get("section_id"));
            System.out.println("Created by: " + caseData.get("created_by"));

        } catch (IOException | APIException e) {
            e.printStackTrace();
        }

        SpringApplication.run(ExportImportApplication.class, args);
    }

}
