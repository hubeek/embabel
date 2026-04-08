package nl.appall.testff.embabel.embabel;

import com.embabel.agent.spi.LlmService;
import com.embabel.common.ai.model.ModelProvider;
import com.embabel.common.ai.model.ModelSelectionCriteria;
import com.embabel.common.ai.model.NoSuitableModelException;
import com.embabel.common.ai.model.PricingModel;
import org.springframework.ai.chat.metadata.DefaultUsage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UsageMetadata {
    private long totalPromptTokens = 0;
    private long totalCompletionTokens = 0;
    private int totalCalls = 0;
    private final Map<String, ModelUsage> usageByModel = new LinkedHashMap<>();

    public void addUsage(String modelName, long promptTokens, long completionTokens, int calls) {
        if (modelName == null || modelName.isBlank()) {
            modelName = "unknown";
        }
        ModelUsage usage = usageByModel.computeIfAbsent(modelName, k -> new ModelUsage());
        usage.promptTokens += promptTokens;
        usage.completionTokens += completionTokens;
        usage.calls += Math.max(1, calls);
        totalPromptTokens += promptTokens;
        totalCompletionTokens += completionTokens;
        totalCalls += Math.max(1, calls);
    }

    public long getTotalPromptTokens() {
        return totalPromptTokens;
    }

    public long getTotalCompletionTokens() {
        return totalCompletionTokens;
    }

    public int getTotalCalls() {
        return totalCalls;
    }

    public List<String> getModelsUsed() {
        return usageByModel.keySet().stream().collect(Collectors.toList());
    }

    public double calculateCost(ModelProvider modelProvider) {
        double total = 0.0;
        for (Map.Entry<String, ModelUsage> entry : usageByModel.entrySet()) {
            total += costForModel(modelProvider, entry.getKey(), entry.getValue());
        }
        return total;
    }

    private double costForModel(ModelProvider modelProvider, String modelName, ModelUsage usage) {
        try {
            LlmService<?> llm = modelProvider.getLlm(ModelSelectionCriteria.byName(modelName));
            PricingModel pricing = llm.getPricingModel();
            if (pricing == null) {
                return 0.0;
            }
            return pricing.costOf(new DefaultUsage((int) usage.promptTokens, (int) usage.completionTokens));
        } catch (NoSuitableModelException e) {
            return 0.0;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Prompt tokens: %d, Completion tokens: %d, Calls: %d",
                totalPromptTokens,
                totalCompletionTokens,
                totalCalls
        );
    }

    private static final class ModelUsage {
        private long promptTokens = 0;
        private long completionTokens = 0;
        private int calls = 0;
    }
}
