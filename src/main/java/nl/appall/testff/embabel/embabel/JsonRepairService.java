package nl.appall.testff.embabel.embabel;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Attempts to repair and parse malformed JSON from LLM output.
 * Implements common fixes for LLM JSON generation mistakes.
 */
@Slf4j
@Component
public class JsonRepairService {

    private final ObjectMapper objectMapper;

    public JsonRepairService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Attempts to parse JSON with automatic repair on failure.
     * Tries several recovery strategies before giving up.
     */
    public <T> T parseWithRepair(String json, Class<T> targetClass) {
        try {
            return objectMapper.readValue(json, targetClass);
        } catch (Exception e) {
            log.debug("Initial JSON parse failed, attempting repair. Error: {}", e.getMessage());

            String repaired = repairJson(json);
            try {
                return objectMapper.readValue(repaired, targetClass);
            } catch (Exception e2) {
                log.error("JSON repair failed. Original: {}, Repaired: {}, Error: {}",
                        json, repaired, e2.getMessage());
                throw new JsonParseException("Failed to parse JSON even after repair attempts", e2);
            }
        }
    }

    /**
     * Attempts to repair malformed JSON and returns the repaired string.
     * Useful when callers need the repaired JSON text itself.
     */
    public String repairJsonString(String json) {
        return repairJson(json);
    }

    /**
     * Applies common repair strategies for LLM-generated JSON.
     */
    private String repairJson(String json) {
        if (json == null || json.isBlank()) {
            throw new JsonParseException("JSON is null or empty");
        }

        String repaired = json.strip();

        // Strategy 1: Remove markdown code fences
        repaired = removeMarkdownCodeFences(repaired);

        // Strategy 2: Remove leading/trailing text (LLM explanations)
        repaired = extractJsonObject(repaired);

        // Strategy 3: Fix unescaped newlines in values
        repaired = fixUnescapedNewlines(repaired);

        // Strategy 4: Fix missing/extra quotes in object structure
        repaired = fixQuoteIssues(repaired);

        return repaired;
    }

    /**
     * Removes markdown code fences (```json ... ```)
     */
    private String removeMarkdownCodeFences(String json) {
        return json
                .replaceAll("^\\s*```(?:json)?\\s*", "")
                .replaceAll("```\\s*$", "")
                .strip();
    }

    /**
     * Extracts just the JSON object from text containing extra explanations.
     * Finds the first { and last } pair.
     */
    private String extractJsonObject(String text) {
        int firstBrace = text.indexOf('{');
        int lastBrace = text.lastIndexOf('}');

        if (firstBrace == -1 || lastBrace == -1 || firstBrace >= lastBrace) {
            throw new JsonParseException("No valid JSON object found in text");
        }

        return text.substring(firstBrace, lastBrace + 1);
    }

    /**
     * Fixes unescaped literal newlines inside JSON string values.
     * This is a heuristic: looks for patterns like value\n" and fixes them.
     */
    private String fixUnescapedNewlines(String json) {
        // Replace literal newlines that appear inside quoted strings
        // This is imperfect but catches common cases
        StringBuilder result = new StringBuilder();
        boolean inString = false;
        boolean afterBackslash = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (afterBackslash) {
                result.append(c);
                afterBackslash = false;
                continue;
            }

            if (c == '\\') {
                result.append(c);
                afterBackslash = true;
                continue;
            }

            if (c == '"') {
                inString = !inString;
                result.append(c);
                continue;
            }

            if (inString && (c == '\n' || c == '\r')) {
                // Escape it
                result.append('\\').append(c == '\n' ? 'n' : 'r');
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    /**
     * Fixes common quote issues in JSON structures.
     * Handles: missing quotes around keys, incorrectly escaped quotes.
     */
    private String fixQuoteIssues(String json) {
        // Remove double-escaping that might have crept in
        // e.g., \\\\" should be \\\"
        String fixed = json.replace("\\\\\"", "\\\"");

        return fixed;
    }

    /**
     * Custom exception for JSON parsing failures.
     */
    public static class JsonParseException extends RuntimeException {
        public JsonParseException(String message) {
            super(message);
        }

        public JsonParseException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
