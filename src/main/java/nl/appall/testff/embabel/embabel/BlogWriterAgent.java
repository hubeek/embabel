package nl.appall.testff.embabel.embabel;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Agent(description = "Write and review a blogpost about a given topic.")
public class BlogWriterAgent {

    private final BlogAgentProperties properties;

    public BlogWriterAgent(BlogAgentProperties properties) {
        this.properties = properties;
    }


    @Action(description = "Write a first draft of the blogpost.")
    public BlogDraft writeDraft(UserInput userInput, Ai ai) {

        return ai
//                .withLlm(LlmOptions.withDefaultLlm().withTemperature(.2))
                .withDefaultLlm()
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
                        Use \\n for line breaks inside "content".
                        Do not add any extra text or markdown code fences.

                        """.formatted(userInput.getContent()));
    }

    @AchievesGoal(description = "A reviewed and polished blog post")
    @Action(description = "Review and improve the draft")
    public ReviewedPost reviewDraft(BlogDraft draft, Ai ai) {
        ReviewedPost reviewed = ai
                .withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .withPromptContributor(Personas.REVIEWER)
                .creating(ReviewedPost.class)
                .fromPrompt("""
                        You are a technical editor. Review and improve this blog post.

                        Title: %s
                        Content:
                        %s

                        Fix any technical errors. Thighten the writing.
                        Provide the revised title, revised content, and a brief summary of the changes you made as feedback.

                        Return ONLY valid JSON with this shape:
                        {"title":"...","content":"...","feedback":"..."}
                        Use \\n for line breaks inside "content".
                        Do not add any extra text or markdown code fences.

                        """.formatted(draft.title(), draft.content()));
        writeToFile(reviewed);
        return reviewed;
    }

    private void writeToFile(ReviewedPost post) {
        String filename = post.title()
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "")
                + ".md";

        Path outputDir = Path.of(properties.outputDir());
        Path filePath = outputDir.resolve(filename);

        try {
            Files.createDirectories(outputDir);
            Files.writeString(filePath, post.content());
            log.info("Blog post written to {}", filePath.toAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to write blog post to {}: {}", filePath, e.getMessage());
        }
    }

}
