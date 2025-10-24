package restAssuredTesting.requests.payload.request;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Answer implements Serializable {
    public int statusCode;
    public String output;

    public JsonNode fieldAnswer;
    public boolean answer;

    // Human-readable messages that explain why "answer" is false
    public List<String> messages = new ArrayList<>();

}
