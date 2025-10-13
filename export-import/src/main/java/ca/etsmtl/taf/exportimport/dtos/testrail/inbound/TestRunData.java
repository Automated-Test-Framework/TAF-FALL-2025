package ca.etsmtl.taf.exportimport.dtos.testrail.inbound;

public class TestRunData {
    private String name;
    private String id;
    private Integer testrailId;

    public TestRunData(String name, Integer testrailId) {
        this.name = name;
        this.testrailId = testrailId;
    }

    public TestRunData(String name, String id, Integer testrailId) {
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
