package ca.etsmtl.taf.performance.jmeter.utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import ca.etsmtl.taf.performance.jmeter.model.JMeterResponseDetails;
import org.apache.jmeter.engine.JMeterEngine;
import org.apache.jmeter.engine.JMeterEngineException;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.exceptions.IllegalUserActionException;
import org.apache.jmeter.gui.tree.JMeterTreeModel;
import org.apache.jmeter.gui.tree.JMeterTreeNode;
import org.apache.jmeter.report.config.ConfigurationException;
import org.apache.jmeter.report.dashboard.GenerationException;
import org.apache.jmeter.report.dashboard.ReportGenerator;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.save.SaveService;
import org.apache.jmeter.testelement.TestStateListener;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.etsmtl.taf.performance.jmeter.JMeterRunnerException;
import ca.etsmtl.taf.performance.jmeter.config.JMeterConfigurator;
import ca.etsmtl.taf.performance.jmeter.model.JMeterResponse;
import ca.etsmtl.taf.performance.jmeter.model.TestPlanBase;

/**
 * This class is responsible for running JMeter tests and converting the results
 * to JSON.
 * 
 * @version 1.0
 */
public class JMeterRunner {

  private static final Logger logger = LoggerFactory.getLogger(JMeterRunner.class);

  private JMeterRunner() {
    throw new IllegalStateException("Utility class");
  }

  public static JMeterResponse executeTestPlanAndGenerateReport(TestPlanBase testPlan) throws JMeterRunnerException {

    JMeterResponse jMeterResponse = new JMeterResponse("", "", null, null);
    JMeterResponseDetails jMeterResponseDetails = new JMeterResponseDetails(null, null);
    jMeterResponse.setDetails(jMeterResponseDetails);
    try {
      File resultsFile = getResultsFile();
      testPlan.generateTestPlan();

      initializeJMeter();

      String dashboardLocation = runTests(resultsFile);

      populateResponse(jMeterResponse, jMeterResponseDetails, dashboardLocation);

    } catch (JMeterRunnerException e) {
      throw new JMeterRunnerException(e.getMessage(), e);
    }

    return jMeterResponse;

  }

  @SuppressWarnings("java:S2139")
  private static void populateResponse(JMeterResponse jMeterResponse, JMeterResponseDetails jMeterResponseDetails, String dashboardLocation)
      throws JMeterRunnerException {
    try {
      // Load statistics.json from the dashboardDir folder and deserialize
      File statisticsFile = new File(dashboardLocation, "statistics.json");
      if (statisticsFile.exists()) {
        logger.info("Loading statistics file generated at {}", statisticsFile.getAbsolutePath());
      }
      ObjectMapper mapper = new ObjectMapper();
      Map<String, Object> jsonData = mapper.readValue(statisticsFile, new TypeReference<>() {
      });
      jMeterResponse.setSummary(jsonData);

      // Merge statistics and dashboarddir into a single JSON object
      jMeterResponseDetails.setContentType("html");
      jMeterResponseDetails.setLocationURL(dashboardLocation);
      logger.debug("JMeterResponse created successfully!");
    } catch (IOException e) {
      logger.error("Error loading statistics file", e);
      throw new JMeterRunnerException(e.getMessage(), e);
    }
  }

  private static File getTestPlan() {
    File jmxFilePath = new File(JMeterConfigurator.getJmeterTemplatesFolder(), "TestPlan.jmx");
    logger.info("Using Test Plan {}", jmxFilePath);

    return jmxFilePath;
  }

  private static File getResultsFile() {

    String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File resultsFile = new File(JMeterConfigurator.getJmeterResultsFolder(), timestamp + ".csv");
    logger.info("Results will be logged in {}", resultsFile.toPath());

    return resultsFile;
  }

  private static void initializeJMeter() throws JMeterRunnerException {

    JMeterUtils
        .loadJMeterProperties(new File(JMeterConfigurator.getJmeterBinFolder(), "jmeter.properties").getAbsolutePath());

    JMeterUtils.initLocale();

    JMeterUtils.setJMeterHome(JMeterConfigurator.getJmeterHome());

    try {
      SaveService.loadProperties();

    } catch (IOException e) {
      throw new JMeterRunnerException(e.getMessage(), e);
    }

  }

  @SuppressWarnings("java:S2139")
  private static String runTests(File resultsFile) throws JMeterRunnerException {

    try {
      // Used to wait for the tests to finish
      CountDownLatch latch = new CountDownLatch(1);

      HashTree tree = SaveService.loadTree(getTestPlan());

      JMeterTreeModel treeModel = new JMeterTreeModel();
      JMeterTreeNode root = (JMeterTreeNode) treeModel.getRoot();
      treeModel.addSubTree(tree, root);

      Summariser summeriser = null;
      String summariserName = JMeterUtils.getPropDefault("summariser.name", "");
      if (!summariserName.isEmpty()) {
        logger.info("Creating summariser <{}>", summariserName);
        summeriser = new Summariser(summariserName);
      }

      ResultCollector collector = new ResultCollector(summeriser);
      collector.setFilename(resultsFile.getAbsolutePath());
      tree.add(tree.getArray()[0], collector);

      logger.debug("Created JMeter tree successfully using {}", getTestPlan().getName());

      JMeterEngine engine = new StandardJMeterEngine();
      tree.add(tree.getArray()[0], new TestListener(latch));

      engine.configure(tree);

      engine.runTest();

      // Wait for the tests to finish
      latch.await();

      // Generate HTML dashboard report in dashboardDir folder
      String dashboardDir = new File(JMeterConfigurator.getJmeterResultsFolder(),
          "dashboard_" + resultsFile.getName().replace(".csv", "")).getAbsolutePath();
      JMeterUtils.setProperty("jmeter.reportgenerator.exporter.html.property.output_dir", dashboardDir);
      JMeterUtils.setProperty("jmeter.reportgenerator.exporter.json.property.output_dir", dashboardDir);
      ReportGenerator generator = new ReportGenerator(resultsFile.getAbsolutePath(), null);
      generator.generate();

      logger.info("JMeter tests completed successfully");

      return dashboardDir;

    } catch (IllegalUserActionException | IOException | InterruptedException | JMeterEngineException e) {
      logger.error("Error running JMeter test", e);
      throw new JMeterRunnerException(e.getMessage(), e);
    } catch (ConfigurationException | GenerationException e) {
      logger.error("Error generating JMeter report", e);
      throw new JMeterRunnerException(e.getMessage(), e);
    }
  }

  private static class TestListener implements TestStateListener {

    private final CountDownLatch latch;

    public TestListener(CountDownLatch latch) {
      this.latch = latch;
    }

    @Override
    public void testStarted() {
      if (logger.isInfoEnabled()) {
        final long now = System.currentTimeMillis();
        logger.info("{} ({})", "Started JMeter tests at", now);
      }
    }

    @Override
    public void testStarted(String host) {
      logger.info("Test started on host {}", host);
    }

    @Override
    public void testEnded() {
      // Should stop waiting for the tests to finish
      latch.countDown();
      if (logger.isInfoEnabled()) {
        final long now = System.currentTimeMillis();
        logger.info("{} ({})", "Ended JMeter tests at", now);
      }
    }

    @Override
    public void testEnded(String host) {
      logger.info("Test ended on host {}", host);
    }

  }
}
