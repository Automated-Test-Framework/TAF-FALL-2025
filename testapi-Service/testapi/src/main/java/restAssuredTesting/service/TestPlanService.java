package restAssuredTesting.service;

import org.springframework.stereotype.Service;
import restAssuredTesting.model.TestPlan;

import java.util.*;

@Service
public class TestPlanService {

    private final Map<Long, TestPlan> repo = new HashMap<>();
    private long counter = 0;

    public List<TestPlan> findAll() {
        return new ArrayList<>(repo.values());
    }

    public Optional<TestPlan> findById(Long id) {
        return Optional.ofNullable(repo.get(id));
    }

    public TestPlan save(TestPlan plan) {
        if (plan.getId() == null) plan.setId(++counter);
        repo.put(plan.getId(), plan);
        return plan;
    }

    public TestPlan update(Long id, TestPlan plan) {
        plan.setId(id);
        repo.put(id, plan);
        return plan;
    }

    public void deleteById(Long id) {
        repo.remove(id);
    }

    public void clear() {
        repo.clear();
    }
}
