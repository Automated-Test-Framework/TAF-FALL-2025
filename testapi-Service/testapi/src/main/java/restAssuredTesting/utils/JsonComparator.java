package restAssuredTesting.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for deep JSON comparison.
 * Recursively compares two JSON structures and returns a JsonNode
 * showing which fields are identical or different.
 */
public class JsonComparator {

    public static JsonNode compareJson(JsonNode node1, JsonNode node2, ObjectNode result) {
        if (node1.isArray() && node2.isArray()) {
            ArrayNode array1 = (ArrayNode) node1;
            ArrayNode array2 = (ArrayNode) node2;
            ArrayNode arrayResult = result.arrayNode();
            for (int i = 0; i < array1.size(); i++) {
                if (array1.get(i).isObject() && array2.get(i).isObject()) {
                    arrayResult.add(compareJson(array1.get(i), array2.get(i), result.objectNode()));
                } else {
                    arrayResult.add(array1.get(i).equals(array2.get(i)));
                }
            }
            return arrayResult;
        } else {
            node1.fieldNames().forEachRemaining(fieldName -> {
                JsonNode value1 = node1.get(fieldName);
                JsonNode value2 = node2.get(fieldName);

                if (value1.isObject()) {
                    if (value2 != null && value2.isObject()) {
                        result.set(fieldName, compareJson(value1, value2, result.objectNode()));
                    } else {
                        result.put(fieldName, false);
                    }
                } else if (value1.isArray() && value2.isArray()) {
                    ArrayNode array1 = (ArrayNode) value1;
                    ArrayNode array2 = (ArrayNode) value2;
                    ArrayNode arrayResult = result.arrayNode();
                    for (int i = 0; i < array1.size(); i++) {
                        if (array1.get(i).isObject() && array2.get(i).isObject()) {
                            arrayResult.add(compareJson(array1.get(i), array2.get(i), result.objectNode()));
                        } else {
                            arrayResult.add(array1.get(i).equals(array2.get(i)));
                        }
                    }
                    result.set(fieldName, arrayResult);
                } else {
                    result.put(fieldName, value1.equals(value2));
                }
            });

            return result;
        }
    }
}
