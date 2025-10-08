package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.models.EntityType;

import java.util.List;
import java.util.Map;

public interface ExportDependencyResolver {
    Map<EntityType, List<String>> resolveDependencies(Map<EntityType, List<String>> ids);
}