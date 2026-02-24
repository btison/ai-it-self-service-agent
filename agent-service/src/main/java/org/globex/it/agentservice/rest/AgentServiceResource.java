package org.globex.it.agentservice.rest;

import io.cloudevents.CloudEvent;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.logging.Log;
import org.globex.it.agentservice.model.CloudEventType;
import org.globex.it.agentservice.service.AgentService;
import org.globex.it.agentservice.utils.CloudEventUtils;

import java.util.Map;

@Path("/api/v1/events/cloudevents")
public class AgentServiceResource {

    @Inject
    AgentService agentService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> handleCloudEvent(CloudEvent cloudEvent) {

        if (cloudEvent == null || cloudEvent.getData() == null) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).entity("Invalid data received. Null or empty event").build());
        }
        return Uni.createFrom().item(() -> cloudEvent).emitOn(Infrastructure.getDefaultWorkerPool())
                .onItem().transform(e -> {
                    Log.infof("CloudEvent received: %s", e);
                    String eventType = e.getType();
                    if (eventType.equals(CloudEventType.REQUEST_CREATED.getType())) {
                        return agentService.handleRequestEvent(e).toJson();
                    } else {
                        return CloudEventUtils.createCloudEventResponse("ignored", "Unhandled event type", Map.of("eventType", eventType));
                    }
                })
                .onItem().transform(json -> Response.status(Response.Status.OK).entity(json.toString()).build())
                .onFailure().recoverWithItem(throwable -> {
                    Log.error("Failed to handle request", throwable);
                    return Response.serverError().build();
                });
    }
}
