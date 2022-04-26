package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class UpdateUser extends POSTRequestHandler {
    public UpdateUser(Database db) {
        super(db);
    }


    private static final String updateUserAccessLevel = Util.loadResource("/com/musicverse/server/sql/update_user_access_level.sql");
    private static final String updateArtistStatus = Util.loadResource("/com/musicverse/server/sql/update_artist_access_level.sql");

    private static final String getPassword = Util.loadResource("/com/musicverse/server/sql/get_user_password.sql");

    private static final String updatePswd = Util.loadResource("/com/musicverse/server/sql/update_user_password.sql");

    private static final String updateNickName = Util.loadResource("/com/musicverse/server/sql/update_user_nickname.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/updateUser".equals(url) && !"/updateUserSettings".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        if ("/updateUser".equals(url)) {
            val user_id = request.getInt("user_id");
            val status_id = request.getInt("status_id");
            val process = request.getInt("process");

            if (process == 0) {
                db.update(updateUserAccessLevel,
                        (ps) -> {
                            ps.setInt(2, user_id);
                            ps.setInt(1, status_id);
                        });
            } else {
                db.update(updateArtistStatus,
                        (ps) -> {
                            ps.setInt(2, user_id);
                            ps.setInt(1, status_id);
                        });
            }

            val response = new ObjectNode();
            response.set("status", "ok");
            HttpHelper.respondWithJson(exchange, 200, response);
            return true;

        }else if ("/updateUserSettings".equals(url)) {
            val user_id = request.getInt("user_id");
            val new_password = request.getString("new_password");
            val nickname = request.getString("nickname");
            val old_password = request.getString("old_password");

            val password = Util.hashText(request.getString("old_password"));
            System.out.println(password);

            val currentPswd = db.query(getPassword,
                    (ps) -> {
                        ps.setInt(1, user_id);
                    },
                    (rs) -> {
                        if (!rs.next()) {
                            return "null";
                        } else {
                            return rs.getString("password");
                        }
                    });
            System.out.println(currentPswd);
            if (Objects.equals(currentPswd, password)){
                System.out.println(new_password);
                System.out.println(nickname);
                System.out.println(user_id);
                if (new_password.getBytes(StandardCharsets.UTF_8).length > 0){
                    db.update(updatePswd, (ps) -> {
                        ps.setInt(2, user_id);
                        ps.setString(1, Util.hashText(new_password));
                    });
                }
                if (nickname.getBytes(StandardCharsets.UTF_8).length > 0){
                    db.update(updateNickName, (ps) -> {
                        ps.setInt(2, user_id);
                        ps.setString(1, nickname);
                    });

                }
            }
            val response = new ObjectNode();
            response.set("status", "ok");
            if (Objects.equals(currentPswd, password))
                response.set("valid", 1);
            else
                response.set("valid", 0);
            HttpHelper.respondWithJson(exchange, 200, response);
        }
        return true;
    }
}
