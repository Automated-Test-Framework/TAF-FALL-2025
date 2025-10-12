package ca.etsmtl.taf.exportimport.dtos.testrail.outbound;

import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRunStatus;

import java.util.HashMap;
import java.util.Map;

public class TestResultDTO {
    private String id;
    private Integer testRunId;
    private Integer testCaseId;
    private Integer statusId;
    private String comment;

    private static final Map<TestRunStatus, Integer> TESTRAIL_STATUS_MAP = Map.of(
            TestRunStatus.PASSED, 1,
            TestRunStatus.BLOCKED, 2,
            TestRunStatus.UNTESTED, 3,
            TestRunStatus.RETEST, 4,
            TestRunStatus.FAILED, 5
    );

    public TestResultDTO(TestResult testResult, Integer testRunId, Integer testCaseId) {
        this.testRunId = testRunId;
        this.testCaseId = testCaseId;
        this.statusId = mapStatusToTestRail(testResult.getStatus());
        this.comment = testResult.get_id();
    }

    private Integer mapStatusToTestRail(TestRunStatus status) {
        // par défaut: UNTESTED
        // Testrail va lancer une erreur si il reçoit un UNTESTED
        return TESTRAIL_STATUS_MAP.getOrDefault(status, 3);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTestRunId() {
        return testRunId;
    }

    public void setTestRunId(Integer testRunId) {
        this.testRunId = testRunId;
    }

    public Integer getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(Integer testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer status_id) {
        this.statusId = status_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("case_id", this.testCaseId);
        data.put("status_id", this.statusId);
        data.put("comment", this.comment);
        return data;
    }
}
