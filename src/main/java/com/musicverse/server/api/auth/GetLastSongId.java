package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.sql.PreparedStatement;

public class GetLastSongId extends POSTRequestHandler {

    public GetLastSongId(Database db) {
        super(db);
    }

    private static final String getLatSongId = Util.loadResource("/com/musicverse/server/sql/get_last_song_id.sql");
    private static final String createSong = Util.loadResource("/com/musicverse/server/sql/create_song.sql");

    private static void process(PreparedStatement ps) {
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
            if (!"/lastSongId".equals(url)) return false;

            val request = HttpHelper.parseJSON(exchange);
            val album_id = request.getInt("album_id");
            val artist_id = request.getInt("artist_id");
            val name = request.getString("name");
            val duration = request.getInt("duration");
            val genre_id = request.getInt("genre_id");

            int t;
            ObjectNode lastId = null;

             t = db.update(createSong,
                    (ps) -> {
                        ps.setInt(1, artist_id);
                        ps.setString(2, name);
                        ps.setInt(3, album_id);
                        ps.setInt(4, genre_id);
                        ps.setString(5, "n");
                        ps.setInt(6, duration);
                        ps.setString(7, "n");
                        ps.setString(8, "n");
                    });

            if (t == 1)
                lastId = db.query(getLatSongId,
                        GetLastSongId::process,
                        (rs) -> {
                            val id = rs.next();
                            if (id){
                                val result = new ObjectNode();
                                result.set("id", rs.getInt("id"));
                                return result;
                            } else {
                                val result = new ObjectNode();
                                result.set("id", 1);
                                return result;
                            }
                        });

            if (lastId != null) {
                val response = new ObjectNode();
                response.set("status", "ok");
                response.set("id", lastId.getInt("id"));
                HttpHelper.respondWithJson(exchange, 200, response);
            } else {
                HttpHelper.respondWithErrorString(exchange, 500, "Could not get data");
            }

            return true;
    }
}