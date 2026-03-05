package org.globex.it.agentservice.graph;

import java.util.ArrayList;
import java.util.List;

public record TransitionCondition (

        List<String> triggerPhrases,
        List<String> excludePhrases,
        List<BaseAction> actions,
        String transition
) {

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private List<String> triggerPhrases;
        private List<String> excludePhrases;
        private List<BaseAction> actions;
        private String transition;

        private Builder() {}

        public Builder triggerPhrases(List<String> triggerPhrases) {
            this.triggerPhrases = triggerPhrases == null ? null : new ArrayList<>(triggerPhrases);
            return this;
        }

        public Builder excludePhrases(List<String> excludePhrases) {
            this.excludePhrases = excludePhrases == null ? null : new ArrayList<>(excludePhrases);
            return this;
        }

        public Builder actions(List<BaseAction> actions) {
            this.actions = actions == null ? null : new ArrayList<>(actions);
            return this;
        }

        public Builder addActions(BaseAction action) {
            if (this.actions == null) {
                this.actions = new ArrayList<>();
            }
            actions.add(action);
            return this;
        }

        public Builder transition(String transition) {
            this.transition = transition;
            return this;
        }

        public TransitionCondition build() {
            return new TransitionCondition(triggerPhrases == null? new ArrayList<>() : triggerPhrases,
                    excludePhrases == null? new ArrayList<>() : excludePhrases,
                    actions == null? new ArrayList<>() : actions, transition);
        }
    }
}
