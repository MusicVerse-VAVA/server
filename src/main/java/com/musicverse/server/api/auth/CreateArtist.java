package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class CreateArtist  extends POSTRequestHandler {

    private static final String createArtist = Util.loadResource("/com/musicverse/server/sql/create_artist.sql");
    private static final String getUserId = Util.loadResource("/com/musicverse/server/sql/get_user_id.sql");

    public CreateArtist(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/create_artist".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val email = request.getString("user_email");
        int user_id;

        user_id =  db.query(getUserId,
                (ps) -> {
                    ps.setString(1, email);
                },
                (rs) -> {
                    if (!rs.next()){
                        return null;
                    } else {
                        return rs.getInt("id");
                    }
                });



        System.out.println(email);
        System.out.println(user_id);

        db.update(createArtist, (ps) -> ps.setInt(1, user_id));
        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
