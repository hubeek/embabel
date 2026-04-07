package nl.appall.testff.embabel.embabel;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "blog-agent")
public record BlogAgentProperties(String outputDir) {

    public BlogAgentProperties {
        if (outputDir == null || outputDir.isBlank()) {
            outputDir = "blog-posts";
        }
    }
}
