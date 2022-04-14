package com.musicverse.server.api.auth;

import com.falsepattern.json.node.JsonNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.GETRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.sql.ResultSet;

public class Register extends GETRequestHandler {
    private static final String emailExistsQuery = Util.loadResource("/com/musicverse/server/sql/email_exists.sql");
    private static final String nicknameExistsQuery = Util.loadResource("/com/musicverse/server/sql/nickname_exists.sql");
    private static final String registerQuery = Util.loadResource("/com/musicverse/server/sql/register.sql");
    public Register(Database db) {
        super(db);
    }

    @Override
    public boolean handleGetRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/register".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val email = request.getString("email");
        val nickname = request.getString("nickname");

        boolean emailExists = db.query(emailExistsQuery, (ps) -> ps.setString(1, email), ResultSet::next);
        boolean nicknameExists = db.query(nicknameExistsQuery, (ps) -> ps.setString(1, nickname), ResultSet::next);

        if (emailExists || nicknameExists) {
            HttpHelper.respondWithErrorString(exchange, 400, "Email or Nickname already exists");
            return true;
        }

        //Do not store passwords in plaintext
        val passwordHash = Util.hashText(request.getString("password"));

        db.query(registerQuery, (ps) -> {
            ps.setString(1, nickname);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
        }, (rs) -> null);

        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
