package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.GETRequestHandler;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.sql.ResultSet;

public class Playlists extends POSTRequestHandler {

    private static final String getPlaylistsQuery = Util.loadResource("/com/musicverse/server/sql/getPlaylists.sql");

    public Playlists(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/playlists".equals(url)) return false;
        val request = HttpHelper.parseJSON(exchange);
        val id = request.getInt("id");

        val playlists = db.query(getPlaylistsQuery, (ps) -> {
            ps.setInt(1,id);
        }, (rs) -> {
            val allPlaylists = new ListNode();

            while (rs.next()){
                val playlist = new ObjectNode();
                playlist.set("name", rs.getString("name"));
                playlist.set("id", rs.getInt("id"));
                playlist.set("description", rs.getString("description"));
                playlist.set("private",rs.getInt("private"));
                allPlaylists.add(playlist);
            }
            return allPlaylists;
        });

        if (playlists != null) {
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("playlists", playlists);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 200, "Invalid id");
        }

        return false;
    }

}
