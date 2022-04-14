package com.musicverse.server;

import com.falsepattern.json.node.JsonNode;
import com.falsepattern.json.node.ObjectNode;
import com.falsepattern.json.node.StringNode;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpHelper {
    private static final Map<Integer, String> errorMessages = new HashMap<>();
    static {
        val in = HttpHelper.class.getResourceAsStream("/com/musicverse/server/error_codes.csv");
        if (in != null)
            try {
                val str = new String(in.readAllBytes());
                val lines = str.split("\n");
                for (val line: lines) {
                    val parts = line.split(",");
                    try {
                        errorMessages.put(Integer.parseInt(parts[0]), parts[1]);
                    } catch (NumberFormatException ignored){}
                }
            } catch (IOException ignored) {}
    }

    public static JsonNode parseJSON(HttpExchange exchange) throws IOException {
        val contents = exchange.getRequestBody().readAllBytes();
        return JsonNode.parse(new String(contents));
    }

    public static void respondWithJson(HttpExchange exchange, int code, JsonNode json) throws IOException {
        val resp = json.prettyPrint(4).getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, resp.length);
        exchange.getResponseBody().write(resp);
        exchange.close();
    }

    public static void respondWithOk(HttpExchange exchange) throws IOException {
        val json = new ObjectNode();
        json.set("status", "ok");
        respondWithJson(exchange, 200, json);
    }

    public static void respondWithErrorJson(HttpExchange exchange, int code, String type, JsonNode details) throws IOException {
        respondWithJson(exchange, code, makeErrorResponse(type, details));
    }

    public static void respondWithErrorString(HttpExchange exchange, int code, String type, String details) throws IOException {
        respondWithErrorJson(exchange, code, type, StringNode.of(details));
    }

    public static void respondWithErrorJson(HttpExchange exchange, int code, JsonNode details) throws IOException {
        respondWithJson(exchange, code, makeErrorResponse(errorMessages.getOrDefault(code, "UNKNOWN"), details));
    }

    public static void respondWithErrorString(HttpExchange exchange, int code, String details) throws IOException {
        respondWithErrorJson(exchange, code, StringNode.of(details));
    }

    public static void respondException(HttpExchange exchange, int code, Exception exception) throws IOException {
        respondWithJson(exchange, code, makeErrorResponse("internal_error", exceptionToJson(exception)));
    }

    private static ObjectNode makeErrorResponse(String type, JsonNode details) {
        val response = new ObjectNode();
        val err = new ObjectNode();
        err.set("type", type);
        if (details != null) err.set("details", details); else err.setNull("details");
        response.set("error", err);
        response.set("status", "error");
        return response;
    }

    private static ObjectNode exceptionToJson(Throwable e) {
        val result = new ObjectNode();
        result.set("type", e.getClass().getName());
        result.set("message", e.getMessage());
        val cause = e.getCause();
        if (cause != null) {
            result.set("cause", exceptionToJson(cause));
        } else {
            result.setNull("cause");
        }
        return result;
    }
}
