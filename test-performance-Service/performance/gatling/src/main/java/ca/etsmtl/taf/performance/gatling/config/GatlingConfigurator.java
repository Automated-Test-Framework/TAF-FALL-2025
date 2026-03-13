package ca.etsmtl.taf.performance.gatling.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

@Component
public class GatlingConfigurator implements WebMvcConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(GatlingConfigurator.class);

    private static final File SYSTEM_TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));
    private static final File GATLING_RESULTS_FOLDER = new File(SYSTEM_TEMP_FOLDER, "gatling");

    private volatile String latestReportPath; // Cache du chemin du dernier rapport

    public static String getGatlingResultsFolder() {
        return GATLING_RESULTS_FOLDER.getAbsolutePath();
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        createGatlingResultsFolder();
        updateLatestReportPath();
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = "file:" + GATLING_RESULTS_FOLDER.getAbsolutePath() + "/";
        registry.addResourceHandler(
                "/api/performance/reports-gatling/**", 
                "/team3/api/performance/reports-gatling/**",
                "/api/reports-gatling/**",
                "/team3/api/reports-gatling/**",
                "/reports-gatling/**"
            )
               .addResourceLocations(location)
               .setCachePeriod(3600)
               .resourceChain(true)
               .addResolver(new PathResourceResolver());
        logger.info("Gatling resource handler configured for location: {}", location);
    }

    private void createGatlingResultsFolder() {
        if (!GATLING_RESULTS_FOLDER.exists()) {
            boolean success = GATLING_RESULTS_FOLDER.mkdirs();
            if (success) {
                logger.info("Dossier de résultats Gatling créé avec succès : {}", //Pour les logs
                    GATLING_RESULTS_FOLDER.getAbsolutePath());
            } else {
                logger.error("Impossible de créer le dossier de résultats Gatling : {}", //Pour les logs
                    GATLING_RESULTS_FOLDER.getAbsolutePath());
                throw new RuntimeException("Échec de la création du dossier de résultats Gatling!");
            }
        } else {
            logger.info("Le dossier de résultats Gatling existe déjà : {}", //Pour les logs
                GATLING_RESULTS_FOLDER.getAbsolutePath());
        }
    }

    private void updateLatestReportPath() {
        try {
            File[] reportDirs = GATLING_RESULTS_FOLDER.listFiles(file -> 
                file.isDirectory() && new File(file, "index.html").exists());
                
            if (reportDirs != null && reportDirs.length > 0) {
                File latestReport = Arrays.stream(reportDirs)
                    .max(Comparator.comparingLong(File::lastModified))
                    .orElse(null);
                
                if (latestReport != null) {
                    this.latestReportPath = latestReport.getName();
                    logger.info("Nouveau rapport le plus récent détecté : {}", this.latestReportPath);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du chemin du dernier rapport", e);
        }
    }

    public String getLatestReportPath() {
        if (latestReportPath == null) {
            updateLatestReportPath();
        }
        return latestReportPath; // Renvoie uniquement le nom du dossier
    }
}
