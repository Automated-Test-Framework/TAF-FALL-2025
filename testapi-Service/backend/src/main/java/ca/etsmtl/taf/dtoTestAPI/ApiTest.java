package ca.etsmtl.taf.dtoTestAPI;

import java.util.Map;

public class ApiTest {
    private String name;
    private RequestSpec request;
    private Expect expect;
    private Map<String,String> save;

    public ApiTest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public RequestSpec getRequest() { return request; }
    public void setRequest(RequestSpec request) { this.request = request; }

    public Expect getExpect() { return expect; }
    public void setExpect(Expect expect) { this.expect = expect; }

    public Map<String, String> getSave() { return save; }
    public void setSave(Map<String, String> save) { this.save = save; }
}
