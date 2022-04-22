DELETE FROM songs_in_playlists WHERE songs_id IN (SELECT id FROM songs WHERE album = ?);
DELETE FROM songs WHERE album = ?;
DELETE FROM albums WHERE id = ?;