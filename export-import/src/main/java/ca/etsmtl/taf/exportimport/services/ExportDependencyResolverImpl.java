package ca.etsmtl.taf.exportimport.services;

import org.springframework.stereotype.Service;

import ca.etsmtl.taf.exportimport.models.EntityType;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ExportDependencyResolverImpl implements ExportDependencyResolver {
    @Override
    public Map<EntityType, List<String>> resolveDependencies(Map<EntityType, List<String>> ids) {
        // TODO: Implement dependency resolution logic here
        // Temp: just return as is
        return ids;
    }
}
