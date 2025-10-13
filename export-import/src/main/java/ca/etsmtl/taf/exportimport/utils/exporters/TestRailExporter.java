package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.inbound.*;
import ca.etsmtl.taf.exportimport.dtos.testrail.outbound.*;
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
import java.util.stream.Collectors;

@Component("testrail")
public class TestRailExporter implements Exporter {

    private final APIClient client;

    private static final Logger logger = LoggerFactory.getLogger(TestRailExporter.class);

    @Autowired
    public TestRailExporter(TestRailConfig testRailConfig) {
        this.client = testRailConfig.createClient();
    }

    /*
     * Important considerations:
     * - Careful with the order of exports (project -> suite -> section -> case -> run -> results)
     * - TAF TestSuite <-> TestRail Suite AND Section (2 exprts per TAF TestSuite)
     * - TestResults can be batched as long as all the result are under the same
     * Case and Run (in same Suite).
     * - Consider API rate limits? Could be solved as a more generic problem (not TR
     * specific)
     */

    @Override
    public void exportTo(Map<EntityType, List<Entity>> entities) throws Exception {
        // Projects
        List<ProjectData> testrailProjectsToExport = exportProject(entities);

        List<TestSuite> allTestSuitesExported = new ArrayList<>();
        List<TestCase> allTestCasesExported = new ArrayList<>();
        List<TestRun> allTestRunsExported = new ArrayList<>();
        List<TestResult> allTestResultsExported = new ArrayList<>();

        for (ProjectData projectData : testrailProjectsToExport) {
            List<TestSuiteData> testrailSuitesOfProjectToExport = exportSuite(entities, projectData, allTestSuitesExported);

            Map<String, Integer> testCaseIdsMap = new HashMap<>();
            Map<String, Integer> testRunIdsMap = new HashMap<>();

            for (TestSuiteData testSuiteData: testrailSuitesOfProjectToExport) {
                // section
                createSection(projectData, testSuiteData);

                // case
                List<TestCase> testCasesExported = exportCase(projectData, testSuiteData, entities, testCaseIdsMap);
                allTestCasesExported.addAll(testCasesExported);

                // run
                List<TestRun> testRunsExported = exportRun(projectData, testSuiteData, entities, testRunIdsMap);
                allTestRunsExported.addAll(testRunsExported);

                //result
                List<TestResult> testResultsExported = exportResult(projectData, testSuiteData, entities, testCaseIdsMap, testRunIdsMap);
                allTestResultsExported.addAll(testResultsExported);
            }

            projectData.setTestSuiteDatas(testrailSuitesOfProjectToExport);
        }

        entities.put(EntityType.TEST_SUITE, allTestSuitesExported.stream().map(testSuite -> (Entity) testSuite).toList());
        entities.put(EntityType.TEST_CASE, allTestCasesExported.stream().map(testCase -> (Entity) testCase).toList());
        entities.put(EntityType.TEST_RUN, allTestRunsExported.stream().map(testRun -> (Entity) testRun).toList());
        entities.put(EntityType.TEST_RESULT, allTestResultsExported.stream().map(testResult -> (Entity) testResult).toList());
    }

    private List<TestSuiteData> exportSuite(Map<EntityType, List<Entity>> entities, ProjectData projectData, List<TestSuite> allTestSuitesExported) throws IOException, APIException {
        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("get_suites/" + projectData.getTestrailId());
        } catch (Exception e) {
            logger.warn("An error occurred when getting suites of project {} from testrail : {}", projectData.getTestrailId(), e.getMessage());
            throw e;
        }

        JSONArray suitesNode = (JSONArray) responseGET.get("suites");

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
                .filter(testSuite -> testSuite.getProjectId().equals(projectData.getId()))
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

        allTestSuitesExported.addAll(testSuiteOfProjectNotInTR);


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
        return testrailSuitesOfProjectToExport;
    }

    private List<TestResult> exportResult(
            ProjectData projectData, TestSuiteData testSuiteData, Map<EntityType, List<Entity>> entities,
            Map<String, Integer> testCaseIdsMap, Map<String, Integer> testRunIdsMap
    ) throws IOException, APIException {
        Set<String> testRunIdsOfSuite = entities.get(EntityType.TEST_RUN)
                .stream()
                .map(TestRun.class::cast)
                .filter(testRun -> testRun.getTestSuiteId().equals(testSuiteData.getId()))
                .map(TestRun::get_id)
                .collect(Collectors.toSet());

        Map<String, List<TestResult>> testResultsByRunIdMap = entities.get(EntityType.TEST_RESULT)
                .stream()
                .map(TestResult.class::cast)
                .filter(testResult -> testRunIdsOfSuite.contains(testResult.getTestRunId()))
                .collect(Collectors.groupingBy(TestResult::getTestRunId));

        List<TestResult> allTestResultsOfRunNotInTR = new ArrayList<>();

        for (Map.Entry<String, List<TestResult>> entry : testResultsByRunIdMap.entrySet()) {
            String testRunId = entry.getKey();
            Integer testRunTestrailId = testRunIdsMap.get(testRunId);

            JSONObject responseGET;
            try {
                responseGET = (JSONObject) client.sendGet("get_results_for_run/" + testRunTestrailId);
            } catch (Exception e) {
                logger.warn("An error occurred when gettings results of run {} of section {} of suite {} of project {} from testrail : {}",
                        testRunId, SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName(), e.getMessage()
                );
                throw e;
            }

            JSONArray resultsNode = (JSONArray) responseGET.get("results");

            Map<String, Integer> testrailTestResultsMap = new HashMap<>();
            for (Object obj : resultsNode) {
                JSONObject runTR = (JSONObject) obj;
                String name = (String) runTR.get("comment");
                Integer id = ((Number) runTR.get("id")).intValue();
                testrailTestResultsMap.put(name, id);
            }
            List<TestResultData> testrailResultsOfRunToExport = new ArrayList<>();
            List<TestResult> testResultsOfRun = entry.getValue();

            List<TestResult> testResultsOfRunNotInTR = testResultsOfRun.stream()
                    .filter(testResult -> {
                        Integer testResultTestrailId = testrailTestResultsMap.get(testResult.get_id());
                        boolean isTestResultInTR = testResultTestrailId != null;
                        if (isTestResultInTR) {
                            // RAPPEL : le "comment" est l'id de notre BD
                            testrailResultsOfRunToExport.add(new TestResultData(testResult.get_id(), testResult.get_id(), testResultTestrailId));
                        }
                        return !isTestResultInTR;
                    }).toList();

            List<TestResultDTO> testResultDTOS = testResultsOfRunNotInTR
                    .stream()
                    .map(testResult -> {
                        Integer testCaseTestrailId = testCaseIdsMap.get(testResult.getTestCaseId());
                        return new TestResultDTO(testResult, testRunTestrailId, testCaseTestrailId);
                    })
                    .toList();

            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> results = testResultDTOS.stream().map(TestResultDTO::toJson).toList();

            if (results.isEmpty()) {
                logger.info("No results to add for run {} of section {} of suite {} of project {} to testrail",
                        testRunId, SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName()
                );
                continue;
            }

            try {
                body.put("results", results);
                client.sendPost("add_results_for_cases/" + testRunTestrailId, body);
            } catch (Exception e) {
                logger.warn("An error occurred when adding results of run {} of section {} of suite {} of project {} to testrail : {}",
                        testRunId, SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName(), e.getMessage()
                );
                throw e;
            }

            allTestResultsOfRunNotInTR.addAll(testResultsOfRunNotInTR);
        }
        return allTestResultsOfRunNotInTR;
    }

    private List<TestRun> exportRun(
            ProjectData projectData, TestSuiteData testSuiteData, Map<EntityType, List<Entity>> entities, Map<String, Integer> testRunIdsMap
    ) throws IOException, APIException {
        int projectId = projectData.getTestrailId();
        int testSuiteId = testSuiteData.getTestrailId();

        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("get_runs/" + projectId + "&suite_id=" + testSuiteId);
        } catch (Exception e) {
            logger.warn("An error occurred when getting runs of suite {} of project {} from testrail : {}",
                    testSuiteId, projectId, e.getMessage());
            throw e;
        }

        JSONArray runsNode = (JSONArray) responseGET.get("runs");

        Map<String, Integer> testrailTestRunMap = new HashMap<>();
        for (Object obj : runsNode) {
            JSONObject runTR = (JSONObject) obj;
            String name = (String) runTR.get("name");
            Integer id = ((Number) runTR.get("id")).intValue();
            testrailTestRunMap.put(name, id);
        }
        List<TestRunData> testrailRunsOfSuiteToExport = new ArrayList<>();

        List<TestRun> testRunsOfSuite = entities.get(EntityType.TEST_RUN)
                .stream()
                .map(TestRun.class::cast)
                .filter(testRun -> testRun.getTestSuiteId().equals(testSuiteData.getId()))
                .toList();

        List<TestRun> testRunsOfSuiteNotInTR = testRunsOfSuite.stream()
                .filter(testRun -> {
                    Integer testRunTestrailId = testrailTestRunMap.get(testRun.getName());
                    boolean isTestRunInTR = testRunTestrailId != null;
                    if (isTestRunInTR) {
                        testrailRunsOfSuiteToExport.add(new TestRunData(testRun.getName(), testRun.get_id(), testRunTestrailId));
                        testRunIdsMap.put(testRun.get_id(), testRunTestrailId);
                    }
                    return !isTestRunInTR;
                }).toList();

        List<TestRunDTO> testRunDTOS = testRunsOfSuiteNotInTR
                .stream()
                .map(testRun -> new TestRunDTO(testRun, testSuiteId))
                .toList();

        for (TestRunDTO testRunDTO: testRunDTOS) {
            try {
                JSONObject createdTestRun = (JSONObject) client.sendPost("add_run/" + projectId, testRunDTO.toJson());
                int testRunTestrailId = ((Number) createdTestRun.get("id")).intValue();
                testrailRunsOfSuiteToExport.add(
                        new TestRunData(testRunDTO.getName(), testRunDTO.getId(), testRunTestrailId)
                );
                testRunIdsMap.put(testRunDTO.getId(), testRunTestrailId);
            } catch (Exception e) {
                logger.warn("An error occurred when adding run {} of section {} of suite {} of project {} to testrail : {}",
                        testRunDTO.getName(), SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName(), e.getMessage()
                );
                throw e;
            }
        }

        testSuiteData.setTestRunDataList(testrailRunsOfSuiteToExport);

        return testRunsOfSuiteNotInTR;
    }

    private List<TestCase> exportCase(
            ProjectData projectData, TestSuiteData testSuiteData, Map<EntityType, List<Entity>> entities, Map<String, Integer> testCaseIdsMap
    ) throws IOException, APIException {
        int projectId = projectData.getTestrailId();
        int testSuiteId = testSuiteData.getTestrailId();
        int sectionId = testSuiteData.getSectionId();

        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("/get_cases/"+ projectId + "&suite_id=" + testSuiteId);
        } catch (Exception e) {
            logger.warn("An error occurred when getting cases of section {} of suite {} of project {} from testrail : {}",
                    sectionId, testSuiteId, projectId, e.getMessage()
            );
            throw e;
        }

        JSONArray casesNode =  (JSONArray) responseGET.get("cases");

        Map<String, Integer> testrailTestCaseMap = new HashMap<>();
        for (Object obj : casesNode) {
            JSONObject caseTR = (JSONObject) obj;
            String name = (String) caseTR.get("title");
            Integer id = ((Number) caseTR.get("id")).intValue();
            testrailTestCaseMap.put(name, id);
        }
        List<TestCaseData> testrailCasesOfSuiteToExport = new ArrayList<>();

        List<TestCase> testCasesOfSuite = entities.get(EntityType.TEST_CASE)
                .stream()
                .map(TestCase.class::cast)
                .filter(testCase -> testCase.getTestSuiteId().equals(testSuiteData.getId()))
                .toList();

        List<TestCase> testCasesOfSuiteNotInTR = testCasesOfSuite.stream()
                .filter(testCase -> {
                    Integer caseIdTR = testrailTestCaseMap.get(testCase.getName());
                    boolean isCaseInTR = caseIdTR != null;
                    if (isCaseInTR) {
                        testrailCasesOfSuiteToExport.add(new TestCaseData(testCase.getName(), testCase.get_id(), caseIdTR));
                        testCaseIdsMap.put(testCase.get_id(), caseIdTR);
                    }
                    return !isCaseInTR;
                }).toList();

        List<TestCaseDTO> testCaseDTOS = testCasesOfSuiteNotInTR
                .stream()
                .map(testCase -> new TestCaseDTO(testCase, sectionId))
                .toList();

        for (TestCaseDTO testCaseDTO: testCaseDTOS) {
            try {
                JSONObject createdCase = (JSONObject) client.sendPost("add_case/" + sectionId, testCaseDTO.toJson());
                int testCaseTestrailId = ((Number) createdCase.get("id")).intValue();
                testrailCasesOfSuiteToExport.add(
                        new TestCaseData(testCaseDTO.getTitle(), testCaseDTO.getId(), testCaseTestrailId)
                );
                testCaseIdsMap.put(testCaseDTO.getId(), testCaseTestrailId);
            } catch (Exception e) {
                logger.warn("An error occurred when adding case {} of section {} of suite {} of project {} to testrail : {}",
                        testCaseDTO.getTitle(), SectionDTO.ROOT_SECTION_NAME, testSuiteData.getName(), projectData.getName(), e.getMessage()
                );
                throw e;
            }
        }

        testSuiteData.setTestCaseDataList(testrailCasesOfSuiteToExport);

        return testCasesOfSuiteNotInTR;
    }

    private void createSection(ProjectData projectData, TestSuiteData testSuiteData) throws IOException, APIException {
        int projectId = projectData.getTestrailId();
        int testSuiteId = testSuiteData.getTestrailId();

        JSONObject responseGET;
        try {
            responseGET = (JSONObject) client.sendGet("/get_sections/"+ projectId + "&suite_id=" + testSuiteId);
        } catch (Exception e) {
            logger.warn("An error occurred when getting sections of suite {} of project {} from testrail : {}",
                    testSuiteId, projectId, e.getMessage()
            );
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
            logger.warn("An error occurred when creating root section of suite {} of project {} from testrail : {}",
                    testSuiteId, projectId, e.getMessage()
            );
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
                testrailProjectsToExport.add(
                        new ProjectData(projectDTO.getName(), projectDTO.getId(), ((Number) createdProject.get("id")).intValue())
                );
            } catch (Exception e) {
                logger.warn("An error occurred when adding project {} to testrail : {}",
                        projectDTO.getName(), e.getMessage()
                );
                throw e;
            }
        }

        return testrailProjectsToExport;
    }

}
