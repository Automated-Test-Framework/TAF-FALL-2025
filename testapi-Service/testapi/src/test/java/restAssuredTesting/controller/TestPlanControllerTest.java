package restAssuredTesting.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import restAssuredTesting.model.TestPlan;
import restAssuredTesting.model.TestPlanResult;
import restAssuredTesting.service.TestPlanService;
import restAssuredTesting.service.TestRunnerService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestPlanControllerTest {

    @Mock
    private TestPlanService planService;

    @Mock
    private TestRunnerService runnerService;

    @InjectMocks
    private TestPlanController controller;

    @Test
    void getAll_ShouldReturnList() {
        when(planService.findAll()).thenReturn(List.of(new TestPlan()));
        assertEquals(1, controller.getAll().size());
    }

    @Test
    void getById_ShouldReturnOk() {
        TestPlan plan = new TestPlan(); plan.setId(1L);
        when(planService.findById(1L)).thenReturn(Optional.of(plan));
        assertTrue(controller.getById(1L).getStatusCode().is2xxSuccessful());
    }

    @Test
    void runPlan_ShouldReturnResult() {
        TestPlan plan = new TestPlan(); plan.setId(1L);
        when(planService.findById(1L)).thenReturn(Optional.of(plan));
        when(runnerService.runTestPlan(plan)).thenReturn(new TestPlanResult());
        ResponseEntity<TestPlanResult> result = controller.runPlan(1L);
        assertEquals(200, result.getStatusCodeValue());
    }
}
