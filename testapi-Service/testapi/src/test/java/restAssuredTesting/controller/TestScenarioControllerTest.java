package restAssuredTesting.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import restAssuredTesting.model.TestScenario;
import restAssuredTesting.service.TestScenarioService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestScenarioControllerTest {

    @Mock
    private TestScenarioService service;

    @InjectMocks
    private TestScenarioController controller;

    @Test
    void getAll_ShouldReturnList() {
        when(service.findAll()).thenReturn(List.of(new TestScenario()));
        assertEquals(1, controller.getAll().size());
    }

    @Test
    void getById_ShouldReturnNotFoundWhenMissing() {
        when(service.findById(99L)).thenReturn(Optional.empty());
        assertEquals(404, controller.getById(99L).getStatusCodeValue());
    }

    @Test
    void create_ShouldCallSave() {
        TestScenario scenario = new TestScenario();
        when(service.save(scenario)).thenReturn(scenario);
        assertNotNull(controller.create(scenario));
    }
}
