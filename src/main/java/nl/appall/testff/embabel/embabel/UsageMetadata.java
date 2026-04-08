package nl.appall.testff.embabel.embabel;

public class UsageMetadata {
    private long totalPromptTokens = 0;
    private long totalCompletionTokens = 0;

    // Anthropic pricing per million tokens
    private static final double HAIKU_INPUT_COST = 0.80;
    private static final double HAIKU_OUTPUT_COST = 4.00;

    public void addUsage(long promptTokens, long completionTokens) {
        this.totalPromptTokens += promptTokens;
        this.totalCompletionTokens += completionTokens;
    }

    public long getTotalPromptTokens() {
        return totalPromptTokens;
    }

    public long getTotalCompletionTokens() {
        return totalCompletionTokens;
    }

    public double calculateCost() {
        // Cost calculation for Claude Haiku
        double inputCost = (totalPromptTokens / 1_000_000.0) * HAIKU_INPUT_COST;
        double outputCost = (totalCompletionTokens / 1_000_000.0) * HAIKU_OUTPUT_COST;
        return inputCost + outputCost;
    }

    @Override
    public String toString() {
        return String.format(
                "Prompt tokens: %d, Completion tokens: %d, Cost: $%.4f",
                totalPromptTokens,
                totalCompletionTokens,
                calculateCost()
        );
    }
}
