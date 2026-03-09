package org.globex.it.agentservice.graph;

import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.bsc.langgraph4j.*;
import org.bsc.langgraph4j.action.*;
import org.bsc.langgraph4j.checkpoint.BaseCheckpointSaver;
import org.bsc.langgraph4j.checkpoint.PostgresSaver;
import org.bsc.langgraph4j.utils.EdgeMappings;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.globex.it.agentservice.agent.RoutingAIService;
import org.globex.it.agentservice.agent.RoutingHandleOtherRequestsAIService;
import org.globex.it.agentservice.agent.RoutingIdentifyIntentAIService;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

import static org.bsc.langgraph4j.GraphDefinition.END;
import static org.bsc.langgraph4j.GraphDefinition.START;

@ApplicationScoped
public class RoutingGraphProducer {

    @Inject
    RoutingAIService routingAgent;

    @Inject
    RoutingIdentifyIntentAIService identifyIntentAgent;

    @Inject
    RoutingHandleOtherRequestsAIService handleOtherRequestsAgent;

    @ConfigProperty(name = "postgresql.host")
    String postgresqlHost;

    @ConfigProperty(name = "postgresql.port", defaultValue = "5432")
    Integer postgresqlPort;

    @ConfigProperty(name = "postgresql.username")
    String postgresqlUsername;

    @ConfigProperty(name = "postgresql.password")
    String postgresqlPassword;

    @ConfigProperty(name = "postgresql.database")
    String postgresqlDatabase;

    @Produces @Identifier("routing-agent")
    public CompiledGraph<State> buildGraph() throws Exception {
        return compileGraph();
    }

    @Produces @Identifier("routing-agent")
    public StateMachine nodes() {
        return new StateMachine().addGraphState(new GraphState("greet_and_identify_need", "llm_processor", true, false, null))
                .addGraphState(new GraphState("handle_other_request", "llm_processor", false, false, null))
                .addGraphState(new GraphState("classify_user_intent", "llm_processor", false, false, null))
                .addGraphState(new GraphState("waiting_user_need", "waiting", false, false, null))
                .addGraphState(new GraphState("end", "terminal", false, true, null))
                .addBusinessField(new BusinessField("routing_decision", String.class, null));
    }

    CommandAction<State> identifyNeedCommandAction = LlmProcessorCommandAction.get("greet_and_identify_need",
            "waiting_user_need", new Function<>() {
                @Override
                public String apply(String input) {
                    return identifyIntentAgent.greetAndIdentifyNeed(input);
                }
            });

    CommandAction<State> handleOtherRequestCommandAction = LlmProcessorCommandAction.get("handle_other_request",
            "waiting_user_need", new Function<>() {
                @Override
                public String apply(String input) {
                    return handleOtherRequestsAgent.handleOtherRequest(input);
                }
            });

    List<TransitionCondition> conditions = List.of(
            TransitionCondition.builder()
                    .triggerPhrases(List.of("LAPTOP_REFRESH"))
                    .actions(List.of(
                            new SetFieldAction("routing_decision", "laptop-refresh"),
                            new AddMessageAction("laptop-refresh")))
                    .transition("end")
                    .build(),
            TransitionCondition.builder()
                    .triggerPhrases(List.of("EMAIL_CHANGE"))
                    .actions(List.of(
                            new SetFieldAction("routing_decision", "email-change"),
                            new AddMessageAction("email-change")))
                    .transition("end")
                    .build(),
            TransitionCondition.builder()
                    .triggerPhrases(List.of("OTHER"))
                    .transition("handle_other_request")
                    .build()
    );

    CommandAction<State> classifyIntentCommandAction = LlmProcessorCommandAction.get("classify_user_intent",
            "handle_other_request", conditions, new Function<>() {
                @Override
                public String apply(String input) {
                    return routingAgent.classifyUserIntent(input);
                }
            });

    CommandAction<State> waitingUserNeedCommandAction = WaitingCommandAction.get("waiting_user_need", "classify_user_intent");

    NodeAction<State> end = TerminalNodeAction.get("end");

    NodeAction<State> wait = WaitingNodeAction.get();

    CommandAction<State> resumeDispatcher = ResumeDispatcherCommandAction.get("greet_and_identify_need");

    private CompiledGraph<State> compileGraph() throws GraphStateException {
        StateGraph<State> graph = new StateGraph<>(State::new)
                .addNode("greet_and_identify_need", AsyncCommandAction.command_async(identifyNeedCommandAction), EdgeMappings.builder()
                        .to("waiting_user_need")
                        .build())
                .addNode("handle_other_request", AsyncCommandAction.command_async(handleOtherRequestCommandAction), EdgeMappings.builder()
                        .to("waiting_user_need")
                        .build())
                .addNode("classify_user_intent", AsyncCommandAction.command_async(classifyIntentCommandAction), EdgeMappings.builder()
                        .to(List.of("end", "handle_other_request"))
                        .build())
                .addNode("waiting_user_need", AsyncCommandAction.command_async(waitingUserNeedCommandAction), EdgeMappings.builder()
                        .to(List.of("classify_user_intent", "wait"))
                        .build())
                .addNode("wait", AsyncNodeAction.node_async(wait))
                .addNode("end", AsyncNodeAction.node_async(end))
                .addNode("_resume_dispatcher_", AsyncCommandAction.command_async(resumeDispatcher), EdgeMappings.builder()
                        .to(List.of("greet_and_identify_need", "handle_other_request", "classify_user_intent", "waiting_user_need", "end"))
                        .build())
                .addEdge(START, "_resume_dispatcher_")
                .addEdge("end", END);

        BaseCheckpointSaver saver;
        try {
            saver = PostgresSaver.builder()
                    .host(postgresqlHost)
                    .port(postgresqlPort)
                    .user(postgresqlUsername)
                    .password(postgresqlPassword)
                    .database(postgresqlDatabase)
                    .stateSerializer( graph.getStateSerializer())
                    .createTables(false)
                    .dropTablesFirst(false)
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        var compileConfig = CompileConfig.builder()
                .checkpointSaver(saver)
                .interruptBefore("wait")
                .build();
        return graph.compile(compileConfig);
    }

}
