package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class CreateCollection extends POSTRequestHandler {
    public CreateCollection(Database db) {
        super(db);
    }

    private static final String createPlaylist = Util.loadResource("/com/musicverse/server/sql/create_playlist.sql");
    private static final String createAlbum = Util.loadResource("/com/musicverse/server/sql/create_album.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/createCollection".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val id = request.getInt("id");
        val what = request.getInt("collection");
        val name = request.getString("name");
        val description = request.getString("description");

        if (what == 1){
            db.update(createAlbum, (ps) -> {
                ps.setInt(1, id); //delete songs from playlists
                ps.setString(2, name); //delete songs itself
                ps.setString(3, description); //delete album itself
            } );

        } else if (what == 0){
            db.update(createPlaylist, (ps) -> {
                ps.setInt(1, id);
                ps.setString(2, name);
                ps.setString(3, description);
            });
        }
        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
