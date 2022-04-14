package com.musicverse.server.api;

import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public abstract class GETRequestHandler extends RequestHandler{
    public GETRequestHandler(Database db) {
        super(db);
    }

    @Override
    public boolean handleRequest(HttpExchange exchange) throws Throwable {
        if (!exchange.getRequestMethod().equals("GET")) return false;
        val uri = exchange.getRequestURI().toString();
        String baseUri;
        String params;
        if (uri.indexOf('?') > 0) {
            baseUri = uri.substring(0, uri.indexOf('?'));
            params = uri.substring(uri.indexOf('?') + 1);
        } else {
            baseUri = uri;
            params = "";
        }
        if (baseUri.endsWith("/")) baseUri = baseUri.substring(0, baseUri.length() - 1);
        return handleGetRequest(baseUri, params, exchange);
    }


    public abstract boolean handleGetRequest(String url, String params, HttpExchange exchange) throws Throwable;
}
