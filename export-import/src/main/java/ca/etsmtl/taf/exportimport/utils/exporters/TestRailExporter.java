package ca.etsmtl.taf.exportimport.utils.exporters;

import ca.etsmtl.taf.exportimport.config.TestRailConfig;
import ca.etsmtl.taf.exportimport.dtos.testrail.*;
import ca.etsmtl.taf.exportimport.models.*;

import ca.etsmtl.taf.exportimport.repositories.TestRailMappingRepository;
import ca.etsmtl.taf.exportimport.services.EntityLookupService;
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
    private final TestRailMappingRepository testRailMappingRepository;
    private final EntityLookupService entityLookupService;

    List<Project> allProjectsExported;
    List<TestSuite> allTestSuitesExported;
    List<TestCase> allTestCasesExported;
    List<TestRun> allTestRunsExported;
    List<TestResult> allTestResultsExported;

    private static final Logger logger = LoggerFactory.getLogger(TestRailExporter.class);

    @Autowired
    public TestRailExporter(TestRailConfig testRailConfig, TestRailMappingRepository testRailMappingRepository, EntityLookupService entityLookupService) {
        this.client = testRailConfig.createClient();
        this.testRailMappingRepository = testRailMappingRepository;
        this.entityLookupService = entityLookupService;
    }

    /*
     * Important considerations:
     * - Consider API rate limits? Could be solved as a more generic problem (not TR specific)
     */

    @Override
    public void exportTo(Map<EntityType, List<Entity>> entities) throws Exception {
        allProjectsExported = new ArrayList<>();
        allTestSuitesExported = new ArrayList<>();
        allTestCasesExported = new ArrayList<>();
        allTestRunsExported = new ArrayList<>();
        allTestResultsExported = new ArrayList<>();

        exportProjects(entities);
        exportSuites(entities);
        exportCases(entities);
        exportRuns(entities);
        exportResults(entities);

        entities.put(EntityType.PROJECT, allProjectsExported.stream().map(project -> (Entity) project).toList());
        entities.put(EntityType.TEST_SUITE, allTestSuitesExported.stream().map(testSuite -> (Entity) testSuite).toList());
        entities.put(EntityType.TEST_CASE, allTestCasesExported.stream().map(testCase -> (Entity) testCase).toList());
        entities.put(EntityType.TEST_RUN, allTestRunsExported.stream().map(testRun -> (Entity) testRun).toList());
        entities.put(EntityType.TEST_RESULT, allTestResultsExported.stream().map(testResult -> (Entity) testResult).toList());
    }

    private void exportProjects(Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        List<Project> projects = Optional.ofNullable(entities.get(EntityType.PROJECT))
                .stream()
                .flatMap(list -> list.stream().map(Project.class::cast))
                .toList();

        for (Project project : projects) {
            String projectKey = TestRailMappingRepository.PROJECT_KEY_SUFFIX + project.get_id();
            Integer projectIdTR = testRailMappingRepository.get(projectKey);

            if (projectIdTR == null) {
                ProjectDTO projectDTO = new ProjectDTO(project);
                try {
                    JSONObject createdProject = (JSONObject) client.sendPost("add_project", projectDTO.toJson());
                    projectIdTR = ((Number) createdProject.get("id")).intValue();

                    testRailMappingRepository.put(projectKey, projectIdTR);
                } catch (Exception e) {
                    logger.warn("An error occurred when adding project {} to testrail : {}",
                            projectDTO.getId(), e.getMessage()
                    );
                    throw e;
                }
                allProjectsExported.add(project);
            }
        }
    }

    private void exportSuites(
            Map<EntityType, List<Entity>> entities
    ) throws IOException, APIException {
        List<TestSuite> testSuites = Optional.ofNullable(entities.get(EntityType.TEST_SUITE))
                .stream()
                .flatMap(list -> list.stream().map(TestSuite.class::cast))
                .toList();

        for (TestSuite testSuite : testSuites) {
            String testSuiteKey = TestRailMappingRepository.TEST_SUITE_KEY_SUFFIX + testSuite.get_id();
            Integer testSuiteIdTR = testRailMappingRepository.get(testSuiteKey);

            if (testSuiteIdTR == null) {
                String projectId =  testSuite.getProjectId();
                String projectKey = TestRailMappingRepository.PROJECT_KEY_SUFFIX + projectId;
                Integer projectIdTR = testRailMappingRepository.get(projectKey);

                TestSuiteDTO testSuiteDTO = new TestSuiteDTO(testSuite);
                try {
                    JSONObject createdSuite = (JSONObject) client.sendPost("add_suite/" + projectIdTR, testSuiteDTO.toJson());
                    testSuiteIdTR = ((Number) createdSuite.get("id")).intValue();

                    testRailMappingRepository.put(testSuiteKey, testSuiteIdTR);

                    createSection(testSuite);
                } catch (Exception e) {
                    logger.warn("An error occurred when adding suite {} of project {} to testrail : {}",
                            testSuiteDTO.getId(), projectId, e.getMessage()
                    );
                    throw e;
                }
                allTestSuitesExported.add(testSuite);
            }
        }
    }

    private void createSection(TestSuite testSuite) throws IOException, APIException {
        String testSuiteId = testSuite.get_id();
        String sectionKey = TestRailMappingRepository.SECTION_KEY_SUFFIX + testSuiteId;
        Integer sectionIdTR = testRailMappingRepository.get(sectionKey);

        if (sectionIdTR == null) {
            String testSuiteKey = TestRailMappingRepository.TEST_SUITE_KEY_SUFFIX + testSuiteId;
            Integer testSuiteIdTR = testRailMappingRepository.get(testSuiteKey);
            String projectId = testSuite.getProjectId();

            String projectKey = TestRailMappingRepository.PROJECT_KEY_SUFFIX + projectId;
            Integer projectIdTR = testRailMappingRepository.get(projectKey);

            SectionDTO sectionDTO = new SectionDTO(testSuiteIdTR, testSuite.getName());
            try {
                JSONObject createdSection = (JSONObject) client.sendPost("add_section/" + projectIdTR, sectionDTO.toJson());
                sectionIdTR = ((Number) createdSection.get("id")).intValue();
                testRailMappingRepository.put(sectionKey, sectionIdTR);
            } catch (Exception e) {
                logger.warn("An error occurred when creating root section of suite {} of project {} from testrail : {}",
                        testSuiteId, projectId, e.getMessage()
                );
                throw e;
            }
        }
    }

    private void exportCases(Map<EntityType, List<Entity>> entities
    ) throws IOException, APIException {
        List<TestCase> testCases = Optional.ofNullable(entities.get(EntityType.TEST_CASE))
                .stream()
                .flatMap(list -> list.stream().map(TestCase.class::cast))
                .toList();

        for (TestCase testCase: testCases) {
            String testCaseId = testCase.get_id();

            String testCaseKey = TestRailMappingRepository.TEST_CASE_KEY_SUFFIX + testCaseId;
            Integer testCaseIdTR = testRailMappingRepository.get(testCaseKey);

            if (testCaseIdTR == null) {
                String testSuiteId = testCase.getTestSuiteId();
                String sectionKey = TestRailMappingRepository.SECTION_KEY_SUFFIX + testSuiteId;
                Integer sectionIdTR = testRailMappingRepository.get(sectionKey);

                TestCaseDTO testCaseDTO = new TestCaseDTO(testCase, sectionIdTR);
                try {
                    JSONObject createdTestCase = (JSONObject) client.sendPost("add_case/" + sectionIdTR, testCaseDTO.toJson());
                    testCaseIdTR = ((Number) createdTestCase.get("id")).intValue();
                    testRailMappingRepository.put(testCaseKey, testCaseIdTR);
                } catch (Exception e) {
                    logger.warn("An error occurred when adding case {} of section {} of suite {} to testrail : {}",
                            testCaseId, testSuiteId, testSuiteId, e.getMessage()
                    );
                    throw e;
                }
                allTestCasesExported.add(testCase);
            }
        }
    }

    private void exportRuns(Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        List<TestRun> testRuns = Optional.ofNullable(entities.get(EntityType.TEST_RUN))
                .stream()
                .flatMap(list -> list.stream().map(TestRun.class::cast))
                .toList();

        for (TestRun testRun: testRuns) {
            String testRunId = testRun.get_id();

            String testRunKey = TestRailMappingRepository.TEST_RUN_KEY_SUFFIX + testRunId;
            Integer testRunIdTR = testRailMappingRepository.get(testRunKey);

            if (testRunIdTR == null) {
                String testSuiteId = testRun.getTestSuiteId();
                String testSuiteKey = TestRailMappingRepository.TEST_SUITE_KEY_SUFFIX + testSuiteId;
                Integer testSuiteIdTR = testRailMappingRepository.get(testSuiteKey);

                TestSuite testSuite = entityLookupService.findTestSuiteById(testSuiteId);
                String projectId = testSuite.getProjectId();

                String projectKey = TestRailMappingRepository.PROJECT_KEY_SUFFIX + projectId;
                Integer projectIdTR = testRailMappingRepository.get(projectKey);

                List<Integer> testCaseIds = testRun.getTestCaseIds().stream().map(testCaseId -> {
                    String testCaseKey = TestRailMappingRepository.TEST_CASE_KEY_SUFFIX + testCaseId;
                    return testRailMappingRepository.get(testCaseKey);
                }).toList();

                TestRunDTO testRunDTO = new TestRunDTO(testRun, testSuiteIdTR, testCaseIds);
                try {
                    JSONObject createdTestRun = (JSONObject) client.sendPost("add_run/" + projectIdTR, testRunDTO.toJson());
                    testRunIdTR = ((Number) createdTestRun.get("id")).intValue();
                    testRailMappingRepository.put(testRunKey, testRunIdTR);
                } catch (Exception e) {
                    logger.warn("An error occurred when adding run {} of section {} of suite {} of project {} to testrail : {}",
                            testRunId, testSuiteId, testSuiteId, projectId, e.getMessage()
                    );
                    throw e;
                }
                allTestRunsExported.add(testRun);
            }
        }
    }

    private void exportResults(Map<EntityType, List<Entity>> entities) throws IOException, APIException {
        Map<String, List<TestResult>> testResultsByRunIdMap = Optional.ofNullable(entities.get(EntityType.TEST_RESULT))
                .stream()
                .flatMap(list -> list.stream().map(TestResult.class::cast))
                .collect(Collectors.groupingBy(TestResult::getTestRunId));

        for (Map.Entry<String, List<TestResult>> entry : testResultsByRunIdMap.entrySet()) {
            String testRunId = entry.getKey();
            List<TestResult> testResultsOfRun = entry.getValue();
            List<TestResultDTO> testResultDTOS = new ArrayList<>();

            String testRunKey = TestRailMappingRepository.TEST_RUN_KEY_SUFFIX + testRunId;
            Integer testRunIdTR = testRailMappingRepository.get(testRunKey);

            for (TestResult testResult : testResultsOfRun) {
                String testResultId = testResult.get_id();

                String testResultKey = TestRailMappingRepository.TEST_RESULT_KEY_SUFFIX + testResultId;
                Integer testResultIdTR = testRailMappingRepository.get(testResultKey);

                if (testResultIdTR == null) {
                    String testCaseId = testResult.getTestCaseId();
                    String testCaseKey = TestRailMappingRepository.TEST_CASE_KEY_SUFFIX + testCaseId;
                    Integer testCaseIdTR = testRailMappingRepository.get(testCaseKey);

                    testResultDTOS.add(new TestResultDTO(testResult, testRunIdTR, testCaseIdTR));
                    allTestResultsExported.add(testResult);
                }
            }

            if (testResultDTOS.isEmpty()) {
                logger.info("No results to add for run {} to testrail", testRunId);
                continue;
            }

            Map<String, Object> body = new HashMap<>();
            List<Map<String, Object>> results = testResultDTOS.stream().map(TestResultDTO::toJson).toList();
            try {

                body.put("results", results);
                JSONArray createdTestResults = (JSONArray) client.sendPost("add_results_for_cases/" + testRunIdTR, body);

                for (Object createdTestResult : createdTestResults) {
                    JSONObject createdResult = (JSONObject) createdTestResult;

                    Integer testResultIdTR = ((Number) createdResult.get("id")).intValue();
                    String testResultId = createdResult.get("comment").toString();

                    if (testResultId == null || testResultId.isBlank()) {
                        logger.warn("Created result {} has no comment — cannot map back to local TestResult", testResultIdTR);
                    }

                    String testResultKey = TestRailMappingRepository.TEST_RESULT_KEY_SUFFIX + testResultId;
                    testRailMappingRepository.put(testResultKey, testResultIdTR);
                }
            } catch (Exception e) {
                logger.warn("An error occurred when adding results of run {} to testrail : {}", testRunId, e.getMessage());
                throw e;
            }
        }
    }
}
