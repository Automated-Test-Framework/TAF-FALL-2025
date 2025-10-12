package ca.etsmtl.taf.exportimport.dtos.testrail.inbound;

import java.util.List;

public class ProjectData {

    private String name;
    private String id;
    private Integer testrailId;
    private List<TestSuiteData> testSuiteDatas;

    public ProjectData() {

    }

    public ProjectData(String name, String id, Integer testrailId) {
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

    public List<TestSuiteData> getTestSuiteDatas() {
        return testSuiteDatas;
    }

    public void setTestSuiteDatas(List<TestSuiteData> testSuiteDatas) {
        this.testSuiteDatas = testSuiteDatas;
    }

    public void addTestSuiteData(TestSuiteData testSuiteData) {
        this.testSuiteDatas.add(testSuiteData);
        testSuiteData.setProjectData(this);
    }
}
