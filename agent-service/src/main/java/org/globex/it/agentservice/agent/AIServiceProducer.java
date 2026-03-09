package org.globex.it.agentservice.agent;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.function.UnaryOperator;

@ApplicationScoped
public class AIServiceProducer {

    @Inject
    @ModelName("routing")
    ChatModel chatModel;

    @Produces
    public RoutingIdentifyIntentAIService provideRoutingIdentifyIntentAIService() {
        return AiServices.builder(RoutingIdentifyIntentAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(unaryOperator(0.3))
                .build();
    }

    @Produces
    public RoutingHandleOtherRequestsAIService provideHandleOtherRequestsAIService() {
        return AiServices.builder(RoutingHandleOtherRequestsAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(unaryOperator(0.3))
                .build();
    }

    @Produces
    public RoutingAIService provideIdentifyIntentAIService() {
        return AiServices.builder(RoutingAIService.class)
                .chatModel(chatModel)
                .chatRequestTransformer(unaryOperator(0.1))
                .build();
    }

    UnaryOperator<ChatRequest> unaryOperator(Double temperature) {
        return chatRequest -> ChatRequest.builder()
                .temperature(temperature)
                .frequencyPenalty(chatRequest.frequencyPenalty())
                .messages(chatRequest.messages())
                .maxOutputTokens(chatRequest.maxOutputTokens())
                .modelName(chatRequest.modelName())
                .presencePenalty(chatRequest.presencePenalty())
                .responseFormat(chatRequest.responseFormat())
                .stopSequences(chatRequest.stopSequences())
                .toolChoice(chatRequest.toolChoice())
                .toolSpecifications(chatRequest.toolSpecifications())
                .topK(chatRequest.topK())
                .topP(chatRequest.topP())
                .build();
    }

}
