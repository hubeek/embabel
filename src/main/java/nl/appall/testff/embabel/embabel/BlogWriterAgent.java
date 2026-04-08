package nl.appall.testff.embabel.embabel;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
import com.embabel.common.ai.model.ModelProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Agent(description = "Write and review a blogpost about a given topic.")
public class BlogWriterAgent {

    private final BlogAgentProperties properties;
    private final String writerModel;
    private final String reviewerModel;
    private final ModelProvider modelProvider;

    public BlogWriterAgent(BlogAgentProperties properties, ModelsProperties modelsProperties, ModelProvider modelProvider) {
        this.properties = properties;
        this.writerModel = modelsProperties.getDefaultLlm();
        this.reviewerModel = modelsProperties.getLlms().getReviewer();
        this.modelProvider = modelProvider;
    }


    @Action(description = "Write a first draft of the blogpost.")
    public DraftWithUsage writeDraft(UserInput userInput, Ai ai) {

        LlmUsageCollector usageCollector = new LlmUsageCollector();
        BlogDraft draft = ai
                .withLlm(LlmOptions.withDefaultLlm().withTemperature(0.0))
                .withId("blog-post-draft-writer")
                .withPromptContributor(Personas.WRITER)
                .withToolLoopInspectors(usageCollector)
                .creating(BlogDraft.class)
                .fromPrompt("""
                        You are a software developer and educator writing a blog post.
                        Write a blog post about: %s

                        Keep it practical and beginner friendly.
                        Use short sentences and plain language.
                        Include code examples but keep them short and simple.
                        Write the content in Markdown.

                        Return ONLY valid JSON with this shape:
                        {"title":"...","content":"..."}
                        Use \\n for line breaks inside "content". Do not use literal newlines.
                        Escape all double quotes inside "content" as \\\".
                        Escape all backslashes inside "content" as \\\\.
                        Do not add any extra text, explanations, or markdown code fences.
                        Before responding, validate that the JSON parses (RFC8259). If invalid, fix it.

                        """.formatted(userInput.getContent()));
        return new DraftWithUsage(draft, usageCollector.snapshot());
    }

    @AchievesGoal(description = "A reviewed and polished blog post")
    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(DraftWithUsage draftWithUsage, Ai ai) {
        UsageMetadata usage = new UsageMetadata();

        BlogDraft draft = draftWithUsage.draft();
        UsageSnapshot draftUsage = draftWithUsage.usage();
        usage.addUsage(writerModel, draftUsage.promptTokens(), draftUsage.completionTokens(), draftUsage.calls());

        String reviewPrompt = """
                        You are a technical editor. Review and improve this blog post.

                        Title: %s
                        Content:
                        %s

                        Fix any technical errors. Thighten the writing.
                        Provide the revised title, revised content, and a brief summary of the changes you made as feedback.

                        Return ONLY valid JSON with this shape:
                        {"title":"...","content":"...","feedback":"..."}
                        Use \\n for line breaks inside "content". Do not use literal newlines.
                        Escape all double quotes inside "content" and "feedback" as \\\".
                        Escape all backslashes inside "content" and "feedback" as \\\\.
                        Do not add any extra text, explanations, or markdown code fences.
                        Before responding, validate that the JSON parses (RFC8259). If invalid, fix it.

                        """.formatted(draft.title(), draft.content());

        LlmUsageCollector usageCollector = new LlmUsageCollector();
        ReviewedPost reviewed = ai
                .withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .withPromptContributor(Personas.REVIEWER)
                .withToolLoopInspectors(usageCollector)
                .creating(ReviewedPost.class)
                .fromPrompt(reviewPrompt);

        UsageSnapshot reviewUsage = usageCollector.snapshot();
        usage.addUsage(reviewerModel, reviewUsage.promptTokens(), reviewUsage.completionTokens(), reviewUsage.calls());

        ReviewedPost enriched = new ReviewedPost(
                reviewed.title(),
                reviewed.content(),
                reviewed.feedback(),
                usage.getTotalPromptTokens(),
                usage.getTotalCompletionTokens(),
                usage.calculateCost(modelProvider)
        );

        writeToFile(enriched, usage);
        return enriched;
    }

    private void writeToFile(ReviewedPost post, UsageMetadata usage) {
        String filename = post.title()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                + ".md";

        Path outputDir = Path.of(properties.outputDir());
        Path filePath = outputDir.resolve(filename);

        try {
            Files.createDirectories(outputDir);

            // Prepend metadata header with token and cost information
            String metadata = String.format("""
                    writer: %s
                    reviewer: %s
                    LLMs used: [%s] across %d calls
                    Prompt tokens: %d
                    Completion tokens: %d
                    Cost: $%.4f

                    """,
                    writerModel,
                    reviewerModel,
                    String.join(", ", usage.getModelsUsed()),
                    usage.getTotalCalls(),
                    usage.getTotalPromptTokens(),
                    usage.getTotalCompletionTokens(),
                    usage.calculateCost(modelProvider)
            );

            String fullContent = metadata + post.content();
            Files.writeString(filePath, fullContent);
            log.info("Blog post written to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write blog post to {}: {}", filePath, e.getMessage());
        }
    }

}
