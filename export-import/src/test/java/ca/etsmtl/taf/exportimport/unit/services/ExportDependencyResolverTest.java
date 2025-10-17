package ca.etsmtl.taf.exportimport.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.models.TestCase;
import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRun;
import ca.etsmtl.taf.exportimport.models.TestRunStatus;
import ca.etsmtl.taf.exportimport.models.TestSuite;
import ca.etsmtl.taf.exportimport.services.EntityLookupService;
import ca.etsmtl.taf.exportimport.services.ExportDependencyResolver;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportDependencyResolverTest {

    @Mock
    private EntityLookupService entityLookupService;

    private ExportDependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ExportDependencyResolver(entityLookupService);
    }

    @Test
    void givenNullIds_whenResolvingDependencies_thenThrowIllegalArgumentException() {
        assertThatThrownBy(() -> resolver.resolveDependencies(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("ids cannot be null");
    }

    @Test
    void givenResultWithRunAndCases_whenResolvingDependencies_thenCollectsEntireGraph() {
        // Arrange
        when(entityLookupService.findTestResultById("result-1"))
            .thenReturn(TestResult.builder()
                ._id("result-1")
                .testCaseId("case-2")
                .testRunId("run-1")
                .status(TestRunStatus.PASSED)
                .build());
        when(entityLookupService.findTestCaseById("case-1"))
            .thenReturn(TestCase.builder()
                ._id("case-1")
                .testSuiteId("suite-1")
                .name("TestCase1")
                .build());
        when(entityLookupService.findTestCaseById("case-2"))
            .thenReturn(TestCase.builder()
                ._id("case-2")
                .testSuiteId("suite-1")
                .name("TestCase2")
                .build());
        when(entityLookupService.findTestCaseById("case-3"))
            .thenReturn(TestCase.builder()
                ._id("case-3")
                .testSuiteId("suite-1")
                .name("TestCase3")
                .build());
        when(entityLookupService.findTestRunById("run-1"))
            .thenReturn(TestRun.builder()
                ._id("run-1")
                .testSuiteId("suite-1")
                .name("TestRun1")
                .testCaseIds(List.of("case-1", "case-2", "case-3"))
                .build());
        when(entityLookupService.findTestSuiteById("suite-1"))
            .thenReturn(TestSuite.builder()
                ._id("suite-1")
                .projectId("project-1")
                .name("TestSuite1")
                .description("TestSuite1 description")
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RESULT, List.of("result-1")
        );

        // Act
        Map<EntityType, List<String>> resolved = resolver.resolveDependencies(input);

        // Assert
        assertThat(resolved.get(EntityType.PROJECT)).containsExactlyInAnyOrder("project-1");
        assertThat(resolved.get(EntityType.TEST_SUITE)).containsExactlyInAnyOrder("suite-1");
        assertThat(resolved.get(EntityType.TEST_CASE)).containsExactlyInAnyOrder("case-2", "case-1", "case-3");
        assertThat(resolved.get(EntityType.TEST_RUN)).containsExactlyInAnyOrder("run-1");
        assertThat(resolved.get(EntityType.TEST_RESULT)).containsExactlyInAnyOrder("result-1");
    }

    @Test
    void givenInputWithNullIdentifiers_whenResolvingDependencies_thenThrowIllegalStateException() {
        Map<EntityType, List<String>> input = new EnumMap<>(EntityType.class);
        input.put(EntityType.TEST_CASE, Arrays.asList(null, "case-1"));
        input.put(EntityType.TEST_RUN, null);

        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TEST_CASE")
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    void givenInputWithEmptyIdentifier_whenResolvingDependencies_thenThrowIllegalStateException() {
        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_CASE, List.of("")
        );

        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TEST_CASE")
            .hasMessageContaining("cannot be null or empty");
    }

    @Test
    void givenInputWithNullIdList_whenResolvingDependencies_thenThrowIllegalStateException() {
        Map<EntityType, List<String>> input = new EnumMap<>(EntityType.class);
        input.put(EntityType.TEST_RUN, null);

        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Id list for type TEST_RUN cannot be null");
    }

    @Test
    void givenInputWithDuplicateIds_whenResolvingDependencies_thenProcessesEachOnce() {
        // Arrange
        when(entityLookupService.findTestCaseById("case-1"))
            .thenReturn(TestCase.builder()
                ._id("case-1")
                .testSuiteId("suite-1")
                .name("TestCase1")
                .build());
        when(entityLookupService.findTestSuiteById("suite-1"))
            .thenReturn(TestSuite.builder()
                ._id("suite-1")
                .projectId("project-1")
                .name("TestSuite1")
                .description("TestSuite1 description")
                .build());

        Map<EntityType, List<String>> input = new EnumMap<>(EntityType.class);
        input.put(EntityType.TEST_CASE, Arrays.asList("case-1", "case-1"));

        // Act
        Map<EntityType, List<String>> resolved = resolver.resolveDependencies(input);

        // Assert
        assertThat(resolved.get(EntityType.TEST_CASE)).containsExactlyInAnyOrder("case-1");
        assertThat(resolved.get(EntityType.TEST_SUITE)).containsExactlyInAnyOrder("suite-1");
        assertThat(resolved.get(EntityType.PROJECT)).containsExactlyInAnyOrder("project-1");
        assertThat(resolved.get(EntityType.TEST_RUN)).isEmpty();
        assertThat(resolved.get(EntityType.TEST_RESULT)).isEmpty();

        verify(entityLookupService).findTestCaseById("case-1");
        verify(entityLookupService).findTestSuiteById("suite-1");
        verifyNoMoreInteractions(entityLookupService);
    }

    @Test
    void givenEntitiesSharingParents_whenResolvingDependencies_thenParentAppearsOnce() {
        // Arrange
        when(entityLookupService.findTestCaseById("case-1"))
            .thenReturn(TestCase.builder()
                ._id("case-1")
                .testSuiteId("suite-1")
                .name("TestCase1")
                .build());
        when(entityLookupService.findTestCaseById("case-2"))
            .thenReturn(TestCase.builder()
                ._id("case-2")
                .testSuiteId("suite-1")
                .name("TestCase2")
                .build());
        when(entityLookupService.findTestSuiteById("suite-1"))
            .thenReturn(TestSuite.builder()
                ._id("suite-1")
                .projectId("project-1")
                .name("TestSuite1")
                .description("TestSuite1 description")
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_CASE, List.of("case-1", "case-2")
        );

        // Act
        Map<EntityType, List<String>> resolved = resolver.resolveDependencies(input);

        // Assert
        assertThat(resolved.get(EntityType.TEST_CASE)).containsExactlyInAnyOrder("case-1", "case-2");
        assertThat(resolved.get(EntityType.TEST_SUITE)).containsExactlyInAnyOrder("suite-1");
        assertThat(resolved.get(EntityType.PROJECT)).containsExactlyInAnyOrder("project-1");
        assertThat(resolved.get(EntityType.TEST_RUN)).isEmpty();
        assertThat(resolved.get(EntityType.TEST_RESULT)).isEmpty();

        verify(entityLookupService).findTestCaseById("case-1");
        verify(entityLookupService).findTestCaseById("case-2");
        verify(entityLookupService).findTestSuiteById("suite-1");
        verifyNoMoreInteractions(entityLookupService);
    }

    @Test
    void givenMissingTestCase_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_CASE, List.of("missing-case")
        );
        when(entityLookupService.findTestCaseById("missing-case")).thenReturn(null);

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestCase")
            .hasMessageContaining("missing-case");
    }

    @Test
    void givenTestCaseWithMissingTestSuite_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        TestCase testCase = TestCase.builder()
            ._id("case-1")
            .testSuiteId("missing-suite")
            .name("TestCase1")
            .build();
        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_CASE, List.of("case-1")
        );
        when(entityLookupService.findTestCaseById("case-1")).thenReturn(testCase);
        when(entityLookupService.findTestSuiteById("missing-suite")).thenReturn(null);

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestSuite")
            .hasMessageContaining("missing-suite");
    }

    @Test
    void givenMissingTestRun_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RUN, List.of("missing-run")
        );
        when(entityLookupService.findTestRunById("missing-run")).thenReturn(null);

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestRun")
            .hasMessageContaining("missing-run");
    }

    @Test
    void givenMissingTestResult_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RESULT, List.of("missing-result")
        );
        when(entityLookupService.findTestResultById("missing-result")).thenReturn(null);

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestResult")
            .hasMessageContaining("missing-result");
    }

    @Test
    void givenTestRunWithNullTestCaseIds_whenResolvingDependencies_thenSucceeds() {
        // Arrange
        when(entityLookupService.findTestRunById("run-1"))
            .thenReturn(TestRun.builder()
                ._id("run-1")
                .testSuiteId("suite-1")
                .name("TestRun1")
                .testCaseIds(null)
                .build());
        when(entityLookupService.findTestSuiteById("suite-1"))
            .thenReturn(TestSuite.builder()
                ._id("suite-1")
                .projectId("project-1")
                .name("TestSuite1")
                .description("TestSuite1 description")
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RUN, List.of("run-1")
        );

        // Act
        Map<EntityType, List<String>> resolved = resolver.resolveDependencies(input);

        // Assert
        assertThat(resolved.get(EntityType.TEST_RUN)).containsExactlyInAnyOrder("run-1");
        assertThat(resolved.get(EntityType.TEST_SUITE)).containsExactlyInAnyOrder("suite-1");
        assertThat(resolved.get(EntityType.PROJECT)).containsExactlyInAnyOrder("project-1");
        assertThat(resolved.get(EntityType.TEST_CASE)).isEmpty();
        assertThat(resolved.get(EntityType.TEST_RESULT)).isEmpty();
    }

    @Test
    void givenTestRunWithInvalidTestCaseId_whenResolvingDependencies_thenThrowIllegalStateException() {
        when(entityLookupService.findTestRunById("run-1"))
            .thenReturn(TestRun.builder()
                ._id("run-1")
                .testSuiteId("suite-1")
                .name("TestRun1")
                .testCaseIds(List.of("case-1", ""))
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RUN, List.of("run-1")
        );

        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("TestRun with id 'run-1' contains an invalid TestCase identifier.");
    }

    @Test
    void givenTestCaseWithoutTestSuiteId_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        when(entityLookupService.findTestCaseById("case-1"))
            .thenReturn(TestCase.builder()
                ._id("case-1")
                .testSuiteId(null)
                .name("TestCase1")
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_CASE, List.of("case-1")
        );

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestCase")
            .hasMessageContaining("case-1")
            .hasMessageContaining("TestSuite");
    }

    @Test
    void givenTestResultWithoutTestCaseId_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        when(entityLookupService.findTestResultById("result-1"))
            .thenReturn(TestResult.builder()
                ._id("result-1")
                .testRunId("run-1")
                .testCaseId(null)
                .status(TestRunStatus.PASSED)
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RESULT, List.of("result-1")
        );

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestResult")
            .hasMessageContaining("result-1")
            .hasMessageContaining("TestCase");
    }

    @Test
    void givenTestResultWithoutTestRunId_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        when(entityLookupService.findTestResultById("result-1"))
            .thenReturn(TestResult.builder()
                ._id("result-1")
                .testRunId(null)
                .testCaseId("case-1")
                .status(TestRunStatus.PASSED)
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RESULT, List.of("result-1")
        );

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestResult")
            .hasMessageContaining("result-1")
            .hasMessageContaining("TestRun");
    }

    @Test
    void givenTestRunWithoutTestSuiteId_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        when(entityLookupService.findTestRunById("run-1"))
            .thenReturn(TestRun.builder()
                ._id("run-1")
                .testSuiteId(null)
                .name("TestRun1")
                .testCaseIds(List.of("case-1"))
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_RUN, List.of("run-1")
        );

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestRun")
            .hasMessageContaining("run-1")
            .hasMessageContaining("TestSuite");
    }

    @Test
    void givenTestSuiteWithoutProjectId_whenResolvingDependencies_thenThrowIllegalStateException() {
        // Arrange
        when(entityLookupService.findTestSuiteById("suite-1"))
            .thenReturn(TestSuite.builder()
                ._id("suite-1")
                .projectId(null)
                .name("TestSuite1")
                .description("TestSuite1 description")
                .build());

        Map<EntityType, List<String>> input = Map.of(
            EntityType.TEST_SUITE, List.of("suite-1")
        );

        // Act / Assert
        assertThatThrownBy(() -> resolver.resolveDependencies(input))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("TestSuite")
            .hasMessageContaining("suite-1")
            .hasMessageContaining("Project");
    }
}
