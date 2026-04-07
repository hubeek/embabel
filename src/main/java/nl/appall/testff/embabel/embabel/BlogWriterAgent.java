package nl.appall.testff.embabel.embabel;


import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.Ai;
import com.embabel.agent.domain.io.UserInput;
import lombok.extern.slf4j.Slf4j;

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
                .withDefaultLlm()
                .withId("blog-post-draft-writer")
                .creating(BlogDraft.class)
                .fromPrompt("""
                        You are a software developer and educator writing a blog post.
                        Write a blog post about: %s
                        
                        Keep it practical and beginner friendly.
                        Use short sentences and plain language.
                        Include code examples but keep them short and simple.
                        Write the content in Markdown.
                        
                        """.formatted(userInput.getContent()));
    }

    @AchievesGoal(description = "A reviewed and polished blog post.")
    @Action(description = "Review and improve draft.")
    public ReviewedPost reviewedDraft(BlogDraft draft, Ai ai) {
        ReviewedPost reviewed =  ai.withLlmByRole("reviewer")
                .withId("blog-post-reviewer")
                .creating(ReviewedPost.class)
                .fromPrompt("""
                        You are a technical editor. Review and improve this blog post.
                        
                        Titel: %s
                        Content: 
                        %s
                        
                        Fix any technical errors. Thighten the writing.
                        Provide the revised title, revised content,and a brief summary of the changes you made as feedback.
                        
                        """.formatted(draft.title(), draft.content()));
        log.info("reviewed ::  {}",reviewed);
        return reviewed;
    }

}
