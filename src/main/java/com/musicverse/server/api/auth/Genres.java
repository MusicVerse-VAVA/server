package com.musicverse.server.api.auth;

import com.musicverse.server.Util;
import com.musicverse.server.db.Database;
import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.api.GETRequestHandler;
import com.musicverse.server.api.POSTRequestHandler;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class Genres extends POSTRequestHandler {

    private static final String getGenresQuery = Util.loadResource("/com/musicverse/server/sql/getGenres.sql");

    public Genres(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/genres".equals(url)) return false;

        val genres = db.query(getGenresQuery,  (ps) -> {},
                (rs) -> {
            val allGenres = new ListNode();
            while (rs.next()){
                val genre = new ObjectNode();
                genre.set("id", rs.getInt("id"));
                genre.set("genre", rs.getString("genre"));
                allGenres.add(genre);
            }
            return allGenres;
        });

        if (genres != null) {
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("genres", genres);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 500, "Could not get data");
        }

        return true;
    }
}