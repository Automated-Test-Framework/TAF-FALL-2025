package ca.etsmtl.taf.serviceTestAPI;

import ca.etsmtl.taf.dtoTestAPI.*;
import ca.etsmtl.taf.repositoryTestAPI.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import ca.etsmtl.taf.entityTestAPI.*;

@Service
public class QaOrchestrator {

    private final TestRunRepo runRepo;
    private final TestCaseResultRepo caseRepo;
    private final TestSuiteRepo suiteRepo;

    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());
    private final ApiExecutor executor = new ApiExecutor();

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    @Value("${qa.reports-dir:target/cucumber-ui}")
    private String reportsDir;

    public QaOrchestrator(TestRunRepo runRepo, TestCaseResultRepo caseRepo, TestSuiteRepo suiteRepo) {
        this.runRepo = runRepo; this.caseRepo = caseRepo; this.suiteRepo = suiteRepo;
    }

    /** Enregistrer une suite (retourne l'id Mongo) */
    public String saveSuite(String name, String owner, String format, String content){
        var doc = new TestSuite();
        doc.name = name; doc.owner = owner; doc.format = format; doc.content = content;
        doc.createdAt = doc.updatedAt = Instant.now(); doc.version = 1;
        return suiteRepo.save(doc).id;
    }

    /** Soumettre un run asynchrone à partir d'une suite existante ou d'un YAML override */
    public String submitRun(String suiteId, String overrideYaml) {
        var suite = suiteRepo.findById(suiteId).orElseThrow();
        var run = new TestRun();
        run.id = UUID.randomUUID().toString();
        run.suiteId = suite.id; run.suiteName = suite.name;
        run.status = "QUEUED"; run.createdAt = Instant.now();
        runRepo.save(run);

        // lancer asynchrone
        pool.submit(() -> execute(run.id, (overrideYaml!=null && !overrideYaml.isBlank()) ? overrideYaml : suite.content));
        return run.id;
    }

    /** Exécution d'un run (Rest-Assured + Cucumber report) */
    private void execute(String runId, String suiteYaml) {
        var run = runRepo.findById(runId).orElseThrow();
        run.status = "RUNNING"; run.startedAt = Instant.now(); runRepo.save(run);

        try {
            // 1) Exécuter la suite via Rest-Assured (ApiExecutor)
            ApiSuite suite = yaml.readValue(suiteYaml, ApiSuite.class);
            RunResult rr = executor.run(suite);

            // 2) Persister résultats par test
            for (var t : rr.getTests()) {
                var cr = new TestCaseResult();
                cr.runId = run.id; cr.name = t.getName(); cr.statusCode = t.getStatusCode();
                cr.passed = t.isPassed(); cr.error = t.getError(); cr.durationMs = t.getDurationMs();
                caseRepo.save(cr);
            }

            // 3) Statistiques globales
            run.found  = rr.getTests().size();
            run.passed = (int) rr.getTests().stream().filter(x->x.isPassed()).count();
            run.failed = run.found - run.passed;
            run.skipped= 0;

            // 4) Générer un rapport Cucumber HTML (optionnel mais demandé)
            run.reportIndexPath = generateCucumberReport(run.id, suiteYaml);

            run.status = (run.failed==0) ? "PASSED" : "FAILED";
        } catch (Exception e) {
            run.status = "FAILED";
            if (run.logs==null) run.logs=new ArrayList<>();
            run.logs.add(e.toString());
        } finally {
            run.endedAt = Instant.now();
            runRepo.save(run);
        }
    }

    /** Génère un .feature temporaire + exécute Cucumber via JUnit Platform → HTML */
    private String generateCucumberReport(String runId, String suiteYaml) throws IOException {
        Path base = Paths.get(reportsDir, runId);
        Files.createDirectories(base);

        // Feature temporaire qui encapsule le YAML (DocString)
        Path feature = base.resolve("ui-suite.feature");
        String text = "" +
        "Feature: UI-triggered API suite"+

                "Scenario: Execute YAML " +
                "Given the following API test suite:"+
                "\"\"\""+
        "" + suiteYaml + "\n            \"\"\"\n"  +
                "When I run the API suite\n " +
                "Then all tests must pass\n";
        Files.writeString(feature, text);

        String htmlOut = base.resolve("html").toString();
        String jsonOut = base.resolve("cucumber.json").toString();
        String junitOut = base.resolve("junit.xml").toString();

        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener sum = new SummaryGeneratingListener(); launcher.registerTestExecutionListeners(sum);

        LauncherDiscoveryRequest req = LauncherDiscoveryRequestBuilder.request()
                .configurationParameters(Map.of(
                        "cucumber.glue", "com.acme.qa.bdd,com.acme.qa.testsupport",   // tes steps Cucumber
                        "cucumber.plugin", String.format("pretty, summary, html:%s, json:%s, junit:%s", htmlOut, jsonOut, junitOut)
                ))
                .selectors(DiscoverySelectors.selectFile(feature.toAbsolutePath().toString()))
                .build();

        launcher.execute(req);
        return htmlOut + "/index.html";
    }
}
