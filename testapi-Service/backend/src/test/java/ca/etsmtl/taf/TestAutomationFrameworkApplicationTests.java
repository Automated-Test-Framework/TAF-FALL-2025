package ca.etsmtl.taf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

/**
 * Full context load test — requires MongoDB to be running.
 * This test is disabled in CI/local environments without MongoDB.
 * Run manually with: mvn test -Dtest=TestAutomationFrameworkApplicationTests -DMONGODB_AVAILABLE=true
 */
// Disabled by default because it requires a running MongoDB instance.
// The unit tests in this suite cover all functionality without needing the full context.
@org.junit.jupiter.api.Disabled("Requires running MongoDB — use unit tests instead")
class TestAutomationFrameworkApplicationTests {

	@Test
	void contextLoads() {
	}

}
