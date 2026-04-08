package nl.appall.testff.embabel.embabel;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "embabel.models")
public class ModelsProperties {

    private String defaultLlm = "claude-opus-4-6";
    private Llms llms = new Llms();

    public String getDefaultLlm() {
        return defaultLlm;
    }

    public void setDefaultLlm(String defaultLlm) {
        this.defaultLlm = defaultLlm != null ? defaultLlm : "claude-opus-4-6";
    }

    public Llms getLlms() {
        return llms;
    }

    public void setLlms(Llms llms) {
        this.llms = llms;
    }

    public static class Llms {
        private String reviewer = "claude-opus-4-6";

        public String getReviewer() {
            return reviewer;
        }

        public void setReviewer(String reviewer) {
            this.reviewer = reviewer != null ? reviewer : "claude-opus-4-6";
        }
    }
}
