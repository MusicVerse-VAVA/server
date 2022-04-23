package com.musicverse.server.api.songfiles;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.IOUtil;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class UploadSongData extends POSTRequestHandler {
    public UploadSongData(Database db) {
        super(db);
    }

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/uploadSongData".equals(url)) return false;
        val in = exchange.getRequestBody();

        val id = IOUtil.readInt(in);
        val file = IOUtil.getSongFile(id);
        IOUtil.streamToFile(in, file);
        HttpHelper.respondWithOk(exchange);
        return true;
    }
}
