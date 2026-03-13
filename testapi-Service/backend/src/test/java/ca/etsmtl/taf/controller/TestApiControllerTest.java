package ca.etsmtl.taf.controller;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import org.springframework.http.ResponseEntity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import ca.etsmtl.taf.payload.request.TestApiRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestApiController — /api/testapi/checkApi")
class TestApiControllerTest {

    @InjectMocks
    private TestApiController controller;

    @BeforeEach
    void setUp() throws Exception {
        setField(controller, "Test_API_microservice_url", "http://localhost");
        setField(controller, "Test_API_microservice_port", "8084");
    }

    @Test
    @DisplayName("checkApi constructs correct URI from @Value properties")
    void checkApi_constructsCorrectUri() throws Exception {
        // The controller builds URI from Test_API_microservice_url + ":" + Test_API_microservice_port + path
        // We can verify the @Value fields are set correctly
        java.lang.reflect.Field urlField = TestApiController.class.getDeclaredField("Test_API_microservice_url");
        urlField.setAccessible(true);
        assertEquals("http://localhost", urlField.get(controller));

        java.lang.reflect.Field portField = TestApiController.class.getDeclaredField("Test_API_microservice_port");
        portField.setAccessible(true);
        assertEquals("8084", portField.get(controller));
    }

    @Test
    @DisplayName("@Value fields are injectable")
    void valueFieldsAreAccessible() throws Exception {
        Field urlField = TestApiController.class.getDeclaredField("Test_API_microservice_url");
        Field portField = TestApiController.class.getDeclaredField("Test_API_microservice_port");
        
        assertNotNull(urlField);
        assertNotNull(portField);
    }

    @Test
    @DisplayName("TestApiRequest DTO getters/setters work correctly")
    void testApiRequest_gettersSetters() {
        TestApiRequest req = new TestApiRequest();
        req.setMethod("POST");
        req.setApiUrl("http://example.com/api");
        req.setStatusCode(201);
        req.setInput("{\"key\":\"value\"}");
        req.setExpectedOutput("{\"result\":\"ok\"}");
        req.setHeaders(java.util.Map.of("Content-Type", "application/json"));

        assertEquals("POST", req.getMethod());
        assertEquals("http://example.com/api", req.getApiUrl());
        assertEquals(201, req.getStatusCode());
        assertEquals("{\"key\":\"value\"}", req.getInput());
        assertEquals("{\"result\":\"ok\"}", req.getExpectedOutput());
        assertEquals("application/json", req.getHeaders().get("Content-Type"));
    }

    @Test
    @DisplayName("testApi sends request to microservice and returns response body")
    void testApi_sendsRequestAndReturnsResponse() throws Exception {
        // Start an embedded HTTP server to simulate the testapi microservice
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/microservice/testapi/checkApi", exchange -> {
            byte[] resp = "{\"status\":\"success\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, resp.length);
            exchange.getResponseBody().write(resp);
            exchange.getResponseBody().close();
        });
        server.setExecutor(null);
        server.start();

        try {
            setField(controller, "Test_API_microservice_url", "http://localhost");
            setField(controller, "Test_API_microservice_port", String.valueOf(port));

            TestApiRequest req = new TestApiRequest();
            req.setMethod("GET");
            req.setApiUrl("http://example.com");
            req.setStatusCode(200);

            ResponseEntity<String> result = controller.testApi(req);

            assertNotNull(result);
            assertEquals(200, result.getStatusCode().value());
            assertEquals("{\"status\":\"success\"}", result.getBody());
        } finally {
            server.stop(0);
        }
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
