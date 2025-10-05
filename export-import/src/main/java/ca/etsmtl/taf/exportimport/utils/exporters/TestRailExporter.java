package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component("testrail")
public class TestRailExporter implements Exporter {

    private final APIClient client;

    public TestRailExporter(TestRailConfig testRailConfig) {
        this.client = testRailConfig.createClient();
    }

    //TODO: A supprimer, Temporaire pour débugger
    public JSONObject getTestCase(int caseId) throws APIException, IOException {
        return (JSONObject) client.sendGet("get_case/" + caseId);
    }

    @Override
    public void exportTo(Map<String, List<String>> ids) throws Exception {
        System.out.println("Do some work, being productive and all that");
    }


}
