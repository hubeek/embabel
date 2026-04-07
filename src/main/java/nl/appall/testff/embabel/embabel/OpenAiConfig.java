package nl.appall.testff.embabel.embabel;

import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class OpenAiConfig {

    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    @Bean
    @Primary
    public OpenAiApi openAiApi() {
        // Use base URL without /v1 - Spring AI appends it automatically
        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl("https://api.openai.com")
                .build();
    }
}
