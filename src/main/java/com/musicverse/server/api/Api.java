package com.musicverse.server.api;

import com.musicverse.server.HttpHelper;
import com.musicverse.server.api.auth.*;
import com.musicverse.server.api.songfiles.DownloadSongData;
import com.musicverse.server.api.songfiles.UploadSongData;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.SneakyThrows;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class Api {
    private final List<RequestHandler> handlers = new ArrayList<>();
    public Api(Database db) {
        handlers.add(new Register(db));
        handlers.add(new Auth(db));
        handlers.add(new Playlists(db));
        handlers.add(new Genres(db));
        handlers.add(new CreateArtist(db));
        handlers.add(new LoadArtistByUser(db));
        handlers.add(new EditArtist(db));
        handlers.add(new ArtistsByGenre(db));
        handlers.add(new SongsByPlaylist(db));
        handlers.add(new SongsByAlbum(db));
        handlers.add(new DeleteSong(db));
        handlers.add(new DeleteCollection(db));
        handlers.add(new CreateCollection(db));
        handlers.add(new addToPlaylist(db));
        handlers.add(new GetLastSongId(db));
        handlers.add(new DownloadSongData(db));
        handlers.add(new UploadSongData(db));
        handlers.add(new SearchUser(db));
        handlers.add(new UpdateUser(db));
        handlers.add(new GetRequests(db));
    }

    @SneakyThrows
    public boolean handle(HttpExchange exchange) {
        try {
            for (val handler : handlers) {
                if (handler.handleRequest(exchange)) {
                    return true;
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
            HttpHelper.respondWithErrorString(exchange, 500, "Internal server error");
            return true;
        }
        return false;
    }
}
