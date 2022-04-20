package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class SongsByPlaylist extends POSTRequestHandler {

    private static final String loadSongsByPlaylist = Util.loadResource("/com/musicverse/server/sql/load_songs_by_playlist.sql");

    public SongsByPlaylist(Database db){
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/songsByPlaylist".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val playlistId = request.getInt("playlist_id");

        val songs = db.query(loadSongsByPlaylist,
                (ps) -> {
                    ps.setInt(1, playlistId);
                },
                (rs) -> {
                    val filteredSongs = new ListNode();
                    while(rs.next()){
                        val result = new ObjectNode();
                        result.set("id", rs.getInt("id"));
                        result.set("name", rs.getString("name"));
                        result.set("description", rs.getString("description"));
                        result.set("user_id", rs.getInt("user_id"));
                        result.set("album_id",rs.getInt("album"));
                        result.set("duration",rs.getInt("duration"));
                        result.set("data",rs.getString("data"));
                        result.set("image",rs.getString("image"));
                        filteredSongs.add(result);
                    }
                    return filteredSongs;
                });

        if (songs != null){
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("songs", songs);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 500, "internal error");
        }
        return true;
    }
}

