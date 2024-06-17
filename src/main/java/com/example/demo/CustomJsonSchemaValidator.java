package com.example.demo;
import org.json.JSONObject;
import org.json.JSONArray;

public class CustomJsonSchemaValidator {

    public static void validate(JSONObject schema, JSONObject json) {
        validateObject(schema, json, "");
    }

    private static void validateObject(JSONObject schema, JSONObject json, String path) {
        JSONObject properties = schema.getJSONObject("properties");
        JSONArray required = schema.has("required") ? schema.getJSONArray("required") : new JSONArray();

        // Check required properties
        for (int i = 0; i < required.length(); i++) {
            String prop = required.getString(i);
            if (!json.has(prop)) {
                throw new ValidationException(path + "." + prop + " is required.");
            }
        }

        // Validate properties
        for (String key : properties.keySet()) {
            if (json.has(key)) {
                JSONObject propertySchema = properties.getJSONObject(key);
                validateProperty(propertySchema, json.get(key), path + "." + key);
            }
        }
    }

    private static void validateProperty(JSONObject schema, Object value, String path) {
        String type = schema.getString("type");

        switch (type) {
        case "string":
            if (!(value instanceof String)) {
                throw new ValidationException(path + " should be a string.");
            }
            break;
        case "integer":
            if (!(value instanceof Integer)) {
                throw new ValidationException(path + " should be an integer.");
            }
            if (schema.has("minimum")) {
                int minValue = schema.getInt("minimum");
                if ((Integer) value < minValue) {
                    throw new ValidationException(path + " should be >= " + minValue);
                }
            }
            break;
        case "object":
            if (!(value instanceof JSONObject)) {
                throw new ValidationException(path + " should be an object.");
            }
            validateObject(schema, (JSONObject) value, path);
            break;
        default:
            throw new ValidationException("Unknown type: " + type);
        }
    }

    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    public static void main(String[] args) {
        String schemaString = "{\n" +
                "  \"type\": \"object\",\n" +
                "  \"properties\": {\n" +
                "    \"name\": {\"type\": \"string\"},\n" +
                "    \"age\": {\"type\": \"integer\", \"minimum\": 0}\n" +
                "  },\n" +
                "  \"required\": [\"name\", \"age\"]\n" +
                "}";

        String jsonString = "{\n" +
                "  \"name\": \"John\",\n" +
                "  \"age\": 30\n" +
                "}";

        JSONObject schemaObject = new JSONObject(schemaString);
        System.out.println(schemaObject);
        JSONObject jsonObject = new JSONObject(jsonString);
        System.out.println(jsonObject);

        try {
            validate(schemaObject, jsonObject);
            System.out.println("JSON is valid against the schema.");
        } catch (ValidationException e) {
            System.out.println("JSON is not valid. Error: " + e.getMessage());
        }
    }
}

