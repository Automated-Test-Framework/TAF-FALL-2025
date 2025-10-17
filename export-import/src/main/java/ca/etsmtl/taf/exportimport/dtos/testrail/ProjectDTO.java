package ca.etsmtl.taf.exportimport.dtos.testrail;

import ca.etsmtl.taf.exportimport.models.Project;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {

    private String id;
    private String name;
    private String announcement;
    private boolean showAnnouncement;

    public ProjectDTO(Project project) {
        this.id = project.get_id();
        this.name = project.getName();
        this.announcement = project.getDescription();
        this.showAnnouncement = project.getDescription() != null;
    }

    /**
     * Convertit ce DTO en un format JSON-compatible pour l’API TestRail.
     */
    public Map<String, Object> toJson() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", this.name);
        data.put("announcement", this.announcement);
        data.put("show_announcement", this.showAnnouncement);
        return data;
    }
}
