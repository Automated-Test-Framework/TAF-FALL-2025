package restAssuredTesting.service;

import org.springframework.stereotype.Service;
import restAssuredTesting.model.TestCase;

import java.util.*;

@Service
public class TestCaseService {
    private final Map<Long, TestCase> repo = new HashMap<>();
    private long counter = 0;

    public List<TestCase> findAll() {
        return new ArrayList<>(repo.values());
    }

    public Optional<TestCase> findById(Long id) {
        return Optional.ofNullable(repo.get(id));
    }

    public TestCase save(TestCase s) {
        if (s.getId() == null) s.setId(++counter);
        repo.put(s.getId(), s);
        return s;
    }

    public TestCase update(Long id, TestCase s) {
        s.setId(id);
        repo.put(id, s);
        return s;
    }

    public void deleteById(Long id) {
        repo.remove(id);
    }
}
