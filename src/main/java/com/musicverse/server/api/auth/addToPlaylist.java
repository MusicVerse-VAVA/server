package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class addToPlaylist extends POSTRequestHandler {
    public addToPlaylist(Database db) {
        super(db);
    }

    private static final String addToPlaylist = Util.loadResource("/com/musicverse/server/sql/add_to_playlist.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/addToPlaylist".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val collectionId = request.getInt("collection_id");
        val songId = request.getInt("song_id");

        db.update(addToPlaylist, (ps) -> {
            ps.setInt(1, collectionId);
            ps.setInt(2, songId);
        });
        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
