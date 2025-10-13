package ca.etsmtl.taf.exportimport.dtos.testrail.inbound;

import java.util.List;

public class TestSuiteData {

    private String name;
    private String id;
    private Integer testrailId;
    private Integer sectionId;
    private ProjectData projectData;

    public TestSuiteData(String name, String id, Integer testrailId) {
        this.name = name;
        this.id = id;
        this.testrailId = testrailId;
    }

    public Integer getSectionId() {
        return sectionId;
    }

    public void setSectionId(Integer sectionId) {
        this.sectionId = sectionId;
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

    public ProjectData getProjectData() {
        return projectData;
    }

    public void setProjectData(ProjectData projectData) {
        this.projectData = projectData;
    }
}
