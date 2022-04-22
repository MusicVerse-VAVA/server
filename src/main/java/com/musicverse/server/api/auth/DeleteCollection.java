package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class DeleteCollection extends POSTRequestHandler {
    public DeleteCollection(Database db) {
        super(db);
    }

    private static final String deletePlaylist = Util.loadResource("/com/musicverse/server/sql/delete_playlist.sql");
    private static final String deleteAlbum = Util.loadResource("/com/musicverse/server/sql/delete_album.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/deleteCollection".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val collectionId = request.getInt("collection_id");
        val what = request.getInt("collection");

        if (what == 1){
            db.update(deleteAlbum, (ps) -> {
                ps.setInt(1, collectionId); //delete songs from playlists
                ps.setInt(2, collectionId); //delete songs itself
                ps.setInt(3, collectionId); //delete album itself
            } );

        } else if (what == 0){
            db.update(deletePlaylist, (ps) -> {
                ps.setInt(1, collectionId);
                ps.setInt(2, collectionId);
            });
        }
        HttpHelper.respondWithOk(exchange);
        return true;
    }

}
