package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.ProjectDTO;
import ca.etsmtl.taf.exportimport.dtos.testrail.TestSuiteDTO;
import ca.etsmtl.taf.exportimport.models.*;

import com.gurock.testrail.APIClient;
import com.gurock.testrail.APIException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

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
        // Projects

        // key:projectId (de testRail), value: projectName
        Map<Integer, String> testrailProjectMapExported = exportProject(entities);

        //TODO: inverser pour avoir les suiteId en première clé, pour faciliter pour case et run

        // Test Suites
        // key:projectId (de testRail), value: Map<suiteIdTR, suiteName>
        Map<Integer, Map<Integer, String>> testrailSuiteMapToExport = new HashMap<>();

        for (Map.Entry<Integer, String> entry : testrailProjectMapExported.entrySet()) {
            Integer projectId = entry.getKey();
            String projectName = entry.getValue();

            JSONObject responseGET;
            try {
                responseGET = (JSONObject) client.sendGet("get_suites/" + projectId);
            } catch (Exception e) {
                logger.warn("An error occurred when getting projects from testrail : {}", e.getMessage());
                throw e;
            }

            JSONArray suitesNode = (JSONArray) responseGET.get("suites");

            //Map<> name et id (de suite de testrail)
            Map<String, Integer> testrailTestSuiteMap = new HashMap<>();
            for (Object obj : suitesNode) {
                JSONObject project = (JSONObject) obj;
                String name = (String) project.get("name");
                Integer id = ((Number) project.get("id")).intValue();
                testrailTestSuiteMap.put(name, id);
            }
            Map<Integer, String> testrailSuiteOfProjectMapToExport = new HashMap<>();

            //Tous les TestSuite du project qu'on veut exporter
            List<TestSuite> testSuiteOfProject = entities.get(EntityType.TEST_SUITE)
                    .stream()
                    .map(TestSuite.class::cast)
                    .filter(testSuite -> testSuite.getProjectId().equals(projectName))
                    .toList();

            List<TestSuite> testSuiteOfProjectNotInTR = testSuiteOfProject.stream()
                    .filter(testSuite -> {
                        Integer suiteIdTR = testrailTestSuiteMap.get(testSuite.getName());
                        boolean isSuiteInTR = suiteIdTR != null;
                        if (isSuiteInTR) {
                            testrailSuiteOfProjectMapToExport.put(suiteIdTR, testSuite.getName());
                        }
                        return !isSuiteInTR;
                    }).toList();

            List<TestSuiteDTO> testSuiteDTOS = testSuiteOfProjectNotInTR
                    .stream()
                    .map(TestSuiteDTO::new)
                    .toList();

            for (TestSuiteDTO testSuiteDTO: testSuiteDTOS) {
                try {

                    JSONObject createdSuite = (JSONObject) client.sendPost("add_suite/" + projectId, testSuiteDTO.toJson());
                    testrailSuiteOfProjectMapToExport.put(((Number) createdSuite.get("id")).intValue(), testSuiteDTO.getName());
                } catch (Exception e) {
                    logger.warn("An error occurred when adding suite {} of project {} to testrail : {}", testSuiteDTO.getName(), projectName, e.getMessage());
                    throw e;
                }
            }


            testrailSuiteMapToExport.put(projectId, testrailSuiteOfProjectMapToExport);
        }

        printMap(testrailSuiteMapToExport);

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

    private static void printMap(Map<Integer, Map<Integer, String>> testrailSuiteMapToExport) {
        System.out.println("===== Testrail Suite Map To Export =====");

        for (Map.Entry<Integer, Map<Integer, String>> projectEntry : testrailSuiteMapToExport.entrySet()) {
            Integer projectId = projectEntry.getKey();
            Map<Integer, String> suites = projectEntry.getValue();

            System.out.println("Project ID: " + projectId);

            if (suites == null || suites.isEmpty()) {
                System.out.println("  (No suites)");
                continue;
            }

            for (Map.Entry<Integer, String> suiteEntry : suites.entrySet()) {
                Integer suiteId = suiteEntry.getKey();
                String suiteName = suiteEntry.getValue();
                System.out.println("  Suite ID: " + suiteId + " | Suite Name: " + suiteName);
            }

            System.out.println("----------------------------------------");
        }
    }

    private Map<Integer, String> exportProject(Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("get_projects");
        } catch (Exception e) {
            logger.warn("An error occurred when getting projects from testrail : {}", e.getMessage());
            throw e;
        }

        // on veux exporter projet 2 et 3
        // dans testRail, on a projets 1 et 2

        JSONArray projectsNode = (JSONArray) responseGET.get("projects");

        //1, 2
        Map<String, Integer> testrailProjectMap = new HashMap<>();
        for (Object obj : projectsNode) {
            JSONObject project = (JSONObject) obj;
            String name = (String) project.get("name");
            Integer id = ((Number) project.get("id")).intValue();
            testrailProjectMap.put(name, id);
        }
        Map<Integer, String> testrailProjectMapToExport = new HashMap<>();

        // TOUS les projets à exporter => 2, 3
        List<Project> projects = entities.get(EntityType.PROJECT)
                .stream()
                .map(Project.class::cast)
                .toList();

        // TOUS les projets qui n'existe pas dans Test Rail
        List<Project> projectsNotInTR = projects
                .stream()
                .filter(project -> {
                    Integer projectIdTR = testrailProjectMap.get(project.getName());
                    boolean isProjectInTR = projectIdTR != null;
                    if (isProjectInTR) {
                        // on ajoute le project 2, déjà dans testRail
                        testrailProjectMapToExport.put(projectIdTR, project.getName());
                    }
                    return !isProjectInTR;
                })
                .toList();

        entities.put(EntityType.PROJECT, projectsNotInTR.stream().map(project -> (Entity) project).toList());

        List<ProjectDTO> projectsDTO = projectsNotInTR
                .stream()
                .map(ProjectDTO::new)
                .toList();

        for (ProjectDTO projectDTO: projectsDTO) {
            try {
                // on crée projet 3
                JSONObject createdProject = (JSONObject) client.sendPost("add_project", projectDTO.toJson());
                testrailProjectMapToExport.put(((Number) createdProject.get("id")).intValue(), projectDTO.getName());
            } catch (Exception e) {
                logger.warn("An error occurred when adding project {} to testrail : {}", projectDTO.getName(), e.getMessage());
                throw e;
            }
        }

        //on ne veux que projet 2 et 3
        return testrailProjectMapToExport;
    }

}
