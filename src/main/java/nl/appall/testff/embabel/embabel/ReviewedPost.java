package nl.appall.testff.embabel.embabel;

public record ReviewedPost(
        String title,
        String content,
        String feedback,
        long promptTokens,
        long completionTokens,
        double totalCost) {

    // Constructor for backward compatibility (without token/cost info)
    public ReviewedPost(String title, String content, String feedback) {
        this(title, content, feedback, 0, 0, 0.0);
    }
}
