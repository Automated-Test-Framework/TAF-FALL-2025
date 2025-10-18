package ca.etsmtl.taf.exportimport.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.models.TestCase;
import ca.etsmtl.taf.exportimport.models.TestResult;
import ca.etsmtl.taf.exportimport.models.TestRun;
import ca.etsmtl.taf.exportimport.models.TestSuite;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class ExportDependencyResolver {
    private record EntityReference(EntityType type, String id) {
        EntityReference {
            Objects.requireNonNull(type, "type");
            Objects.requireNonNull(id, "id");
        }
    }

    private final EntityLookupService entityLookupService;

    @Autowired
    public ExportDependencyResolver(EntityLookupService entityLookupService) {
        this.entityLookupService = entityLookupService;

    }

    public Map<EntityType, List<String>> resolveDependencies(Map<EntityType, List<String>> ids) {
        if (ids == null) {
            throw new IllegalArgumentException("ids cannot be null");
        }

        Map<EntityType, LinkedHashSet<String>> result = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            result.put(type, new LinkedHashSet<>());
        }

        // Work queue for BFS
        Deque<EntityReference> q = new ArrayDeque<>();
        // Keep track of visited nodes
        Set<EntityReference> visited = new HashSet<>();

        // Prepare work queue with initial ids
        ids.forEach((type, idList) -> {
            if (idList == null) {
                throw new IllegalStateException("Id list for type " + type + " cannot be null");
            }

            for (String id : idList) {
                if (id == null || id.isBlank()) {
                    throw new IllegalStateException("Id for type " + type + " cannot be null or empty");
                }
                result.get(type).add(id);
                q.add(new EntityReference(type, id));
            }
        });

        // BFS traversal
        while (!q.isEmpty()) {
            EntityReference current = q.removeFirst();
            if (!visited.add(current)) {
                continue;
            }

            List<EntityReference> dependencies = getDependencies(current);
            for (EntityReference dep : dependencies) {
                if (dep == null) {
                    throw new IllegalStateException("Dependency reference cannot be null for type " + current.type());
                }
                String dependencyId = dep.id();
                if (dependencyId == null || dependencyId.isBlank()) {
                    throw new IllegalStateException("Id for dependency type " + dep.type() + " cannot be null or empty");
                }

                if (result.get(dep.type()).add(dependencyId)) {
                    q.addLast(dep);
                }
            }
        }

        // Convert to output format
        Map<EntityType, List<String>> finalResult = new EnumMap<>(EntityType.class);
        result.forEach((type, set) -> finalResult.put(type, List.copyOf(set)));
        return finalResult;
    }

    private List<EntityReference> getDependencies(EntityReference entity) {
        switch(entity.type()) {
            case PROJECT -> {
                return getProjectDependencies(entity.id());
            }
            case TEST_SUITE -> {
                return getTestSuiteDependencies(entity.id());
            }
            case TEST_RUN -> {
                return getTestRunDependencies(entity.id());
            }
            case TEST_RESULT -> {
                return getTestResultDependencies(entity.id());
            }
            case TEST_CASE -> {
                return getTestCaseDependencies(entity.id());
            }
            default -> throw new IllegalArgumentException("Unknown entity type: " + entity.type());
        }
    }

    private List<EntityReference> getTestCaseDependencies(String id) {
        TestCase testCase = entityLookupService.findTestCaseById(id);
        if (testCase == null) {
            throw new IllegalStateException("TestCase with id '" + id + "' not found while resolving dependencies.");
        }
        String testSuiteId = testCase.getTestSuiteId();
        if (testSuiteId == null) {
            throw new IllegalStateException("TestCase with id '" + id + "' is missing an associated TestSuite while resolving dependencies.");
        }
        return List.of(new EntityReference(EntityType.TEST_SUITE, testSuiteId));
    }

    private List<EntityReference> getTestResultDependencies(String id) {
        TestResult testResult = entityLookupService.findTestResultById(id);
        if (testResult == null) {
            throw new IllegalStateException("TestResult with id '" + id + "' not found while resolving dependencies.");
        }
        String testCaseId = testResult.getTestCaseId();
        if (testCaseId == null) {
            throw new IllegalStateException("TestResult with id '" + id + "' is missing an associated TestCase while resolving dependencies.");
        }
        String testRunId = testResult.getTestRunId();
        if (testRunId == null) {
            throw new IllegalStateException("TestResult with id '" + id + "' is missing an associated TestRun while resolving dependencies.");
        }
        return List.of(
            new EntityReference(EntityType.TEST_CASE, testCaseId),
            new EntityReference(EntityType.TEST_RUN, testRunId)
        );
    }

    private List<EntityReference> getTestRunDependencies(String id) {
        TestRun testRun = entityLookupService.findTestRunById(id);
        if (testRun == null) {
            throw new IllegalStateException("TestRun with id '" + id + "' not found while resolving dependencies.");
        }
        String testSuiteId = testRun.getTestSuiteId();
        if (testSuiteId == null) {
            throw new IllegalStateException("TestRun with id '" + id + "' is missing an associated TestSuite while resolving dependencies.");
        }
        List<String> testCaseIds = testRun.getTestCaseIds();
        List<EntityReference> cases = testCaseIds == null
            ? List.of()
            : testCaseIds.stream()
            .map(tcId -> {
                if (tcId == null || tcId.isBlank()) {
                    throw new IllegalStateException("TestRun with id '" + id + "' contains an invalid TestCase identifier.");
                }
                return new EntityReference(EntityType.TEST_CASE, tcId);
            })
            .toList();
        LinkedList<EntityReference> res = new LinkedList<>(cases);
        res.addFirst(new EntityReference(EntityType.TEST_SUITE, testSuiteId));
        return res;
    }

    private List<EntityReference> getTestSuiteDependencies(String id) {
        TestSuite testSuite = entityLookupService.findTestSuiteById(id);
        if (testSuite == null) {
            throw new IllegalStateException("TestSuite with id '" + id + "' not found while resolving dependencies.");
        }
        String projectId = testSuite.getProjectId();
        if (projectId == null) {
            throw new IllegalStateException("TestSuite with id '" + id + "' is missing an associated Project while resolving dependencies.");
        }
        return List.of(new EntityReference(EntityType.PROJECT, projectId));
    }

    private List<EntityReference> getProjectDependencies(String id) {
        // Projects have no dependencies
        return List.of();
    }
}
