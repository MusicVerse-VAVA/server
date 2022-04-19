package com.musicverse.server.api.auth;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class EditArtist extends POSTRequestHandler {

    private static final String editArtist = Util.loadResource("/com/musicverse/server/sql/edit_artist.sql");
    private static final String getGenre = Util.loadResource("/com/musicverse/server/sql/get_genre.sql");

    public EditArtist(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {

        if (!"/editArtist".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val id = request.getInt("id");
        val name = request.getString("name");
        val description = request.getString("description");
        val genre = request.getString("genre");
        int genreId;

        genreId = db.query(getGenre,
                (ps) -> {
                    ps.setString(1, genre);
                },
                (rs) -> {
                    if (!rs.next())
                        return null;
                    else
                        return rs.getInt("id");
                });

        db.update(editArtist, (ps) -> {
            ps.setInt(4, id);
            ps.setString(1, name);
            ps.setString(2, description);
            ps.setInt(3, genreId)
            ;});

        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
