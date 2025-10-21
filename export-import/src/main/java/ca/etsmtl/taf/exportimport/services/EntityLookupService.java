package ca.etsmtl.taf.exportimport.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ca.etsmtl.taf.exportimport.config.CacheConfig;
import ca.etsmtl.taf.exportimport.models.Entity;
import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.models.Project;
import ca.etsmtl.taf.exportimport.models.TestCase;
import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRun;
import ca.etsmtl.taf.exportimport.models.TestSuite;
import ca.etsmtl.taf.exportimport.repositories.ProjectRepository;
import ca.etsmtl.taf.exportimport.repositories.TestResultRepository;
import ca.etsmtl.taf.exportimport.repositories.TestRunRepository;
import ca.etsmtl.taf.exportimport.repositories.TestSuiteRepository;
import ca.etsmtl.taf.exportimport.repositories.TestCaseRepository;

@Service
@Transactional(readOnly = true)
public class EntityLookupService {
    private final ProjectRepository projectRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestRunRepository testRunRepository;
    private final TestResultRepository testResultRepository;
    private final TestCaseRepository testCaseRepository;

    @Autowired
    public EntityLookupService(ProjectRepository projectRepository,
                                   TestSuiteRepository testSuiteRepository,
                                   TestRunRepository testRunRepository,
                                   TestResultRepository testResultRepository,
                                   TestCaseRepository testCaseRepository) {
        this.projectRepository = projectRepository;
        this.testSuiteRepository = testSuiteRepository;
        this.testRunRepository = testRunRepository;
        this.testResultRepository = testResultRepository;
        this.testCaseRepository = testCaseRepository;
    }

    @Cacheable(cacheNames = CacheConfig.PROJECTS_CACHE, key = "#id")
    public Project findProjectById(String id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Project with id %s not found".formatted(id)));
    }

    @Cacheable(cacheNames = CacheConfig.TEST_SUITES_CACHE, key = "#id")
    public TestSuite findTestSuiteById(String id) {
        return testSuiteRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Test suite with id %s not found".formatted(id)));
    }

    @Cacheable(cacheNames = CacheConfig.TEST_RUNS_CACHE, key = "#id")
    public TestRun findTestRunById(String id) {
        return testRunRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Test run with id %s not found".formatted(id)));
    }

    @Cacheable(cacheNames = CacheConfig.TEST_RESULTS_CACHE, key = "#id")
    public TestResult findTestResultById(String id) {
        return testResultRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Test result with id %s not found".formatted(id)));
    }

    @Cacheable(cacheNames = CacheConfig.TEST_CASES_CACHE, key = "#id")
    public TestCase findTestCaseById(String id) {
        return testCaseRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Test case with id %s not found".formatted(id)));
    }

    public Entity findById(String id, EntityType type) {
        return switch (type) {
            case PROJECT -> findProjectById(id);
            case TEST_SUITE -> findTestSuiteById(id);
            case TEST_RUN -> findTestRunById(id);
            case TEST_RESULT -> findTestResultById(id);
            case TEST_CASE -> findTestCaseById(id);
        };
    }
}
