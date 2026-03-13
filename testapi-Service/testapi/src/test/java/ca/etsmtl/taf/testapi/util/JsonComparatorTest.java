package ca.etsmtl.taf.testapi.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonComparatorTest {

    @Test
    void testCompareJson_SameJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\": 1, \"b\": 2}";
        String expectedJson = "{\"a\": true, \"b\": true}";

        JsonNode node = mapper.readTree(json);
        JsonNode expected = mapper.readTree(expectedJson);
        JsonNode comparedResult = JsonComparator.compareJson(node, node, mapper.createObjectNode());

        assertEquals(expected, comparedResult);
    }
}
