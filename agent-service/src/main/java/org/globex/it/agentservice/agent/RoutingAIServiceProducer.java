package org.globex.it.agentservice.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

@ApplicationScoped
public class RoutingAIServiceProducer {

    @Inject
    @ModelName("routing")
    ChatModel chatModel;

    @Produces
    public RoutingIdentifyIntentAIService provideRoutingIdentifyIntentAIService() {
        return AiServices.builder(RoutingIdentifyIntentAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(0.3))
                .build();
    }

    @Produces
    public RoutingHandleOtherRequestsAIService provideHandleOtherRequestsAIService() {
        return AiServices.builder(RoutingHandleOtherRequestsAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(0.3))
                .build();
    }

    @Produces
    public RoutingAIService provideIdentifyIntentAIService() {
        return AiServices.builder(RoutingAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(ChatRequestTransformer.overrideTemperature(0.1))
                .build();
    }

}
