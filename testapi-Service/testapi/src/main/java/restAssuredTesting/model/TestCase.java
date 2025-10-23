package restAssuredTesting.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCase {
    private Long id;
    private String name;
    private String description;
    private String method;        // GET, POST, PUT, DELETE
    private String endpoint;
    private String body;          // JSON string
    private int expectedStatus;   // 200, 404, etc.
}
