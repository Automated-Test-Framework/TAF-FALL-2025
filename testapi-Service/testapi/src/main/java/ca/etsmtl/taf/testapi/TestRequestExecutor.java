package ca.etsmtl.taf.testapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ca.etsmtl.taf.testapi.payload.request.Answer;
import ca.etsmtl.taf.testapi.payload.request.TestApiRequest;
import ca.etsmtl.taf.testapi.util.JsonComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class TestRequestExecutor {

    private static final Logger log = LoggerFactory.getLogger(TestRequestExecutor.class);
    
    private final TestApiRequest request;
    private final RequestSpecification httpRequest;
    private Response response;

    private JsonNode fieldAnswer;
    private final List<String> messages = new ArrayList<>();

    private boolean timedOut = false;
    private long actualResponseTime = -1;

    public TestRequestExecutor(TestApiRequest request) {
        log.debug("TestRequestExecutor initialized");

        this.request = request;
        RequestSpecification spec = given()
                .header("Content-Type", "application/json");

        if (this.request.getHeaders() != null) {
            spec = spec.headers(this.request.getHeaders());
        }

        if (this.request.getInput() != null && !this.request.getInput().isEmpty()) {
            spec = spec.body(this.request.getInput());
        }

        this.httpRequest = spec;
    }

    public Answer getAnswer() {
        log.debug("getAnswer() called");

        this.execute();
        Answer answer = new Answer();

    // If request failed (connection refused, timeout, etc.)
    if (this.response == null) {
        answer.answer = false;
        answer.statusCode = -1;
        answer.output = "";
        answer.actualResponseTime = this.actualResponseTime;
        if (this.timedOut) {
            answer.messages.add("⏱ Timeout : La requête vers " + this.request.getApiUrl()
                    + " a dépassé le délai d'attente (" + this.actualResponseTime + " ms)");
        } else {
            answer.messages.add("❌ Erreur: Impossible de joindre l'API cible à " + this.request.getApiUrl());
        }
        return answer;
    }
        answer.statusCode = this.response.getStatusCode();
        answer.output = this.response.getBody().asPrettyString();
        answer.actualResponseTime = this.response.getTime();

        boolean statusOK = this.checkStatusCode();
        boolean outputOK = this.checkOutput();
        boolean timeOK = this.checkResponseTime();
        boolean headersOK = this.checkResponseHeaders();

        log.debug("checkStatusCode()={}, checkOutput()={}, checkResponseTime()={}, checkResponseHeaders()={}",
                statusOK, outputOK, timeOK, headersOK);

        // Initialiser answer.answer à true
        answer.answer = true;

        // Ajouter les erreurs dans messages
        if (!statusOK) {
            answer.messages.add("❌ Erreur : Le code de statut ne correspond pas à l'attendu !");
            answer.answer = false;
        }
        if (!outputOK) {
            answer.messages.add("❌ Erreur : Le contenu de la réponse ne correspond pas à l'attendu !");
            answer.answer = false;
        }
        if (!timeOK) {
            answer.messages.add("⏱ Temps de réponse trop long : " + this.response.getTime()
                    + " ms (max: " + this.request.getResponseTime() + " ms)");
            answer.answer = false;
        }
        if (!headersOK) {
            answer.messages.add("❌ Erreur : Les headers ne correspondent pas à ceux attendus !");
            answer.answer = false;
        }

        answer.fieldAnswer = this.fieldAnswer;

        log.debug("Final answer={}, messages={}", answer.answer, answer.messages);

        return answer;
    }



private void execute() {
    long start = System.currentTimeMillis();
    try {
        this.response = this.request.getMethod().execute(this.httpRequest, this.request.getApiUrl());
        this.actualResponseTime = this.response.getTime();
    } catch (Exception e) {
        this.actualResponseTime = System.currentTimeMillis() - start;
        Throwable cause = e.getCause();
        if (cause instanceof SocketTimeoutException
                || e instanceof SocketTimeoutException
                || (e.getMessage() != null && e.getMessage().contains("timed out"))) {
            this.timedOut = true;
            log.warn("TIMEOUT: Request to {} timed out after {} ms", this.request.getApiUrl(), this.actualResponseTime);
        } else {
            log.error("Failed to execute request to {}: {}", this.request.getApiUrl(), e.getMessage());
        }
        this.response = null;
    }
}

    private boolean checkStatusCode() {
        log.debug("Expected StatusCode={}, Actual StatusCode={}", this.request.getStatusCode(), this.response.getStatusCode());
        return this.request.getStatusCode() == this.response.getStatusCode();
    }


    /**
     * This method checks the output of a request against the expected output.
     * <p>
     * It uses an ObjectMapper to handle JSON serialization and deserialization.
     *
     * @return true if the expected output is empty, indicating that there is no output to compare against;
     *         otherwise, it compares the expected output with the actual output and returns the result.
     */
    private boolean checkOutput() {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode expectedOutput = this.request.getExpectedOutput();
        log.debug("expectedOutput raw = {}", expectedOutput);

        // Si aucun expectedOutput ou texte vide, ignorer la comparaison
        if (expectedOutput == null || expectedOutput.isNull() ||
            (expectedOutput.isTextual() && expectedOutput.asText().trim().isEmpty())) {
            log.debug("No expectedOutput defined, skipping comparison");
            return true;
        }

        if (expectedOutput.isTextual()) {
            String expectedText = expectedOutput.asText().trim();
            log.debug("expectedOutput as text = {}", expectedText);

            // Ajout automatique des {} si le JSON semble incomplet
            if (!expectedText.startsWith("{") && !expectedText.startsWith("[") &&
                !expectedText.endsWith("}") && !expectedText.endsWith("]")) {
                expectedText = "{" + expectedText + "}";
                log.debug("expectedText wrapped with braces = {}", expectedText);
            }

            try {
                expectedOutput = mapper.readTree(expectedText);
                log.debug("expectedOutput parsed = {}", expectedOutput);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse expectedOutput: {}", e.getMessage());
                return false;
            }
        }

        // Si expectedOutput est un objet vide, ignorer la comparaison
        if (expectedOutput.isObject() && expectedOutput.size() == 0) {
            log.debug("expectedOutput is empty object, skipping comparison");
            return true;
        }

        // Parser la réponse reçue
        JsonNode output;
        try {
            String responseBody = this.response.getBody().asPrettyString();
            log.debug("Response body = {}", responseBody);
            output = mapper.readTree(responseBody);
            log.debug("Output parsed = {}", output);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse response body: {}", e.getMessage());
            return false;
        }

        // Comparaison JSON
        this.fieldAnswer = JsonComparator.compareJson(expectedOutput, output, mapper.createObjectNode());
        log.debug("fieldAnswer = {}", this.fieldAnswer);

        boolean result = expectedOutput.equals(output);
        log.debug("Comparison result = {}", result);
        return result;
    }




    private boolean checkResponseTime() {
        if (request.getResponseTime() <= 0) {
            return true;
        }
        return response.getTime() < request.getResponseTime();
    }

    private boolean checkResponseHeaders() {
        boolean ok = true;
        if (request.getExpectedHeaders() == null || request.getExpectedHeaders().isEmpty()) {
            return true;
        }    

        for (Map.Entry<String, String> expected : request.getExpectedHeaders().entrySet()) {
            String foundValue = response.header(expected.getKey());
            if (foundValue == null) {
                messages.add(String.format("Required header %s wasn't set in response", expected.getKey()));
                ok = false;
                continue;
            }

            if (!foundValue.equals(expected.getValue())) {
                messages.add(String.format("Header %s should have had the value \"%s\", found \"%s\"", expected.getKey(), expected.getValue(), foundValue));
                ok = false;
            }
        }

        return ok;
    }
}
