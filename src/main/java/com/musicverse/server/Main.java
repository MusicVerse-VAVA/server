package com.musicverse.server;

import com.falsepattern.json.node.JsonNode;
import com.musicverse.server.api.Api;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpServer;
import lombok.Cleanup;
import lombok.val;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;

public class Main {
    public static final Semaphore sem = new Semaphore(0);

    private static void runServer(JsonNode serverConfig, Database db) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(serverConfig.getString("host"), serverConfig.getInt("port")), 0);
        val api = new Api(db);
        server.createContext("/", (exchange -> {
            System.out.println(exchange.getRequestMethod() + ": " + exchange.getRequestURI().toString());
            try {
                if (!api.handle(exchange)) {
                    HttpHelper.respondWithErrorString(exchange, 404, "Not found");
                }
            } catch (Exception e) {
                HttpHelper.respondException(exchange, 500, e);
            }
        }));
        server.start();
        while (true) {
            try {
                sem.acquire();
                break;
            } catch (InterruptedException ignored) {}
        }
        server.stop(Integer.MAX_VALUE);
    }

    public static void main(String[] args) {
        final JsonNode secrets;
        try {
            secrets = JsonNode.parse(Files.readString(Path.of("./secrets.json")));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read secrets file", e);
        }
        @Cleanup
        val database = new Database(secrets.get("database"));
        try {
            runServer(secrets.get("server"), database);
        } catch (IOException e) {
            System.err.println("Failed to initialize server");
            e.printStackTrace();
        }
    }
}
