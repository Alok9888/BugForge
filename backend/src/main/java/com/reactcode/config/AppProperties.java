package com.reactcode.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Cors cors = new Cors();
    private final Mvp mvp = new Mvp();

    public Cors getCors() {
        return cors;
    }

    public Mvp getMvp() {
        return mvp;
    }

    public static class Cors {
        private String origins = "http://localhost:5173";

        public String getOrigins() {
            return origins;
        }

        public void setOrigins(String origins) {
            this.origins = origins;
        }
    }

    public static class Mvp {
        private int challengeLimit = 10;

        public int getChallengeLimit() {
            return challengeLimit;
        }

        public void setChallengeLimit(int challengeLimit) {
            this.challengeLimit = challengeLimit;
        }
    }
}
