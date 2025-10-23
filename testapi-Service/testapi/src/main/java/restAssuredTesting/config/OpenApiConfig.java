package restAssuredTesting.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🧪 RestAssured Testing API")
                        .description("This API allows you to manage and execute Test Plans, " +
                                "Test Scenarios, and Test Cases dynamically.\n\n" +
                                "- `/api/testplans` → Manage and run full plans\n" +
                                "- `/api/testscenarios` → Manage individual scenarios\n" +
                                "- `/api/testcases` → Manage test cases\n\n" +
                                "Use **POST /api/testplans/{id}/run** to execute a plan with RestAssured.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TAF / TestAPI Service")
                                .email("qa-team@example.com")));
    }
}
