package org.globex.it.agentservice.utils;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import java.util.Map;

public class CloudEventUtils {

    public static JsonObject createCloudEventResponse(String status, String message, Map<String, String> details) {

        return Json.createObjectBuilder()
                .add("status", status)
                .add("message", message)
                .add("details", Json.createObjectBuilder(details).build())
                .build();

    }
}
