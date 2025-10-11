package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.ProjectDTO;
import ca.etsmtl.taf.exportimport.models.*;

import ca.etsmtl.taf.exportimport.services.ExportService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gurock.testrail.APIClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component("testrail")
public class TestRailExporter implements Exporter {

    private final APIClient client;

    private static final Logger logger = LoggerFactory.getLogger(TestRailExporter.class);

    @Autowired
    public TestRailExporter(TestRailConfig testRailConfig) {
        this.client = testRailConfig.createClient();
    }

    @Override
    public void exportTo(Map<EntityType, List<Entity>> entities) throws Exception {
        System.out.println("Do some work, being productive and all that");
        System.out.println(entities.keySet());
        System.out.println(entities.values());
        entities.values().forEach(entity -> {
            System.out.println(entity.toString());
            System.out.println();
        });

        // Projects
        //TODO: check si on met try catch
        JSONObject responseGET = (JSONObject) client.sendGet("get_projects");

        JSONArray projectsNode = (JSONArray) responseGET.get("projects");

        Map<String, Integer> existingProjectsMap = new HashMap<>();
        for (Object obj : projectsNode) {
            JSONObject project = (JSONObject) obj;
            String name = (String) project.get("name");
            Integer id = ((Number) project.get("id")).intValue();
            existingProjectsMap.put(name, id);
        }

        List<Project> projectsNotInTestRail = entities.get(EntityType.PROJECT)
                .stream()
                .map(Project.class::cast)
                .filter(project -> existingProjectsMap.get(project.getName()) == null)
                .toList();

        entities.put(EntityType.PROJECT, projectsNotInTestRail.stream().map(project -> (Entity) project).toList());

        List<ProjectDTO> projectsDTO = projectsNotInTestRail
                .stream()
                .map(ProjectDTO::new)
                .toList();

        for (ProjectDTO projectDTO: projectsDTO) {
            try {
                JSONObject createdProject = (JSONObject) client.sendPost("add_project", projectDTO.toJson());
                existingProjectsMap.put(projectDTO.getName(), ((Number) createdProject.get("id")).intValue());
            } catch (Exception e) {
                logger.warn("An error occurred when adding project {} to testrail : {}", projectDTO.getName(), e.getMessage());
                throw e;
            }
        }

        // Test Suites
        List<TestSuite> suites = entities.get(EntityType.TEST_SUITE)
                .stream()
                .map(TestSuite.class::cast)
                .toList();

        // Test Cases
        List<TestCase> cases = entities.get(EntityType.TEST_CASE)
                .stream()
                .map(TestCase.class::cast)
                .toList();

        // Test Runs
        List<TestRun> runs = entities.get(EntityType.TEST_RUN)
                .stream()
                .map(TestRun.class::cast)
                .toList();

        // Test Results
        List<TestResult> results = entities.get(EntityType.TEST_RESULT)
                .stream()
                .map(TestResult.class::cast)
                .toList();



        /*
         * Important considerations:
         * - Careful with the order of exports (project -> suite -> section -> case -> run -> results)
         * - TAF TestSuite <-> TestRail Suite AND Section (2 exprts per TAF TestSuite)
         * - TestResults can be batched as long as all the result are under the same
         * Suite.
         * - Consider API rate limits? Could be solved as a more generic problem (not TR
         * specific)
         */
    }

}
