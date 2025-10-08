package ca.etsmtl.taf.exportimport.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
public class EntityLookupServiceImpl implements EntityLookupService {
    private final ProjectRepository projectRepository;
    private final TestSuiteRepository testSuiteRepository;
    private final TestRunRepository testRunRepository;
    private final TestResultRepository testResultRepository;
    private final TestCaseRepository testCaseRepository;

    @Autowired
    public EntityLookupServiceImpl(ProjectRepository projectRepository,
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

    @Override
    public Project findProjectById(String id) {
        return projectRepository.findById(id).orElse(null);
    }

    @Override
    public TestSuite findTestSuiteById(String id) {
        return testSuiteRepository.findById(id).orElse(null);
    }

    @Override
    public TestRun findTestRunById(String id) {
        return testRunRepository.findById(id).orElse(null); 
    }

    @Override
    public TestResult findTestResultById(String id) {
        return testResultRepository.findById(id).orElse(null);
    }

    @Override
    public TestCase findTestCaseById(String id) {
        return testCaseRepository.findById(id).orElse(null);
    }

    @Override
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