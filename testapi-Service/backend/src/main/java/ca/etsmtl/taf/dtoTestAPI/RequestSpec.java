package ca.etsmtl.taf.dtoTestAPI;

import java.util.Map;

public class RequestSpec {
    private String method;
    private String path;
    private Map<String,String> headers;
    private String body;
    private Map<String,String> query;

    public RequestSpec() {}

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Map<String, String> getQuery() { return query; }
    public void setQuery(Map<String, String> query) { this.query = query; }
}