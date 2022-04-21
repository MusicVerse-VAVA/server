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

public class LoadArtistByUser extends POSTRequestHandler {

    private static final String loadArtistByUser = Util.loadResource("/com/musicverse/server/sql/load_artist_by_user.sql");
    private static final String loadAlbums = Util.loadResource("/com/musicverse/server/sql/load_albums_of_artist.sql");
    private static final String loadGenre = Util.loadResource("/com/musicverse/server/sql/load_genre_of_artist.sql");
    private static final String loadArtistByArtist = Util.loadResource("/com/musicverse/server/sql/load_artist_by_artist.sql");

    public LoadArtistByUser(Database db){
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/loadArtistByUser".equals(url) && !"/loadArtistByArtist".equals(url)) return false;
            val request = HttpHelper.parseJSON(exchange);
            val id = request.getInt("id");

            val artist = db.query("/loadArtistByUser".equals(url) ? loadArtistByUser : loadArtistByArtist,
                    (ps) -> {
                        ps.setInt(1, id);
                    },
                    (rs) -> {
                        if (!rs.next()) {
                            return null;
                        } else {
                            val result = new ObjectNode();
                            result.set("id", rs.getInt("id"));
                            result.set("name", rs.getString("name"));
                            result.set("description", rs.getString("description"));
                            result.set("user_id", rs.getInt("user_id"));
                            result.set("genre_id", rs.getInt("genre_id"));
                            result.set("avatar", rs.getString("avatar"));
                            result.set("status", rs.getInt("status"));
                            return result;
                        }
                    });

            val albums = db.query(loadAlbums,
                    (ps) -> {
                        ps.setInt(1, artist.getInt("id"));
                    },
                    (rs) -> {
                        val listOfAlbums = new ListNode();
                        while (rs.next()) {
                            val record = new ObjectNode();
                            record.set("id", rs.getInt("id"));
                            record.set("name", rs.getString("name"));
                            record.set("description", rs.getString("description"));
                            record.set("artist_id", rs.getInt("artist_id"));
                            listOfAlbums.add(record);
                        }
                        ;
                        return listOfAlbums;
                    });

            val genre = db.query(loadGenre,
                    (ps) -> {
                        ps.setInt(1, artist.getInt("genre_id"));
                    },
                    (rs) -> {
                        if (!rs.next()) {
                            return null;
                        } else {
                            return rs.getString("genre");
                        }
                    });

            artist.set("genre", String.valueOf(genre));
            artist.set("albums", albums);

            if (artist != null) {
                val response = new ObjectNode();
                response.set("status", "ok");
                response.set("artist", artist);
                HttpHelper.respondWithJson(exchange, 200, response);
            } else {
                HttpHelper.respondWithErrorString(exchange, 500, "internal error");
            }

        return true;
    }
}
