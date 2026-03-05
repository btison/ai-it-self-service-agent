package org.globex.it.agentservice.agent;

import io.quarkus.arc.Arc;
import io.quarkus.arc.InjectableBean;
import io.quarkus.arc.InstanceHandle;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.common.annotation.Identifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
public class AgentManager {

    private final Map<String, Agent> agents = new HashMap<>();

    public void initialize(@Observes StartupEvent event) {

        //use arc container to loop through all agent beans and build the map
        List<InstanceHandle<Agent>> agentHandles = Arc.container().listAll(Agent.class);
        for (InstanceHandle<Agent> handle : agentHandles) {
            if (handle.isAvailable()) {
                Agent agent = handle.get();
                InjectableBean<Agent> agentBean = handle.getBean();
                Set<Annotation> annotations = agentBean.getQualifiers();
                for (Annotation annotation : annotations) {
                    if (annotation.annotationType() == Identifier.class) {
                        try {
                            Method valueMethod = annotation.annotationType().getMethod("value");
                            String value = valueMethod.invoke(annotation).toString();
                            agents.put(value, agent);
                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            //ignore
                        }
                    }
                }
            }
        }
    }

    public Agent getAgent(String agentId) {
        return agents.get(agentId);
    }

    public Map<String, Agent> getAgents() {
        return agents;
    }

}
