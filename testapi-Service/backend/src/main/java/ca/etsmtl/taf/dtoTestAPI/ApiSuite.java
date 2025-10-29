package ca.etsmtl.taf.dtoTestAPI;

import java.util.List;
import java.util.Map;

public class ApiSuite {
    private String name;
    private String baseUrl;
    private Auth auth;
    private Map<String,String> variables;
    private List<ApiTest> tests;

    public ApiSuite() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Auth getAuth() { return auth; }
    public void setAuth(Auth auth) { this.auth = auth; }

    public Map<String, String> getVariables() { return variables; }
    public void setVariables(Map<String, String> variables) { this.variables = variables; }

    public List<ApiTest> getTests() { return tests; }
    public void setTests(List<ApiTest> tests) { this.tests = tests; }
}
