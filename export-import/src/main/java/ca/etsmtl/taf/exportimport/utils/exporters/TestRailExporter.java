package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.ProjectDTO;
import ca.etsmtl.taf.exportimport.dtos.testrail.ProjectData;
import ca.etsmtl.taf.exportimport.dtos.testrail.TestSuiteDTO;
import ca.etsmtl.taf.exportimport.dtos.testrail.TestSuiteData;
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
        List<ProjectData> testrailProjectsToExport = exportProject(entities);

        //TODO: inverser pour avoir les suiteId en première clé, pour faciliter pour case et run

        // Test Suites
        // key:projectId (de testRail), value: Map<suiteIdTR, suiteName>

        for (ProjectData projectData : testrailProjectsToExport) {
            JSONObject responseGET;
            try {
                responseGET = (JSONObject) client.sendGet("get_suites/" + projectData.getTestrailId());
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
            List<TestSuiteData> testrailSuitesOfProjectToExport = new ArrayList<>();

            List<TestSuite> testSuiteOfProject = entities.get(EntityType.TEST_SUITE)
                    .stream()
                    .map(TestSuite.class::cast)
                    .filter(testSuite -> {
                        System.out.println("Testting for testSuite: " + testSuite.getName());
                        return testSuite.getProjectId().equals(projectData.getId());
                    })
                    .toList();

            List<TestSuite> testSuiteOfProjectNotInTR = testSuiteOfProject.stream()
                    .filter(testSuite -> {
                        Integer suiteIdTR = testrailTestSuiteMap.get(testSuite.getName());
                        boolean isSuiteInTR = suiteIdTR != null;
                        if (isSuiteInTR) {
                            testrailSuitesOfProjectToExport.add(new TestSuiteData(testSuite.getName(), testSuite.get_id(), suiteIdTR));
                        }
                        return !isSuiteInTR;
                    }).toList();

            List<TestSuiteDTO> testSuiteDTOS = testSuiteOfProjectNotInTR
                    .stream()
                    .map(TestSuiteDTO::new)
                    .toList();

            for (TestSuiteDTO testSuiteDTO: testSuiteDTOS) {
                try {
                    JSONObject createdSuite = (JSONObject) client.sendPost("add_suite/" + projectData.getTestrailId(), testSuiteDTO.toJson());
                    testrailSuitesOfProjectToExport.add(new TestSuiteData(testSuiteDTO.getName(), testSuiteDTO.getId(), ((Number) createdSuite.get("id")).intValue()));
                } catch (Exception e) {
                    logger.warn("An error occurred when adding suite {} of project {} to testrail : {}", testSuiteDTO.getName(), projectData.getName(), e.getMessage());
                    throw e;
                }
            }

            projectData.setTestSuiteDatas(testrailSuitesOfProjectToExport);
        }

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

    private List<ProjectData> exportProject(Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("get_projects");
        } catch (Exception e) {
            logger.warn("An error occurred when getting projects from testrail : {}", e.getMessage());
            throw e;
        }

        JSONArray projectsNode = (JSONArray) responseGET.get("projects");

        Map<String, Integer> testrailProjectMap = new HashMap<>();
        for (Object obj : projectsNode) {
            JSONObject project = (JSONObject) obj;
            String name = (String) project.get("name");
            Integer id = ((Number) project.get("id")).intValue();
            testrailProjectMap.put(name, id);
        }
        List<ProjectData> testrailProjectsToExport = new ArrayList<>();

        List<Project> projects = entities.get(EntityType.PROJECT)
                .stream()
                .map(Project.class::cast)
                .toList();

        List<Project> projectsNotInTR = projects
                .stream()
                .filter(project -> {
                    Integer projectIdTR = testrailProjectMap.get(project.getName());
                    boolean isProjectInTR = projectIdTR != null;
                    if (isProjectInTR) {
                        // on ajoute le project 2, déjà dans testRail
                        testrailProjectsToExport.add(new ProjectData(project.getName(), project.get_id(), projectIdTR));
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
                JSONObject createdProject = (JSONObject) client.sendPost("add_project", projectDTO.toJson());
                testrailProjectsToExport.add(new ProjectData(projectDTO.getName(), projectDTO.getId(), ((Number) createdProject.get("id")).intValue()));
            } catch (Exception e) {
                logger.warn("An error occurred when adding project {} to testrail : {}", projectDTO.getName(), e.getMessage());
                throw e;
            }
        }

        return testrailProjectsToExport;
    }

}
