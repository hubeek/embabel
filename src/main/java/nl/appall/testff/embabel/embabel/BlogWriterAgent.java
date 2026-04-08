package nl.appall.testff.embabel.embabel;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import com.embabel.common.ai.model.LlmOptions;
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

    public BlogWriterAgent(BlogAgentProperties properties, ModelsProperties modelsProperties) {
        this.properties = properties;
        this.writerModel = modelsProperties.getDefaultLlm();
        this.reviewerModel = modelsProperties.getLlms().getReviewer();
    }


    @Action(description = "Write a first draft of the blogpost.")
    public BlogDraft writeDraft(UserInput userInput, Ai ai) {

        return ai
                .withLlm(LlmOptions.withDefaultLlm().withTemperature(0.0))
                .withId("blog-post-draft-writer")
                .withPromptContributor(Personas.WRITER)
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
    }

    @AchievesGoal(description = "A reviewed and polished blog post")
    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(BlogDraft draft, Ai ai) {
        UsageMetadata usage = new UsageMetadata();

        // Track writer call tokens (estimate based on content)
        long writerPromptTokens = estimateTokens(draft.title() + draft.content());
        usage.addUsage(2266, writerPromptTokens); // 2266 from example, completion tokens from content

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

        ReviewedPost reviewed = ai
                .withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .withPromptContributor(Personas.REVIEWER)
                .creating(ReviewedPost.class)
                .fromPrompt(reviewPrompt);

        // Track reviewer call tokens
        long reviewerPromptTokens = estimateTokens(reviewPrompt);
        long reviewerCompletionTokens = estimateTokens(reviewed.content() + reviewed.feedback());
        usage.addUsage(reviewerPromptTokens, reviewerCompletionTokens);

        writeToFile(reviewed, usage);
        return reviewed;
    }

    private long estimateTokens(String text) {
        // Rough estimate: 1 token per 4 characters for English text
        return Math.max(1, text.length() / 4);
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
                    LLMs used: [%s] across 2 calls
                    Prompt tokens: %d
                    Completion tokens: %d
                    Cost: $%.4f

                    """,
                    writerModel,
                    reviewerModel,
                    writerModel,
                    usage.getTotalPromptTokens(),
                    usage.getTotalCompletionTokens(),
                    usage.calculateCost()
            );

            String fullContent = metadata + post.content();
            Files.writeString(filePath, fullContent);
            log.info("Blog post written to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write blog post to {}: {}", filePath, e.getMessage());
        }
    }

}
