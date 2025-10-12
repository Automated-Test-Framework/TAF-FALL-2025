package ca.etsmtl.taf.exportimport.dtos.testrail.outbound;

import java.util.HashMap;
import java.util.Map;

public class SectionDTO {

    public static final String ROOT_SECTION_NAME = "Root Section";

    private Integer suite_id;
    private String name;

    public SectionDTO() {}

    public SectionDTO(Integer suite_id) {
        this.suite_id = suite_id;
        this.name = ROOT_SECTION_NAME;
    }

    public Integer getSuite_id() {
        return suite_id;
    }

    public void setSuite_id(Integer suite_id) {
        this.suite_id = suite_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Convertit ce DTO en un format JSON-compatible pour l’API TestRail.
     */
    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("suite_id", this.suite_id);
        data.put("name", this.name);
        return data;
    }

}
