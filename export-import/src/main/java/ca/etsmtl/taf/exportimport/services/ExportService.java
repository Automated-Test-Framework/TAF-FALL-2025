package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.repositories.ProjectRepository;
import ca.etsmtl.taf.exportimport.utils.exporters.Exporter;
import ca.etsmtl.taf.exportimport.utils.exporters.TestRailExporter;
import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExportService {

    private final ProjectRepository projectRepository;
    private final Map<String, Exporter> exporters;
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);


    @Autowired
    public ExportService(ProjectRepository projectRepository, Map<String, Exporter> exporters) {
        this.projectRepository = projectRepository;
        this.exporters = exporters;
    }

    //Temporaire pour debugger
    public JSONObject getTestCase(int caseId) throws IOException, APIException {
        Exporter testrailExporter = exporters.get("testrail");

        return ((TestRailExporter) testrailExporter) .getTestCase(caseId);
    }

    public String exportTo(String type, Map<String, List<String>> ids) throws Exception {
        logger.info(String.valueOf(ids.get("project")));
        logger.info(ids.get("project").getFirst());
        logger.info(String.valueOf(projectRepository.findById(ids.get("project").getFirst())));

        Exporter exporter = exporters.get(type);
        if (exporter == null) {
            String message = String.format("Unsupported type: %s", type);
            logger.warn(message);
            throw new Exception(message);
        }

        exporter.exportTo(ids);

        return getExportConfirmationMessage(ids);
    }

    private static String getExportConfirmationMessage(Map<String, List<String>> ids) {
        int nbProjects = ids.getOrDefault("project", List.of()).size();
        int nbSuites = ids.getOrDefault("suite", List.of()).size();
        int nbCases = ids.getOrDefault("case", List.of()).size();
        int nbRuns = ids.getOrDefault("run", List.of()).size();

        StringBuilder messageBuilder = new StringBuilder("Successfully exported");
        if (nbProjects > 0) messageBuilder.append(" ").append(nbProjects).append(" projects");
        if (nbSuites > 0) messageBuilder.append(nbProjects > 0 ? "," : "").append(" ").append(nbSuites).append(" suites");
        if (nbCases > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0) ? "," : "").append(" ").append(nbCases).append(" cases");
        if (nbRuns > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0 || nbCases > 0) ? "," : "").append(" ").append(nbRuns).append(" runs");

        return messageBuilder.toString();
    }
}
