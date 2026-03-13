package ca.etsmtl.taf.testapi.payload.request;

import com.fasterxml.jackson.databind.JsonNode;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Answer implements Serializable {
    public int statusCode;
    public String output;

    public JsonNode fieldAnswer;
    public boolean answer;
    public long actualResponseTime = -1;

    // Human-readable messages that explain why "answer" is false
    public List<String> messages = new ArrayList<>();

}
