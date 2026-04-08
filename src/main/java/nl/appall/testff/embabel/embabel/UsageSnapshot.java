package nl.appall.testff.embabel.embabel;

public record UsageSnapshot(long promptTokens, long completionTokens, int calls) {
}
