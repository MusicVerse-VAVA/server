package com.musicverse.server.api;

import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class RequestHandler {
    protected final Database db;
    public abstract boolean handleRequest(HttpExchange exchange) throws Throwable;
}
