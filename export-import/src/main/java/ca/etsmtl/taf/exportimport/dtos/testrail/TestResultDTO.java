package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRunStatus;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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

    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("case_id", this.testCaseId);
        data.put("status_id", this.statusId);
        data.put("comment", this.comment);
        return data;
    }
}
