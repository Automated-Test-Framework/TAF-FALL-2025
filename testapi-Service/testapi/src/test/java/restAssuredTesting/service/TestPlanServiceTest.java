package restAssuredTesting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restAssuredTesting.model.TestPlan;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TestPlanServiceTest {

    private TestPlanService service;

    @BeforeEach
    void setup() {
        service = new TestPlanService();
    }

    @Test
    void save_ShouldAssignId() {
        TestPlan plan = new TestPlan();
        service.save(plan);
        assertNotNull(plan.getId());
    }

    @Test
    void update_ShouldReplaceExisting() {
        TestPlan p = service.save(new TestPlan());
        p.setName("Updated");
        service.update(p.getId(), p);
        assertEquals("Updated", service.findById(p.getId()).get().getName());
    }

    @Test
    void delete_ShouldRemovePlan() {
        TestPlan plan = service.save(new TestPlan());
        service.deleteById(plan.getId());
        assertEquals(Optional.empty(), service.findById(plan.getId()));
    }
}
