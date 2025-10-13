package ca.etsmtl.taf.exportimport.dtos.testrail.inbound;

public class TestResultData {
    private String comment;
    private String id;
    private Integer testrailId;

    public TestResultData(String comment, Integer testrailId) {
        this.comment = comment;
        this.testrailId = testrailId;
    }

    public TestResultData(String comment, String id, Integer testrailId) {
        this.comment = comment;
        this.id = id;
        this.testrailId = testrailId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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
