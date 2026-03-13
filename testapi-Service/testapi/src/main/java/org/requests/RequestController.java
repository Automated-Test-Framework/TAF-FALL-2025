package org.requests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.requests.payload.request.Answer;
import org.requests.payload.request.TestApiRequest;
import org.utils.JsonComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class RequestController {
    
    private final TestApiRequest request;
    private final RequestSpecification httpRequest;
    private Response response;

    private JsonNode fieldAnswer;
    private final List<String> messages = new ArrayList<>();

    public RequestController(TestApiRequest request) {
        System.out.println("############### DEBUG : RequestController INITIALISÉ ###############");

        this.request = request;
        this.httpRequest = given()
                .header("Content-Type", "application/json")
                .headers(this.request.getHeaders())
                .body(this.request.getInput());
    }

    public Answer getAnswer() {
        System.out.println("############### DEBUG : getAnswer() appelé ###############");

        this.execute();
        Answer answer = new Answer();

    // If request failed (connection refused, timeout, etc.)
    if (this.response == null) {
        answer.answer = false;
        answer.statusCode = -1;
        answer.output = "";
        answer.messages.add("❌ Erreur: Impossible de joindre l'API cible à " + this.request.getApiUrl());
        return answer;
    }
        answer.statusCode = this.response.getStatusCode();
        answer.output = this.response.getBody().asPrettyString();

        boolean statusOK = this.checkStatusCode();
        boolean outputOK = this.checkOutput();
        boolean timeOK = this.checkResponseTime();
        boolean headersOK = this.checkResponseHeaders();

        System.out.println("DEBUG : checkStatusCode() = " + statusOK);
        System.out.println("DEBUG : checkOutput() = " + outputOK);
        System.out.println("DEBUG : checkResponseTime() = " + timeOK);
        System.out.println("DEBUG : checkResponseHeaders() = " + headersOK);

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
        /*if (!timeOK) {
            answer.messages.add("❌ Erreur : Temps de réponse trop long !");
            answer.answer = false;
        }*/
        if (!headersOK) {
            answer.messages.add("❌ Erreur : Les headers ne correspondent pas à ceux attendus !");
            answer.answer = false;
        }

        answer.fieldAnswer = this.fieldAnswer;

        System.out.println("💡 DEBUG : Résultat final de answer.answer = " + answer.answer);
        System.out.println("💡 DEBUG : Messages d'erreur = " + answer.messages);

        return answer;
    }



private void execute() {
    try {
        this.response = this.request.getMethod().execute(this.httpRequest, this.request.getApiUrl());
    } catch (Exception e) {
        System.out.println("ERROR: Failed to execute request to " + this.request.getApiUrl() + " : " + e.getMessage());
        this.response = null;
    }
}

    private boolean checkStatusCode() {
        System.out.println("Expected Status Code: " + this.request.getStatusCode());
        System.out.println("Actual Status Code: " + this.response.getStatusCode());
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
        System.out.println("DEBUG : expectedOutput brut = " + expectedOutput);

        // Si aucun expectedOutput ou texte vide, ignorer la comparaison
        if (expectedOutput == null || expectedOutput.isNull() ||
            (expectedOutput.isTextual() && expectedOutput.asText().trim().isEmpty())) {
            System.out.println("DEBUG : Aucun expectedOutput défini.");
            return true;
        }

        if (expectedOutput.isTextual()) {
            String expectedText = expectedOutput.asText().trim();
            System.out.println("DEBUG : expectedOutput en texte = " + expectedText);

            // Ajout automatique des {} si le JSON semble incomplet
            if (!expectedText.startsWith("{") && !expectedText.startsWith("[") &&
                !expectedText.endsWith("}") && !expectedText.endsWith("]")) {
                expectedText = "{" + expectedText + "}";
                System.out.println("DEBUG : expectedText modifié avec {} = " + expectedText);
            }

            try {
                expectedOutput = mapper.readTree(expectedText);
                System.out.println("DEBUG : expectedOutput parsé = " + expectedOutput);
            } catch (JsonProcessingException e) {
                System.out.println("DEBUG : Erreur lors du parsing de expectedOutput : " + e.getMessage());
                return false;
            }
        }

        // Si expectedOutput est un objet vide, ignorer la comparaison
        if (expectedOutput.isObject() && expectedOutput.size() == 0) {
            System.out.println("DEBUG : expectedOutput est vide (objet vide).");
            return true;
        }

        // Parser la réponse reçue
        JsonNode output;
        try {
            String responseBody = this.response.getBody().asPrettyString();
            System.out.println("DEBUG : Réponse brute = " + responseBody);
            output = mapper.readTree(responseBody);
            System.out.println("DEBUG : Output parsé = " + output);
        } catch (JsonProcessingException e) {
            System.out.println("DEBUG : Erreur lors du parsing de la réponse : " + e.getMessage());
            return false;
        }

        // Comparaison JSON
        this.fieldAnswer = JsonComparator.compareJson(expectedOutput, output, mapper.createObjectNode());
        System.out.println("DEBUG : Différence (fieldAnswer) = " + this.fieldAnswer);

        boolean result = expectedOutput.equals(output);
        System.out.println("DEBUG : Résultat de la comparaison = " + result);
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
