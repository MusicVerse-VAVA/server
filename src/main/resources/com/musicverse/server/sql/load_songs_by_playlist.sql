select * from songs where id in (
select songs_id from songs_in_playlists where playlists_id = ?)