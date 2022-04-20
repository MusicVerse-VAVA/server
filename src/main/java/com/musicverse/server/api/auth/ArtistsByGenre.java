package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.util.ArrayList;

public class ArtistsByGenre extends POSTRequestHandler {

    private static final String loadArtistsByGenre = Util.loadResource("/com/musicverse/server/sql/load_artists_by_genre.sql");

    public ArtistsByGenre(Database db){
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/artistsByGenre".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val genreId = request.getInt("genre_id");

        val artists = db.query(loadArtistsByGenre,
                (ps) -> {
                    ps.setInt(1, genreId);
                },
                (rs) -> {
                    val filteredArtists = new ListNode();
                    while(rs.next()){
                            val result = new ObjectNode();
                            result.set("id", rs.getInt("id"));
                            result.set("name", rs.getString("name"));
                            result.set("description", rs.getString("description"));
                            result.set("user_id", rs.getInt("user_id"));
                            result.set("genre_id", rs.getInt("genre_id"));
                            result.set("avatar", rs.getString("avatar"));
                            result.set("status", rs.getInt("status"));
                            filteredArtists.add(result);
                    }
                    return filteredArtists;
                });

        if (artists != null){
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("artists", artists);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 500, "internal error");
        }
        return true;
    }
}
