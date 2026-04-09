package nl.appall.testff.embabel.embabel;

import com.embabel.agent.api.annotation.LlmTool;
import org.springframework.stereotype.Component;

/**
 * Tool-style wrapper that exposes JSON repair and sanitization utilities.
 * Can be used directly in code or exposed to LLM tool-calling.
 */
@Component
public class JsonFixerTool {

    private final JsonRepairService jsonRepairService;
    private final JsonContentSanitizer contentSanitizer;

    public JsonFixerTool(JsonRepairService jsonRepairService, JsonContentSanitizer contentSanitizer) {
        this.jsonRepairService = jsonRepairService;
        this.contentSanitizer = contentSanitizer;
    }

    @LlmTool(
            name = "repair_json",
            description = "Repair malformed JSON and return a valid JSON string."
    )
    public String repairJson(
            @LlmTool.Param(description = "Raw JSON text to repair") String json
    ) {
        return jsonRepairService.repairJsonString(json);
    }

    @LlmTool(
            name = "escape_for_json",
            description = "Escape text so it can be safely embedded inside a JSON string value."
    )
    public String escapeForJson(
            @LlmTool.Param(description = "Raw text to escape") String input
    ) {
        return contentSanitizer.escapeForJson(input);
    }

    public BlogDraft parseBlogDraft(String json) {
        return jsonRepairService.parseWithRepair(json, BlogDraft.class);
    }

    public ReviewedPostPayload parseReviewedPost(String json) {
        return jsonRepairService.parseWithRepair(json, ReviewedPostPayload.class);
    }

    public String draftToJsonForPrompt(BlogDraft draft) {
        if (draft == null) {
            return "{}";
        }
        BlogDraft sanitized = contentSanitizer.sanitizeDraft(draft);
        return "{\"title\":\"%s\",\"content\":\"%s\"}".formatted(
                sanitized.title(),
                sanitized.content()
        );
    }
}
