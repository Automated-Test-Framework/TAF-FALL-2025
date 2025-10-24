package restAssuredTesting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restAssuredTesting.model.TestScenario;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestScenarioServiceTest {

    private TestScenarioService service;

    @BeforeEach
    void setup() {
        service = new TestScenarioService();
    }

    @Test
    void save_ShouldAssignId() {
        TestScenario s = service.save(new TestScenario());
        assertNotNull(s.getId());
    }

    @Test
    void findById_ShouldReturnPresent() {
        TestScenario s = service.save(new TestScenario());
        assertTrue(service.findById(s.getId()).isPresent());
    }

    @Test
    void delete_ShouldRemoveScenario() {
        TestScenario s = service.save(new TestScenario());
        service.deleteById(s.getId());
        assertTrue(service.findAll().isEmpty());
    }
}
