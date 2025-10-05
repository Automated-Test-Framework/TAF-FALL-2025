package ca.etsmtl.taf.exportimport;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ExportImportApplication {

    public static void main(String[] args) {
        APIClient client = TestRailConfig.createClient();

        int caseId = 1;

        try {
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
