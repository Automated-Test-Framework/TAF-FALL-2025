package ca.etsmtl.taf.performance.jmeter.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * This class is responsible for performing initialization tasks of JMeter
 * component on application
 * startup:
 * - Creating a temp folder for JMeter
 * - Copying the JMX template files from resources to the temp folder
 * - Copying JMeter properties files from resources to the temp folder
 * - Initializing the resource handler to serve the JMeter results folder as a
 * static resource
 * 
 * These files are necessary for running JMeter test plans.
 * 
 * @version 1.0
 */
@Component
public class JMeterConfigurator implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(JMeterConfigurator.class);
    private static final File SYSTEM_TEMP_FOLDER = new File(System.getProperty("java.io.tmpdir"));

    private static final File JMETER_TEMP_FOLDER = new File(SYSTEM_TEMP_FOLDER, "jmeter");
    private static final File JMETER_BIN_FOLDER = new File(JMETER_TEMP_FOLDER, "bin");
    private static final File JMETER_REPORT_TEMPLATE_FOLDER = new File(JMETER_BIN_FOLDER, "report-template");
    private static final File JMETER_TEMPLATES_FOLDER = new File(JMETER_TEMP_FOLDER, "templates");
    private static final File JMETER_RESULTS_FOLDER = new File(JMETER_TEMP_FOLDER, "results");

    private static final List<String> JMETER_PROPERTIES_FILES = List.of("jmeter.properties", "log4j2.xml",
            "reportgenerator.properties", "saveservice.properties", "system.properties", "upgrade.properties",
            "user.properties");

    private static final List<String> JMETER_TEMPLATES_FILES = List.of("FTPSamplerTemplate.jmx",
            "HttpSamplerTemplate.jmx");

    @Autowired
    ResourceLoader resourceLoader;

    /**
     * Get the path to the JMeter home folder used during runtime
     * 
     * @return The path
     */
    public static String getJmeterHome() {
        return JMETER_TEMP_FOLDER.getAbsolutePath();
    }

    /**
     * Get the path to the JMeter bin folder used during runtime.
     * This is where all JMeter properties file are stored.
     * 
     * @return The path
     */
    public static String getJmeterBinFolder() {
        return JMETER_BIN_FOLDER.getAbsolutePath();
    }

    /**
     * Get the path to the JMeter templates folder used during runtime.
     * The templates most contains JMX files (test plans) that can be used as either
     * HTTP or FTP tests.
     * 
     * @return The path
     */
    public static String getJmeterTemplatesFolder() {
        return JMETER_TEMPLATES_FOLDER.getAbsolutePath();
    }

    /**
     * Get the path to the JMeter results folder used during runtime.
     * This is where JMeter will store the results of the test runs.
     * 
     * @return The path
     */
    public static String getJmeterResultsFolder() {
        return JMETER_RESULTS_FOLDER.getAbsolutePath();
    }

    /**
     * Get the absolute path to the JMX file containing the test plan for HTTP
     * tests.
     * 
     * @return The absolute path path
     */
    public static String getHTTPSamplerTemplate() {
        return new File(JMETER_TEMPLATES_FOLDER, "HttpSamplerTemplate.jmx").getAbsolutePath();
    }

    /**
     * Get the absolute path to the JMX file containing the test plan for FTP
     * tests.
     * 
     * @return The absolute path path
     */
    public static String getFTPSamplerTemplate() {
        return new File(JMETER_TEMPLATES_FOLDER, "FTPSamplerTemplate.jmx").getAbsolutePath();
    }

    /**
     * Convenient event listener called when the SpringBootApplication has been
     * initialized and is
     * ready to listen to requests.
     * 
     * It creates a temp folder for JMeter and copies various files that JMeter
     * requires at runtime. All the files are container in the Maven package
     * org.apache.jmeter:ApacheJMeter_config.
     * 
     * @param event ApplicationReadyEvent
     * @see <a href=
     *      "https://mvnrepository.com/artifact/org.apache.jmeter/ApacheJMeter_config">JMeter
     *      config package</a>
     */
    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {

        // Create the temp folder for JMeter and the templates folder
        createJMeterFolders();

        // Copy all properties files from resources folder org.apache.jmeter to the temp
        // folder
        copyJMeterPropertiesFiles();

        // Copy all JMX template files from resources folder org.apache.jmeter.templates
        // to the temp folder
        copyJMXTemplateFiles();

        // Copy all report template files from resources folder
        // org.apache.jmeter.report.templates
        // to the temp folder
        copyReportTemplateFiles();
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        String location = "file:" + JMETER_RESULTS_FOLDER.getAbsolutePath() + "/";
        registry.addResourceHandler("/reports/performance/jmeter/dashboard/**", "/team3/reports/performance/jmeter/dashboard/**")
                .addResourceLocations(location)
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
        logger.info("JMeter resource handler configured for location: {}", location);
    }

    /**
     * Create the folders that JMeter requires at runtime.
     */
    private void createJMeterFolders() {
        if (!JMETER_TEMP_FOLDER.exists()) {
            createFolder(JMETER_TEMP_FOLDER, "temp folder", true);
        } else {
            logger.info("JMeter temp folder already exists!");
        }

        if (!JMETER_BIN_FOLDER.exists()) {
            createFolder(JMETER_BIN_FOLDER, "bin folder", true);
        } else {
            logger.info("JMeter bin folder already exists!");
        }

        // Start fresh, delete the complete file structure under JMETER_REPORT_TEMPLATE
        if (JMETER_REPORT_TEMPLATE_FOLDER.exists()) {
            logger.info("Cleaning up JMeter report template folder");
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<?> future = executor.submit(() -> {
                try {
                    Files.walk(JMETER_REPORT_TEMPLATE_FOLDER.toPath()).map(Path::toFile).forEach(File::delete);
                } catch (IOException e) {
                    logger.error("Error cleaning up JMeter report template folder", e);
                }
            });
            try {
                future.get();
                createFolder(JMETER_REPORT_TEMPLATE_FOLDER, "report template", false);
            } catch (InterruptedException | ExecutionException e) {
                Thread.currentThread().interrupt();
                // Dramatic failure
                throw new RuntimeException(e);
            }
        } else {
            createFolder(JMETER_REPORT_TEMPLATE_FOLDER, "report template", true);
        }

        if (!JMETER_TEMPLATES_FOLDER.exists()) {
            createFolder(JMETER_TEMPLATES_FOLDER, "templates folder", true);
        } else {
            logger.info("JMeter templates folder already exists!");
        }

        // Results folder is created by JMeter itself
    }

    /**
     * Create a folder and log the result
     * 
     * @param folder       The folder to create
     * @param folderName   The name of the folder
     * @param throwFailure If true, throw a RuntimeException if the folder creation
     *                     fails
     */
    private void createFolder(File folder, String folderName, boolean throwFailure) {
        boolean success = folder.mkdirs();

        if (success) {
            logger.info("JMeter {} folder created successfully!", folderName);
        } else {
            logger.info("Failed to create JMeter {} folder!", folderName);
            // Dramatic failure
            if (throwFailure) {
                throw new RuntimeException();
            }
        }
    }

    /**
     * Copy all JMeter properties files from resources to the temp folder
     * JMeter properties files are found in the Maven package
     * org.apache.jmeter:ApacheJMeter_config
     * Using {@link java.lang.ClassLoader#getResourceAsStream(String)
     * getResourceAsStream} to ensure portability.
     * 
     * @see <a href=
     *      "https://www.baeldung.com/java-classpath-resource-cannot-be-opened">How
     *      to Avoid the Java FileNotFoundException When Loading Resources</a>
     */
    private void copyJMeterPropertiesFiles() {
        for (String propertiesFile : JMETER_PROPERTIES_FILES) {
            try (var inputStream = getClass().getResourceAsStream("/bin/" + propertiesFile)) {
                if (inputStream == null) {
                    logger.warn("Resource not found: {}", propertiesFile);
                    continue;
                }
                Path destinationPath = new File(JMETER_BIN_FOLDER, propertiesFile).toPath();
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied JMeter properties file: {} -> {}", propertiesFile, destinationPath);
            } catch (IOException e) {
                logger.error("Error copying JMeter properties file: {}", propertiesFile, e);
            }
        }
    }

    /**
     * Copy all JMX template files from resources to the temp/templates folder
     * Using {@link java.lang.ClassLoader#getResourceAsStream(String)
     * getResourceAsStream} to ensure portability.
     * 
     * @see <a href=
     *      "https://www.baeldung.com/java-classpath-resource-cannot-be-opened">How
     *      to Avoid the Java FileNotFoundException When Loading Resources</a>
     */
    private void copyJMXTemplateFiles() {
        for (String templateFile : JMETER_TEMPLATES_FILES) {
            try (var inputStream = getClass().getResourceAsStream("/org/apache/jmeter/templates/" + templateFile)) {
                if (inputStream == null) {
                    logger.warn("Resource not found: {}", templateFile);
                    continue;
                }
                Path destinationPath = new File(JMETER_TEMPLATES_FOLDER, templateFile).toPath();
                Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                logger.debug("Copied JMeter template file: {} -> {}", templateFile, destinationPath);
            } catch (IOException e) {
                logger.error("Error copying JMeter template file: {}", templateFile, e);
            }
        }
    }

    /**
     * Copy folder structure under
     * /bin/report-template to the report-template
     * temp folder
     * Using {@link java.lang.ClassLoader#getResourceAsStream(String)
     * getResourceAsStream} to ensure portability.
     * 
     * @see <a href=
     *      "https://www.baeldung.com/java-classpath-resource-cannot-be-opened">How
     *      to Avoid the Java FileNotFoundException When Loading Resources</a>
     */
    private void copyReportTemplateFiles() {

        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath:/bin/report-template/**");

            AtomicInteger count = new AtomicInteger(0);
            for (Resource resource : resources) {

                if (count.get() == 0) {
                    // Skip the first resource, it's the bin/report-template itself
                    count.incrementAndGet();
                    continue;
                }

                String resourcePathParts[] = resource.getURL().toString().split("!");
                String resourcePath = resourcePathParts[resourcePathParts.length - 1];

                Path targetResourcePath = new File(JMETER_TEMP_FOLDER, resourcePath)
                        .toPath();

                if (resourcePath.endsWith("/")) {
                    // This is a directory, create it in the temp folder
                    Files.createDirectories(targetResourcePath);
                } else {
                    // This is a file, copy it to the temp folder
                    try (InputStream inputStream = resource.getInputStream()) {
                        Files.copy(inputStream,
                                targetResourcePath,
                                StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }

            logger.info("Successfully copied report template folder structure");
            logger.debug("Report template folder structure to: {}", JMETER_REPORT_TEMPLATE_FOLDER);
        } catch (IOException e) {
            logger.error("Error copying JMeter template report folder structure", e);
        }

    }
}
