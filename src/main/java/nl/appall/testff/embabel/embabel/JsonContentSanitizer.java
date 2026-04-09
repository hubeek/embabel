package nl.appall.testff.embabel.embabel;

import org.springframework.stereotype.Component;

/**
 * Sanitizes and escapes text content to be valid within JSON strings.
 * Used as a final cleanup layer after LLM generation.
 */
@Component
public class JsonContentSanitizer {

    /**
     * Escapes a string to be safe within a JSON string value.
     * Handles: quotes, backslashes, control characters, unicode.
     */
    public String escapeForJson(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("\\", "\\\\")      // backslash first (must be first!)
                .replace("\"", "\\\"")      // escape quotes
                .replace("\r", "\\r")       // carriage return
                .replace("\n", "\\n")       // newline
                .replace("\t", "\\t")       // tab
                .replace("\b", "\\b")       // backspace
                .replace("\f", "\\f");      // form feed
    }

    /**
     * Sanitizes a BlogDraft by escaping content field.
     */
    public BlogDraft sanitizeDraft(BlogDraft draft) {
        if (draft == null) {
            return null;
        }
        return new BlogDraft(
                escapeForJson(draft.title()),
                escapeForJson(draft.content())
        );
    }

    /**
     * Sanitizes a ReviewedPost by escaping content and feedback fields.
     */
    public ReviewedPost sanitizeReviewedPost(ReviewedPost post) {
        if (post == null) {
            return null;
        }
        return new ReviewedPost(
                escapeForJson(post.title()),
                escapeForJson(post.content()),
                escapeForJson(post.feedback()),
                post.promptTokens(),
                post.completionTokens(),
                post.totalCost()
        );
    }
}