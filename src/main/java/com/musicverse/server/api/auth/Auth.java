package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.GETRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class Auth extends GETRequestHandler {
    private static final String authQuery = Util.loadResource("/com/musicverse/server/sql/authenticate.sql");

    public Auth(Database db) {
        super(db);
    }

    @Override
    public boolean handleGetRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!url.equals("auth")) return false;
        val request = HttpHelper.parseJSON(exchange);
        val email = request.getString("email");
        val password = Util.hashText(request.getString("password"));
        String username = db.query(authQuery, (ps) -> {
            ps.setString(1, email);
            ps.setString(2, password);
        }, (rs) -> {
            if (!rs.next()) {
                return null;
            } else {
                return rs.getString("nickname");
            }
        });
        if (username != null) {
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("username", username);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 403, "Invalid email or password");
        }
        return true;
    }
}
