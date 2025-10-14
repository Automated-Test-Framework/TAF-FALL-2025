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
import java.util.stream.Collectors;

@Service
public class ExportDependencyResolver {
    private static record EntityReference(EntityType type, String id) {
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
        // TODO: Have our own exceptions
        if (ids == null) {
            throw new IllegalArgumentException("ids cannot be null");
        }

        Map<EntityType, LinkedHashSet<String>> result = new EnumMap<>(EntityType.class);
        for (EntityType type : EntityType.values()) {
            result.put(type, new LinkedHashSet<>());
        }

        // Work queue for BFS
        Deque<EntityReference> q = new ArrayDeque<>();
        // Keep track of visited nodes to avoid cycles
        Set<EntityReference> visited = new HashSet<>();

        // Prepare work queue with initial ids
        ids.forEach((type, idList) -> {
            if(idList == null) {
                return;
            }

            for (String id : idList) {
                if (id == null) {
                    continue;
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
                if (dep == null || dep.id() == null) {
                    continue;
                }

                if (result.get(dep.type()).add(dep.id())) {
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
            // TODO: Exception
            return List.of();
        }
        return List.of(new EntityReference(EntityType.TEST_SUITE, testCase.getTestSuiteId()));
    }

    private List<EntityReference> getTestResultDependencies(String id) {
        TestResult testResult = entityLookupService.findTestResultById(id);
        if (testResult == null) {
            // TODO: Exception
            return List.of();
        }
        return List.of(
            new EntityReference(EntityType.TEST_CASE, testResult.getTestCaseId()),
            new EntityReference(EntityType.TEST_RUN, testResult.getTestRunId())
        );
    }

    private List<EntityReference> getTestRunDependencies(String id) {
        TestRun testRun = entityLookupService.findTestRunById(id);
        if (testRun == null) {
            // TODO: Exception
            return List.of();
        }
        List<EntityReference> cases = testRun.getTestCaseIds().stream()
            .filter(Objects::nonNull)
            .map(tcId -> new EntityReference(EntityType.TEST_CASE, tcId))
            .collect(Collectors.toList());
        LinkedList<EntityReference> res = new LinkedList<>(cases);
        res.addFirst(new EntityReference(EntityType.TEST_SUITE, testRun.getTestSuiteId()));
        return res;
    }

    private List<EntityReference> getTestSuiteDependencies(String id) {
        TestSuite testSuite = entityLookupService.findTestSuiteById(id);
        if (testSuite == null) {
            // TODO: Exception
            return List.of();
        }
        return List.of(new EntityReference(EntityType.PROJECT, testSuite.getProjectId()));
    }

    private List<EntityReference> getProjectDependencies(String id) {
        // Projects have no dependencies
        return List.of();
    }
}
