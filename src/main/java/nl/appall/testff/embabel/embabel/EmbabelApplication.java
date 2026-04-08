package nl.appall.testff.embabel.embabel;

import com.embabel.agent.api.common.Ai;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.event.EventListener;

@EnableConfigurationProperties({BlogAgentProperties.class, ModelsProperties.class})
@SpringBootApplication
public class EmbabelApplication {

    private final Ai ai;

    public EmbabelApplication(Ai ai) {
        this.ai = ai;
    }

    public static void main(String[] args) {
        SpringApplication.run(EmbabelApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void verifyLlmConfigurationOnStartup() {
        // Fail fast if no default LLM is configured or it can't be resolved.
        ai.withDefaultLlm();
        // Fail fast if the role-based LLM used by the app can't be resolved.
        ai.withLlmByRole("reviewer");
    }

}
