package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class DeleteSong extends POSTRequestHandler {
    public DeleteSong(Database db) {
        super(db);
    }
    private static final String fromAlbum = Util.loadResource("/com/musicverse/server/sql/delete_from_album.sql");
    private static final String fromPlaylist = Util.loadResource("/com/musicverse/server/sql/delete_from_playlist.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/deleteSong".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val id = request.getInt("song_id");
        val collectionId = request.getInt("collection_id");
        val what = request.getInt("collection");

        if (what == 1){
            db.update(fromAlbum, (ps) -> { ps.setInt(1, id); ps.setInt(2, id);});

        } else if (what == 0){
            db.update(fromPlaylist, (ps) -> {
                ps.setInt(1, id);
                ps.setInt(2, collectionId);
                ;});
        }
        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
