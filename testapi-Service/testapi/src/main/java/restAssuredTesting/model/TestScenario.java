package restAssuredTesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestScenario {
    private Long id;
    private String name;
    private String description;

    // 1 scenario → many test cases
    private List<TestCase> testCases = new ArrayList<>();
}
