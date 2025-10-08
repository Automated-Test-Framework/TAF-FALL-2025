package ca.etsmtl.taf.exportimport.services;

import ca.etsmtl.taf.exportimport.models.Entity;
import ca.etsmtl.taf.exportimport.models.EntityType;
import ca.etsmtl.taf.exportimport.utils.exporters.Exporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExportService {

    private final EntityLookupService entityLookupService;
    private final Map<String, Exporter> exporters;
    private final ExportDependencyResolver exportDependencyResolver;
    private static final Logger logger = LoggerFactory.getLogger(ExportService.class);


    @Autowired
    public ExportService(EntityLookupService entityLookupService,
                         Map<String, Exporter> exporters,
                         ExportDependencyResolver exportDependencyResolver) {
        this.entityLookupService = entityLookupService;
        this.exporters = exporters;
        this.exportDependencyResolver = exportDependencyResolver;
    }

    public String exportTo(String type, Map<EntityType, List<String>> ids) throws Exception {
        Map<EntityType, List<String>> fullExportIds = exportDependencyResolver.resolveDependencies(ids);
        Map<EntityType, List<Entity>> entitiesMap =
            fullExportIds.entrySet().stream()
            .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream()
                          .map(id -> entityLookupService.findById(id, e.getKey()))
                          .collect(Collectors.toList()),
                    (a, b) -> b,
                    LinkedHashMap::new // To keep type order
            ));

        Exporter exporter = exporters.get(type);
        if (exporter == null) {
            String message = String.format("Unsupported type: %s", type);
            logger.warn(message);
            throw new Exception(message);
        }

        exporter.exportTo(entitiesMap);

        return getExportConfirmationMessage(ids);
    }

    private static String getExportConfirmationMessage(Map<EntityType, List<String>> ids) {
        int nbProjects = ids.getOrDefault(EntityType.PROJECT, List.of()).size();
        int nbSuites = ids.getOrDefault(EntityType.TEST_SUITE, List.of()).size();
        int nbCases = ids.getOrDefault(EntityType.TEST_CASE, List.of()).size();
        int nbRuns = ids.getOrDefault(EntityType.TEST_RUN, List.of()).size();

        StringBuilder messageBuilder = new StringBuilder("Successfully exported");
        if (nbProjects > 0) messageBuilder.append(" ").append(nbProjects).append(" projects");
        if (nbSuites > 0) messageBuilder.append(nbProjects > 0 ? "," : "").append(" ").append(nbSuites).append(" suites");
        if (nbCases > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0) ? "," : "").append(" ").append(nbCases).append(" cases");
        if (nbRuns > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0 || nbCases > 0) ? "," : "").append(" ").append(nbRuns).append(" runs");

        return messageBuilder.toString();
    }
}
