package ca.etsmtl.taf.exportimport.dtos;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.ValidationException;
import java.util.*;

public class ExportRequest {

    private String type;

    @JsonIgnore
    private static final Set<String> ALLOWED_KEYS = Set.of("project", "suite", "case", "run");

    private final Map<String, List<String>> ids = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        if (!ALLOWED_KEYS.contains(key)) {
            throw new ValidationException("Non authorized key : " + key + ". Valid keys : " + ALLOWED_KEYS);
        }

        // Cast sécurisé
        ids.put(key, new ArrayList<>((List<String>) value));
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, List<String>> getIds() {
        return ids;
    }
}
