# server
The server side logic of MusicVerse

# db scheme

![dbscheme](https://user-images.githubusercontent.com/43637485/166413785-1f7f8895-cec5-45c0-b5db-a949b504a2fd.png)


# back end logic running on our server

It connects to the DB using built-in java JDBC, the Database class on the server is just a wrapper class that wraps the raw jdbc calls into the query(queryString, prepareCallback, resultCallback) method

the communication between the server and the client are also just java built-in HTTP server and HTTP urlConnection, and the JSON library is just a minimalistic json serializer/deserializer from maven

for actually connecting to the database and launching the HTTP endpoint, the server needs a secrets.json file in the running directory that has the following format:

![config](https://user-images.githubusercontent.com/43637485/166414078-295f72b1-6da6-46e1-92a6-a0f0f2e416ac.png)

likewise, at the client, it needs a file called server.json with this format:

![config2](https://user-images.githubusercontent.com/43637485/166414228-43a832dc-50fb-4e42-b7e4-9c0c90ca1c1b.png)



