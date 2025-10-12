package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.*;
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
        List<ProjectData> testrailProjectsToExport = exportProject(entities);

        for (ProjectData projectData : testrailProjectsToExport) {
            JSONObject responseGET;
            try {
                responseGET = (JSONObject) client.sendGet("get_suites/" + projectData.getTestrailId());
            } catch (Exception e) {
                logger.warn("An error occurred when getting suites of project {} from testrail : {}", projectData.getTestrailId(), e.getMessage());
                throw e;
            }

            JSONArray suitesNode = (JSONArray) responseGET.get("suites");

            //Map<> name et id (de suite de testrail)
            Map<String, Integer> testrailTestSuiteMap = new HashMap<>();
            for (Object obj : suitesNode) {
                JSONObject suite = (JSONObject) obj;
                String name = (String) suite.get("name");
                Integer id = ((Number) suite.get("id")).intValue();
                testrailTestSuiteMap.put(name, id);
            }
            List<TestSuiteData> testrailSuitesOfProjectToExport = new ArrayList<>();

            List<TestSuite> testSuiteOfProject = entities.get(EntityType.TEST_SUITE)
                    .stream()
                    .map(TestSuite.class::cast)
                    .filter(testSuite -> {
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

            entities.put(EntityType.TEST_SUITE, testSuiteOfProjectNotInTR.stream().map(testSuite -> (Entity) testSuite).toList());

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

            List<TestCase> allTestCasesExported = new ArrayList<>();
            for (TestSuiteData testSuiteData: testrailSuitesOfProjectToExport) {
                System.out.println("starting test suite creation for suite: " + testSuiteData.getName());
                // section
                createSection(projectData, testSuiteData);

                // case
                List<TestCase> testCasesExported = exportCase(projectData, testSuiteData, entities);
                allTestCasesExported.addAll(testCasesExported);

                // run
            }
            entities.put(EntityType.TEST_CASE, allTestCasesExported.stream().map(testCase -> (Entity) testCase).toList());

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

    private List<TestCase> exportCase(ProjectData projectData, TestSuiteData testSuiteData, Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        int projectId = projectData.getTestrailId();
        int testSuiteId = testSuiteData.getTestrailId();
        int sectionId = testSuiteData.getSectionId();

        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("/get_cases/"+ projectId + "&suite_id=" + testSuiteId);
        } catch (Exception e) {
            logger.warn("An error occurred when getting cases of section {} of suite {} of project {} from testrail : {}", sectionId, testSuiteId, projectId, e.getMessage());
            throw e;
        }

        JSONArray casesNode =  (JSONArray) responseGET.get("cases");

        //Map<> name et id (de case de testrail)
        Map<String, Integer> testrailTestCaseMap = new HashMap<>();
        for (Object obj : casesNode) {
            JSONObject caseTR = (JSONObject) obj;
            String name = (String) caseTR.get("title");
            Integer id = ((Number) caseTR.get("id")).intValue();
            testrailTestCaseMap.put(name, id);
        }
        List<TestCaseData> testrailCasesOfSuiteToExport = new ArrayList<>();

        List<TestCase> testCaseOfSuite = entities.get(EntityType.TEST_CASE)
                .stream()
                .map(TestCase.class::cast)
                .filter(testCase -> testCase.getTestSuiteId().equals(testSuiteData.getId()))
                .toList();

        List<TestCase> testCaseOfSuiteNotInTR = testCaseOfSuite.stream()
                .filter(testCase -> {
                    Integer caseIdTR = testrailTestCaseMap.get(testCase.getName());
                    boolean isCaseInTR = caseIdTR != null;
                    if (isCaseInTR) {
                        testrailCasesOfSuiteToExport.add(new TestCaseData(testCase.getName(), testCase.get_id(), caseIdTR));
                    }
                    return !isCaseInTR;
                }).toList();

        List<TestCaseDTO> testCaseDTOS = testCaseOfSuiteNotInTR
                .stream()
                .map(testCase -> new TestCaseDTO(testCase, sectionId))
                .toList();

        for (TestCaseDTO testCaseDTO: testCaseDTOS) {
            try {
                JSONObject createdCase = (JSONObject) client.sendPost("add_case/" + sectionId, testCaseDTO.toJson());
                testrailCasesOfSuiteToExport.add(new TestCaseData(testCaseDTO.getTitle(), testCaseDTO.getId(), ((Number) createdCase.get("id")).intValue()));
            } catch (Exception e) {
                logger.warn("An error occurred when adding case {} of section {} of suite {} of project {} to testrail : {}",
                        testCaseDTO.getTitle(), SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName(), e.getMessage()
                );
                throw e;
            }
        }

        testSuiteData.setTestCaseDataList(testrailCasesOfSuiteToExport);

        return testCaseOfSuiteNotInTR;
    }

    private void createSection(ProjectData projectData, TestSuiteData testSuiteData) throws IOException, APIException {
        int projectId = projectData.getTestrailId();
        int testSuiteId = testSuiteData.getTestrailId();

        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("/get_sections/"+ projectId + "&suite_id=" + testSuiteId);
        } catch (Exception e) {
            logger.warn("An error occurred when getting sections of suite {} of project {} from testrail : {}", testSuiteId, projectId, e.getMessage());
            throw e;
        }

        JSONArray sectionsNode =  (JSONArray) responseGET.get("sections");
        if (!sectionsNode.isEmpty()) {
            for (Object obj : sectionsNode) {
                JSONObject section = (JSONObject) obj;
                String name = (String) section.get("name");
                if (SectionDTO.ROOT_SECTION_NAME.equals(name)) {
                    Integer sectionId = ((Number) section.get("id")).intValue();
                    testSuiteData.setSectionId(sectionId);
                }
            }
            return;
        }

        SectionDTO sectionDTO = new SectionDTO(testSuiteId);

        try {
            JSONObject createdSection = (JSONObject) client.sendPost("/add_section/" + projectId, sectionDTO.toJson());
            int sectionId = ((Number) createdSection.get("id")).intValue();
            testSuiteData.setSectionId(sectionId);
        } catch (Exception e) {
            logger.warn("An error occurred when creating root section of suite {} of project {} from testrail : {}", testSuiteId, projectId, e.getMessage());
            throw e;
        }
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
