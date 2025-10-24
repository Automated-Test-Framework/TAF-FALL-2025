package restAssuredTesting.service;

import org.springframework.stereotype.Service;
import restAssuredTesting.model.TestScenario;
import java.util.*;

@Service
public class TestScenarioService {
    private final Map<Long, TestScenario> repo = new HashMap<>();
    private long counter = 0;

    public List<TestScenario> findAll() { return new ArrayList<>(repo.values()); }
    public Optional<TestScenario> findById(Long id) { return Optional.ofNullable(repo.get(id)); }
    public TestScenario save(TestScenario s) { if (s.getId() == null) s.setId(++counter); repo.put(s.getId(), s); return s; }
    public TestScenario update(Long id, TestScenario s) { s.setId(id); repo.put(id, s); return s; }
    public void deleteById(Long id) { repo.remove(id); }
}
