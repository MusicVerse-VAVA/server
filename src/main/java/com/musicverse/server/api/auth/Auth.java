package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class Auth extends POSTRequestHandler {
    private static final String authQuery = Util.loadResource("/com/musicverse/server/sql/authenticate.sql");

    public Auth(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!url.equals("/auth")) return false;
        val request = HttpHelper.parseJSON(exchange);
        val email = request.getString("email");
        val password = Util.hashText(request.getString("password"));
        val user = db.query(authQuery, (ps) -> {
            ps.setString(1, email);
            ps.setString(2, password);
        }, (rs) -> {
            if (!rs.next()) {
                return null;
            } else {
                val result = new ObjectNode();
                result.set("username", rs.getString("nickname"));
                result.set("email", rs.getString("email"));
                result.set("accessLevel", rs.getInt("access_level"));
                result.set("id", rs.getInt("id"));
                return result;
            }
        });
        if (user != null) {
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("user", user);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 200, "Invalid email or password");
        }
        return true;
    }
}
