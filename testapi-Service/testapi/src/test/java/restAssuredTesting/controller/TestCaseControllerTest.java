package restAssuredTesting.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import restAssuredTesting.model.TestCase;
import restAssuredTesting.service.TestCaseService;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestCaseControllerTest {

    @Mock private TestCaseService service;
    @InjectMocks private TestCaseController controller;

    @Test
    void getAll_ShouldReturnCases() {
        when(service.findAll()).thenReturn(List.of(new TestCase()));
        assertEquals(1, controller.getAll().size());
    }

    @Test
    void delete_ShouldCallService() {
        controller.delete(1L);
        verify(service).deleteById(1L);
    }

    @Test
    void getById_ShouldReturnOk() {
        TestCase tc = new TestCase();
        when(service.findById(1L)).thenReturn(Optional.of(tc));
        assertEquals(200, controller.getById(1L).getStatusCodeValue());
    }
}
