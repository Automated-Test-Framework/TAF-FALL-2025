package restAssuredTesting.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JsonComparator utility.
 */
public class JsonComparatorTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void testCompareJson_SameJson() throws JsonProcessingException {
        String json = "{\"a\": 1, \"b\": 2}";
        String expectedJson = "{\"a\": true, \"b\": true}";

        JsonNode node = mapper.readTree(json);
        JsonNode expected = mapper.readTree(expectedJson);
        JsonNode compared = JsonComparator.compareJson(node, node, mapper.createObjectNode());

        assertEquals(expected, compared);
    }

    @Test
    void testCompareJson_DifferentJson() throws JsonProcessingException {
        String json1 = "{\"a\": 1, \"b\": 2}";
        String json2 = "{\"a\": 1, \"b\": 3}";
        String expectedJson = "{\"a\": true, \"b\": false}";

        JsonNode node1 = mapper.readTree(json1);
        JsonNode node2 = mapper.readTree(json2);
        JsonNode expected = mapper.readTree(expectedJson);

        JsonNode compared = JsonComparator.compareJson(node1, node2, mapper.createObjectNode());
        assertEquals(expected, compared);
    }

    @Test
    void testCompareJson_NestedJson() throws JsonProcessingException {
        String json1 = "{\"a\": {\"x\": 1, \"y\": 2}, \"b\": 3}";
        String json2 = "{\"a\": {\"x\": 1, \"y\": 2}, \"b\": 3}";
        String expectedJson = "{\"a\": {\"x\": true, \"y\": true}, \"b\": true}";

        JsonNode node1 = mapper.readTree(json1);
        JsonNode node2 = mapper.readTree(json2);
        JsonNode expected = mapper.readTree(expectedJson);

        JsonNode compared = JsonComparator.compareJson(node1, node2, mapper.createObjectNode());
        assertEquals(expected, compared);
    }

    @Test
    void testCompareJson_Arrays() throws JsonProcessingException {
        String json1 = "[1, 2, 3]";
        String json2 = "[1, 2, 3]";
        String expectedJson = "[true, true, true]";

        JsonNode node1 = mapper.readTree(json1);
        JsonNode node2 = mapper.readTree(json2);
        JsonNode expected = mapper.readTree(expectedJson);

        JsonNode compared = JsonComparator.compareJson(node1, node2, mapper.createObjectNode());
        assertEquals(expected, compared);
    }

    @Test
    void testCompareJson_DifferentArrays() throws JsonProcessingException {
        String json1 = "[1, 2, 3]";
        String json2 = "[1, 2, 4]";
        String expectedJson = "[true, true, false]";

        JsonNode node1 = mapper.readTree(json1);
        JsonNode node2 = mapper.readTree(json2);
        JsonNode expected = mapper.readTree(expectedJson);

        JsonNode compared = JsonComparator.compareJson(node1, node2, mapper.createObjectNode());
        assertEquals(expected, compared);
    }
}
