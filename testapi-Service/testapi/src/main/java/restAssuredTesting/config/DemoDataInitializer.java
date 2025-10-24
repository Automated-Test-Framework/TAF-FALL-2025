package restAssuredTesting.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import restAssuredTesting.model.TestCase;
import restAssuredTesting.model.TestPlan;
import restAssuredTesting.model.TestScenario;
import restAssuredTesting.service.TestPlanService;

import java.util.List;

@Configuration
public class DemoDataInitializer {

    @Bean
    CommandLineRunner initDemoData(TestPlanService service) {
        return args -> {

            // ---------------------------------------------------------------------
            // SCENARIO 1: AUTHENTICATION FLOW
            // ---------------------------------------------------------------------
            TestScenario authScenario = new TestScenario(null,
                    "Authentication Flow",
                    "Tests login, refresh, and logout endpoints",
                    List.of(
                            new TestCase(null, "Login - valid credentials",
                                    "Should return 200 OK and JWT token",
                                    "POST", "/auth/login",
                                    "{\"username\":\"admin\",\"password\":\"1234\"}", 200),
                            new TestCase(null, "Login - invalid credentials",
                                    "Should return 401 Unauthorized",
                                    "POST", "/auth/login",
                                    "{\"username\":\"wrong\",\"password\":\"bad\"}", 401),
                            new TestCase(null, "Login - missing body",
                                    "Should return 400 Bad Request",
                                    "POST", "/auth/login",
                                    "{}", 400),
                            new TestCase(null, "Refresh Token",
                                    "Should return 200 OK and new token",
                                    "POST", "/auth/refresh",
                                    "{\"refreshToken\":\"mockTokenValue\"}", 200),
                            new TestCase(null, "Logout",
                                    "Should revoke token successfully",
                                    "POST", "/auth/logout",
                                    null, 200)
                    ));

            // ---------------------------------------------------------------------
            // SCENARIO 2: USER MANAGEMENT
            // ---------------------------------------------------------------------
            TestScenario userScenario = new TestScenario(null,
                    "User Management",
                    "Validates CRUD operations for /api/users endpoints",
                    List.of(
                            new TestCase(null, "Create User",
                                    "Should create user successfully",
                                    "POST", "/api/users",
                                    "{\"name\":\"Alice\",\"role\":\"tester\"}", 201),
                            new TestCase(null, "Get All Users",
                                    "Should return list of users",
                                    "GET", "/api/users",
                                    null, 200),
                            new TestCase(null, "Get User by ID",
                                    "Should return user details",
                                    "GET", "/api/users/1",
                                    null, 200),
                            new TestCase(null, "Update User",
                                    "Should update existing user info",
                                    "PUT", "/api/users/1",
                                    "{\"role\":\"lead-tester\"}", 200),
                            new TestCase(null, "Delete User",
                                    "Should delete user successfully",
                                    "DELETE", "/api/users/1",
                                    null, 204)
                    ));

            // ---------------------------------------------------------------------
            // SCENARIO 3: ORDER PROCESSING
            // ---------------------------------------------------------------------
            TestScenario orderScenario = new TestScenario(null,
                    "Order Processing",
                    "Tests creation, update, retrieval, and deletion of orders",
                    List.of(
                            new TestCase(null, "Create Order",
                                    "Should create a new order",
                                    "POST", "/api/orders",
                                    "{\"productId\":101,\"quantity\":2}", 201),
                            new TestCase(null, "Get Order by ID",
                                    "Should retrieve order details",
                                    "GET", "/api/orders/1",
                                    null, 200),
                            new TestCase(null, "List All Orders",
                                    "Should list all existing orders",
                                    "GET", "/api/orders",
                                    null, 200),
                            new TestCase(null, "Update Order Quantity",
                                    "Should modify quantity field",
                                    "PUT", "/api/orders/1",
                                    "{\"quantity\":3}", 200),
                            new TestCase(null, "Delete Order",
                                    "Should delete order successfully",
                                    "DELETE", "/api/orders/1",
                                    null, 204)
                    ));

            // ---------------------------------------------------------------------
            // SCENARIO 4: ERROR & VALIDATION CASES
            // ---------------------------------------------------------------------
            TestScenario errorScenario = new TestScenario(null,
                    "Error and Validation",
                    "Negative test cases to verify error responses",
                    List.of(
                            new TestCase(null, "Invalid Endpoint",
                                    "Should return 404 Not Found",
                                    "GET", "/api/unknown-endpoint",
                                    null, 404),
                            new TestCase(null, "Invalid Method",
                                    "Should return 405 Method Not Allowed",
                                    "PUT", "/auth/login",
                                    null, 405),
                            new TestCase(null, "Unauthorized Access",
                                    "Should return 403 Forbidden for protected endpoint",
                                    "GET", "/api/admin/secret",
                                    null, 403)
                    ));

            // ---------------------------------------------------------------------
            // TEST PLAN 1: AUTH PLAN
            // ---------------------------------------------------------------------
            TestPlan authPlan = new TestPlan(null,
                    "Auth Plan",
                    "Covers all authentication-related endpoints",
                    List.of(authScenario));

            // ---------------------------------------------------------------------
            // TEST PLAN 2: USER PLAN
            // ---------------------------------------------------------------------
            TestPlan userPlan = new TestPlan(null,
                    "User Plan",
                    "Covers CRUD operations for /api/users",
                    List.of(userScenario));

            // ---------------------------------------------------------------------
            // TEST PLAN 3: ORDER PLAN
            // ---------------------------------------------------------------------
            TestPlan orderPlan = new TestPlan(null,
                    "Order Plan",
                    "Covers order lifecycle endpoints",
                    List.of(orderScenario));

            // ---------------------------------------------------------------------
            // TEST PLAN 4: FULL REGRESSION PLAN
            // ---------------------------------------------------------------------
            TestPlan regressionPlan = new TestPlan(null,
                    "Full Regression Plan",
                    "Runs all modules (Auth, Users, Orders, Errors) in one suite",
                    List.of(authScenario, userScenario, orderScenario, errorScenario));

            // ---------------------------------------------------------------------
            // 💾 SAVE ALL PLANS
            // ---------------------------------------------------------------------
            service.save(authPlan);
            service.save(userPlan);
            service.save(orderPlan);
            service.save(regressionPlan);

            System.out.println("✅ Loaded 4 demo test plans (Auth, User, Order, Regression)");
            System.out.println("👉 Try: GET /api/testplans");
            System.out.println("👉 Or: POST /api/testplans/1/run");
        };
    }
}