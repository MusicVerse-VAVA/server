package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class UpdateUser extends POSTRequestHandler {
    public UpdateUser(Database db) {
        super(db);
    }


    private static final String updateUserAccessLevel = Util.loadResource("/com/musicverse/server/sql/update_user_access_level.sql");
    private static final String updateArtistStatus = Util.loadResource("/com/musicverse/server/sql/update_artist_access_level.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/updateUser".equals(url)) return false;

        val request = HttpHelper.parseJSON(exchange);
        val user_id = request.getInt("user_id");
        val status_id = request.getInt("status_id");
        val process = request.getInt("process");

        if (process == 0){
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

    }
}
