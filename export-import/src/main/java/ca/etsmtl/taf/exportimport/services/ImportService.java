package ca.etsmtl.taf.exportimport.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ImportService {

    private static final Logger logger = LoggerFactory.getLogger(ImportService.class);

    public ImportService() {

    }

    public String importTo(String type, Map<String, List<String>> ids) throws Exception {

        int nbProjects = ids.get("project").size();
        int nbSuitests = ids.get("suite").size();
        int nbCases = ids.get("case").size();
        int nbRuns = ids.get("run").size();

        switch (type) {
            default:
                String message = String.format("Unsupported type: %s", type);
                logger.warn(message);
                throw new Exception(message);
        }

        //return String.format("Successfully imported %d projects, %d suites, %d cases and %d runs", nbProjects, nbSuitests, nbCases, nbRuns);
    }
}



