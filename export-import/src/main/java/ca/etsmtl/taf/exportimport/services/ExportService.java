package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExportService {

    private final APIClient client;

    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);

    public ExportService(TestRailConfig testRailConfig) {
        this.client = testRailConfig.createClient();
    }

    //Temporaire pour debugger
    public JSONObject getTestCase(int caseId) throws IOException, APIException {
        return (JSONObject) client.sendGet("get_case/" + caseId);
    }

    public String exportTo(String type, Map<String, List<String>> ids) throws Exception {

        int nbProjects = ids.get("project").size();
        int nbSuitests = ids.get("suite").size();
        int nbCases = ids.get("case").size();
        int nbRuns = ids.get("run").size();

        switch (type) {
            case "testrail":
                logger.info("TestRail");
                break;

            default:
                String message = String.format("Unsupported type: %s", type);
                logger.warn(message);
                throw new Exception(message);
        }

        return String.format("Successfully exported %d projects, %d suites, %d cases and %d runs", nbProjects, nbSuitests, nbCases, nbRuns);
    }
}
