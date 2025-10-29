package ca.etsmtl.taf.dtoTestAPI;

import java.util.Map;

public class Expect {
    private Integer status;
    private Map<String,Object> json;
    private Map<String,String> headers;

    public Expect() {}

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Map<String, Object> getJson() { return json; }
    public void setJson(Map<String, Object> json) { this.json = json; }

    public Map<String, String> getHeaders() { return headers; }
    public void setHeaders(Map<String, String> headers) { this.headers = headers; }
}
