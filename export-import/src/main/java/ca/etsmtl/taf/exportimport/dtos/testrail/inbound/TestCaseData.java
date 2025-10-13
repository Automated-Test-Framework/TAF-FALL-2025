package ca.etsmtl.taf.exportimport.dtos.testrail.inbound;

public class TestCaseData {
    private String name;
    private String id;
    private Integer testrailId;

    public TestCaseData(String name, String id, Integer testrailId) {
        this.name = name;
        this.id = id;
        this.testrailId = testrailId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getTestrailId() {
        return testrailId;
    }

    public void setTestrailId(Integer testrailId) {
        this.testrailId = testrailId;
    }
}
