package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.sql.ResultSet;

public class Register extends POSTRequestHandler {
    private static final String emailExistsQuery = Util.loadResource("/com/musicverse/server/sql/email_exists.sql");
    private static final String nicknameExistsQuery = Util.loadResource("/com/musicverse/server/sql/nickname_exists.sql");
    private static final String registerQuery = Util.loadResource("/com/musicverse/server/sql/register.sql");
    public Register(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/register".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val email = request.getString("email");
        val username = request.getString("username");
        val access_level = request.getInt("access_level");

        boolean emailExists = db.query(emailExistsQuery, (ps) -> ps.setString(1, email), ResultSet::next);
        boolean nicknameExists = db.query(nicknameExistsQuery, (ps) -> ps.setString(1, username), ResultSet::next);

        if (emailExists || nicknameExists) {
            HttpHelper.respondWithErrorString(exchange, 200, "Email or Nickname already exists");
            return true;
        }

        //Do not store passwords in plaintext
        val passwordHash = Util.hashText(request.getString("password"));

        db.update(registerQuery, (ps) -> {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setInt(4, access_level);
        });

        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
