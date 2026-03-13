package ca.etsmtl.taf.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import ca.etsmtl.taf.entity.GatlingRequest;
import ca.etsmtl.taf.provider.GatlingJarPathProvider;

@Service
public class GatlingExecutionService {

    public String runSimulation(GatlingRequest gatlingRequest) throws IOException, URISyntaxException, InterruptedException {
        String gatlingJarPath = new GatlingJarPathProvider().getGatlingJarPath();

        String testRequest = "{\\\"baseUrl\\\":\\\"" + gatlingRequest.getTestBaseUrl()
                + "\\\",\\\"scenarioName\\\":\\\"" + gatlingRequest.getTestScenarioName()
                + "\\\",\\\"requestName\\\":\\\"" + gatlingRequest.getTestRequestName()
                + "\\\",\\\"uri\\\":\\\"" + gatlingRequest.getTestUri()
                + "\\\",\\\"requestBody\\\":\\\"" + gatlingRequest.getTestRequestBody()
                + "\\\",\\\"methodType\\\":\\\"" + gatlingRequest.getTestMethodType()
                + "\\\",\\\"usersNumber\\\":\\\"" + gatlingRequest.getTestUsersNumber() + "\\\"}";

        List<String> commandArgs = new ArrayList<>();
        commandArgs.add("java");
        commandArgs.add("-jar");
        commandArgs.add(gatlingJarPath);
        commandArgs.add("-DrequestJson=" + testRequest);

        ProcessBuilder processBuilder = new ProcessBuilder(commandArgs);
        Process process = processBuilder.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line).append('\n');
        }

        int exitCode = process.waitFor();
        return "Exit Code: " + exitCode + "\nOutput:\n" + output.toString();
    }
}
