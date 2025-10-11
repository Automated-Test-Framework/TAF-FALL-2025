package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.Project;

import java.util.HashMap;
import java.util.Map;

public class ProjectDTO {

    private String name;
    private String announcement;
    private boolean show_announcement;

    public ProjectDTO() {

    }

    public ProjectDTO(Project project) {
        this.name = project.getName();
        this.announcement = project.getDescription();
        this.show_announcement = project.getDescription() != null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
    }

    public boolean isShow_announcement() {
        return show_announcement;
    }

    public void setShow_announcement(boolean show_announcement) {
        this.show_announcement = show_announcement;
    }

    /**
     * Convertit ce DTO en un format JSON-compatible pour l’API TestRail.
     */
    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("announcement", this.announcement);
        data.put("show_announcement", this.show_announcement);
        return data;
    }
}
