package ca.etsmtl.taf.performance.gatling.services;

import ca.etsmtl.taf.performance.gatling.config.GatlingConfigurator;
import ca.etsmtl.taf.performance.gatling.model.GatlingTestRequest;
import ca.etsmtl.taf.performance.gatling.simulation.SimulationFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.app.Gatling;
import io.gatling.core.config.GatlingPropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.io.ByteArrayOutputStream;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SimulationService {

    private static final Logger logger = LoggerFactory.getLogger(SimulationService.class);

    public String runSimulation(GatlingTestRequest gatlingRequest) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String testRequest = objectMapper.writeValueAsString(gatlingRequest);
        
        // Use a ByteArrayOutputStream to capture output safely without piping complexity for now
        // if the output is not too large, this is much safer.
        // If it is large, we should use a file or a better pipe implementation.
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(outputStream);

        // Run Gatling simulation in a separate thread
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> future = executor.submit(() -> {
            @SuppressWarnings("java:S106")
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            try {
                System.setOut(ps);
                System.setErr(ps);
                logger.info("Executing Gatling with request: {}", testRequest);
                System.setProperty("requestJson", testRequest);
                
                GatlingPropertiesBuilder props = new GatlingPropertiesBuilder();
                String resultsFolder = GatlingConfigurator.getGatlingResultsFolder();
                logger.info("Using results folder: {}", resultsFolder);
                props.resultsDirectory(resultsFolder);
                
                String simulationClass = new SimulationFactory().getSimulationName(gatlingRequest.getSimulationStrategy());
                logger.info("Using simulation class: {}", simulationClass);
                props.simulationClass(simulationClass);
                
                int exitCode = Gatling.fromMap(props.build());
                ps.flush();
                
                if (exitCode != 0) {
                    logger.warn("Gatling simulation finished with exit code: {}", exitCode);
                    if (exitCode == 1) {
                         throw new RuntimeException("Gatling simulation failed to start or encountered a fatal error (exit code 1). Check logs for details.");
                    }
                }
            } catch (Exception e) {
                logger.error("Error happened during Gatling execution", e);
                throw new RuntimeException(e);
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }
        });

        try {
            future.get(); // Wait for the simulation to finish
        } catch (Exception e) {
            Throwable cause = e.getCause();
            String gatlingOutput = outputStream.toString();
            String errorMessage = (cause != null) ? cause.getMessage() : e.getMessage();
            logger.error("Gatling simulation failed. Output captured so far: \n{}", gatlingOutput);
            throw new IOException("Gatling simulation failed: " + errorMessage + ". Output: " + gatlingOutput, e);
        } finally {
            ps.close();
            executor.shutdown();
        }

        return outputStream.toString();
    }


    public String parseOutput(String output) {
        StringBuilder returnString = new StringBuilder("---- Global Information --------------------------------------------------------\n");
        String startPattern = "---- Global Information --------------------------------------------------------";
        String endPattern = "---- Response Time Distribution ------------------------------------------------";
        Pattern pattern = Pattern.compile(startPattern + "(.*?)" + endPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(output);
        if (matcher.find()) {
            returnString.append(matcher.group(1).trim()).append("\n");
        } else {
            returnString.append("Not found in Gatling output.");
        }

        returnString.append("---- Generated Report ------------------------------------------------------\n");

        String regex = "Please open the following file: (file:///[^\\s]+|https?://[^\\s]+)";

        pattern = Pattern.compile(regex, Pattern.MULTILINE);
        matcher = pattern.matcher(output);

        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                returnString.append(matcher.group(i).trim());
            }
        } else {
            returnString.append("Not found in Gatling output.");
        }

        return returnString.toString();
    }
}