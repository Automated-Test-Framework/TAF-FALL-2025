package ca.etsmtl.taf.exportimport.dtos;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.etsmtl.taf.exportimport.models.EntityType;
import jakarta.validation.ValidationException;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

@Getter
@Setter
public class ExportRequest {

    private String type;

    @JsonIgnore
    private final Map<EntityType, List<String>> ids = new HashMap<>();

    @JsonAnySetter
    public void add(String key, Object value) {
        try {
            EntityType type = EntityType.valueOf(key);
            // Cast sécurisé
            ids.put(type, new ArrayList<>((List<String>) value));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Non authorized key : " + key);
        }
    }
}
