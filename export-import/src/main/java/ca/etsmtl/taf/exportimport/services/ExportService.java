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
        Exporter exporter = exporters.get(type);
        if (exporter == null) {
            String message = String.format("Unsupported exporter type: %s", type);
            logger.warn(message);
            throw new Exception(message);
        }

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

        try {
            exporter.exportTo(entitiesMap);
        } catch (Exception e) {
            throw new Exception("An error occured during the exportation: " + e.getMessage());
        }


        return getExportConfirmationMessage(entitiesMap);
    }

    private static String getExportConfirmationMessage(Map<EntityType, List<Entity>> entitiesMap) {
        int nbProjects = entitiesMap.getOrDefault(EntityType.PROJECT, List.of()).size();
        int nbSuites = entitiesMap.getOrDefault(EntityType.TEST_SUITE, List.of()).size();
        int nbCases = entitiesMap.getOrDefault(EntityType.TEST_CASE, List.of()).size();
        int nbRuns = entitiesMap.getOrDefault(EntityType.TEST_RUN, List.of()).size();
        int nbResults = entitiesMap.getOrDefault(EntityType.TEST_RESULT, List.of()).size();

        StringBuilder messageBuilder = new StringBuilder("Successfully exported");
        if (nbProjects > 0) messageBuilder.append(" ").append(nbProjects).append(" project(s)");
        if (nbSuites > 0) messageBuilder.append(nbProjects > 0 ? "," : "").append(" ").append(nbSuites).append(" suite(s)");
        if (nbCases > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0) ? "," : "").append(" ").append(nbCases).append(" case(s)");
        if (nbRuns > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0 || nbCases > 0) ? "," : "").append(" ").append(nbRuns).append(" run(s)");
        if (nbResults > 0) messageBuilder.append((nbProjects > 0 || nbSuites > 0 || nbCases > 0 || nbRuns > 0) ? "," : "").append(" ").append(nbResults).append(" result(s)");

        if ("Successfully exported".contentEquals(messageBuilder)) {
            return "Nothing was exported";
        }

        return messageBuilder.toString();
    }
}
