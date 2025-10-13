package ca.etsmtl.taf.exportimport.utils.exporters;

import java.util.Map;
import java.util.List;

import ca.etsmtl.taf.exportimport.models.Entity;
import ca.etsmtl.taf.exportimport.models.EntityType;

public interface Exporter {
    void exportTo(Map<EntityType, List<Entity>> entities) throws Exception;
}
