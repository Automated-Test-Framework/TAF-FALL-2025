package restAssuredTesting.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import restAssuredTesting.model.TestCase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestCaseServiceTest {

    private TestCaseService service;

    @BeforeEach
    void setup() {
        service = new TestCaseService();
    }

    @Test
    void save_ShouldAssignId() {
        TestCase c = service.save(new TestCase());
        assertNotNull(c.getId());
    }

    @Test
    void update_ShouldReplaceExisting() {
        TestCase c = service.save(new TestCase());
        c.setName("Test");
        service.update(c.getId(), c);
        assertEquals("Test", service.findById(c.getId()).get().getName());
    }
}
