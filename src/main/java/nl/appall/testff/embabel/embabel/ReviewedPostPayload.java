package nl.appall.testff.embabel.embabel;

/**
 * Minimal payload for reviewed posts when token/cost fields are absent.
 */
public record ReviewedPostPayload(String title, String content, String feedback) {}
