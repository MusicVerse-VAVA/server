SELECT * FROM songs WHERE (name ~ ?) AND
    exists (SELECT * FROM artists WHERE (artist_id = id AND 1 = status))