package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ExportService {

    private final APIClient client;

    public ExportService(TestRailConfig testRailConfig) {
        this.client = testRailConfig.createClient();
    }

    //Temporaire pour debugger
    public JSONObject getTestCase(int caseId) throws IOException, APIException {
        return (JSONObject) client.sendGet("get_case/" + caseId);
    }

}
