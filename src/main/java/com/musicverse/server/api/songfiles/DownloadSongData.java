package com.musicverse.server.api.songfiles;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.IOUtil;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

import java.nio.file.Files;

public class DownloadSongData extends POSTRequestHandler {
    public DownloadSongData(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/downloadSongData".equals(url)) return false;
        val json = HttpHelper.parseJSON(exchange);
        val id = json.getInt("song_id");
        val file = IOUtil.getSongFile(id);
        if (!file.exists()) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
        } else {
            long responseLength = Files.size(file.toPath()) + 4L;
            exchange.sendResponseHeaders(200, responseLength);
            val resp = exchange.getResponseBody();
            IOUtil.fileToStream(file, resp);
            resp.flush();
            exchange.close();
        }
        return true;
    }
}
