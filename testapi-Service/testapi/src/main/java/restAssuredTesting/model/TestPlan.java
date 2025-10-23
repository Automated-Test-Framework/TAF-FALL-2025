package restAssuredTesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestPlan {
    private Long id;
    private String name;
    private String description;

    private List<TestScenario> scenarios = new ArrayList<>();
}
