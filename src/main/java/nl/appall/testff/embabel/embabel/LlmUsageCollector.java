package nl.appall.testff.embabel.embabel;

import com.embabel.agent.api.tool.callback.AfterLlmCallContext;
import com.embabel.agent.api.tool.callback.ToolLoopInspector;
import com.embabel.agent.core.Usage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public final class LlmUsageCollector implements ToolLoopInspector {

    private final AtomicLong promptTokens = new AtomicLong();
    private final AtomicLong completionTokens = new AtomicLong();
    private final AtomicInteger calls = new AtomicInteger();

    @Override
    public void afterLlmCall(AfterLlmCallContext context) {
        calls.incrementAndGet();
        Usage usage = context.getUsage();
        if (usage == null) {
            return;
        }
        Integer prompt = usage.getPromptTokens();
        Integer completion = usage.getCompletionTokens();
        if (prompt != null) {
            promptTokens.addAndGet(prompt);
        }
        if (completion != null) {
            completionTokens.addAndGet(completion);
        }
    }

    public UsageSnapshot snapshot() {
        return new UsageSnapshot(promptTokens.get(), completionTokens.get(), calls.get());
    }
}
