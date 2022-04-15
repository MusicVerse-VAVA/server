package com.musicverse.server.api;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.api.auth.Auth;
import com.musicverse.server.api.auth.Playlists;
import com.musicverse.server.api.auth.Register;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class Api {
    private final List<RequestHandler> handlers = new ArrayList<>();
    public Api(Database db) {
        handlers.add(new Register(db));
        handlers.add(new Auth(db));
        handlers.add(new Playlists(db));
    }

    @SneakyThrows
    public boolean handle(HttpExchange exchange) {
        try {
            for (val handler : handlers) {
                if (handler.handleRequest(exchange)) {
                    return true;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            HttpHelper.respondWithErrorString(exchange, 500, "Internal server error");
            return true;
        }
        return false;
    }
}
