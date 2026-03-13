package ca.etsmtl.taf.security.jwt;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

@DisplayName("AuthEntryPointJwt — 401 response format")
class AuthEntryPointJwtTest {

    private final AuthEntryPointJwt entryPoint = new AuthEntryPointJwt();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("Produces JSON body with status=401, error, message, path")
    void commence_shouldReturnJsonUnauthorizedResponse() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/testapi/checkApi");

        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response,
                new BadCredentialsException("Full authentication is required"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertEquals("application/json", response.getContentType());

        Map<String, Object> body = objectMapper.readValue(
                response.getContentAsString(), new TypeReference<>() {});

        assertEquals(401, body.get("status"));
        assertEquals("Unauthorized", body.get("error"));
        assertEquals("Full authentication is required", body.get("message"));
        assertEquals("/api/testapi/checkApi", body.get("path"));
    }

    @Test
    @DisplayName("Different path is reflected in response body")
    void commence_shouldReflectRequestPath() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/auth/signin");

        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response,
                new BadCredentialsException("Bad credentials"));

        Map<String, Object> body = objectMapper.readValue(
                response.getContentAsString(), new TypeReference<>() {});

        assertEquals("/api/auth/signin", body.get("path"));
        assertEquals("Bad credentials", body.get("message"));
    }
}
