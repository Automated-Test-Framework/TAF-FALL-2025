package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.models.Entity;
import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.models.Project;
import ca.etsmtl.taf.exportimport.models.TestCase;
import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRun;
import ca.etsmtl.taf.exportimport.models.TestSuite;

public interface EntityLookupService {
    Project findProjectById(String projectId);
    TestSuite findTestSuiteById(String testSuiteId);
    TestCase findTestCaseById(String testCaseId);
    TestRun findTestRunById(String testRunId);
    TestResult findTestResultById(String testResultId);
    Entity findById(String id, EntityType type);
}