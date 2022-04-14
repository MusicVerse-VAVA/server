package com.musicverse.server.db;

import com.falsepattern.json.node.JsonNode;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An abstraction layer on top of the raw jdbc database driver to simplify queries
 */
public class Database implements AutoCloseable {
    private static final int MAX_RETRY_COUNT = 10;
    private final String url;
    private final String user;
    private final String pass;

    private final AtomicReference<Connection> c = new AtomicReference<>(null);
    private final Thread autoReconnect;
    private final AtomicBoolean running = new AtomicBoolean(true);
    /**
     * Connect to a database.
     */
    public Database(JsonNode secrets) {
        try {
            //Initialize postgres driver
            Class.forName("org.postgresql.Driver");

            val server = secrets.get("server");
            val login = secrets.get("login");
            url = "jdbc:postgresql://" + server.getString("host") + ":" + server.getInt("port") + "/"
                  + secrets.getString("db");
            user = login.getString("user");
            pass = login.getString("pass");
            connect();
            System.out.println("Starting database autoreconnect worker");
            autoReconnect = new Thread(() -> {
                while (running.get()) {
                    try {
                        //noinspection BusyWait
                        Thread.sleep(60000); //1 minute
                    } catch (InterruptedException e) {
                        continue;
                    }
                    synchronized (c) {
                        try {
                            val db = c.get();
                            c.get().close();
                            connect();
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            autoReconnect.setName("Database auto-reconnect thread");
            autoReconnect.setDaemon(true);
            autoReconnect.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void connect() throws SQLException {
        c.set(DriverManager.getConnection(url, user, pass));
    }

    /**
     * Query the database and process the results in a callback.
     * The SQL logic is done using a "prepare + execute" combo to avoid SQL injections.
     */
    public <T> T query(String queryString, PrepareCallback prepareCallback, QueryCallback<T> queryCallback) {
        int retries = 0;
        T result;
        while (true) {
            synchronized (c) {
                try {
                    @Cleanup val ps = c.get().prepareStatement(queryString);
                    prepareCallback.process(ps);
                    @Cleanup val rs = ps.executeQuery();
                    result = queryCallback.process(rs);
                    break;
                } catch (SQLException e) {
                    if (retries >= MAX_RETRY_COUNT) {
                        throw new RuntimeException("Failed to query database", e);
                    } else {
                        System.err.println("Failed to query database");
                        e.printStackTrace();
                        System.err.println("Reconnecting...");
                        try {
                            c.get().close();
                        } catch (Throwable ignored) {}
                        try {
                            connect();
                        } catch (SQLException ex) {
                            throw new IllegalStateException("Failed to reconnect to database!", ex);
                        }
                        retries++;
                    }
                }
            }
        }
        return result;
    }

    @Override
    @SneakyThrows
    public void close() {
        running.set(false);
        autoReconnect.interrupt();
        autoReconnect.join();
        c.get().close();
    }

    public interface PrepareCallback {
        void process(PreparedStatement ps) throws SQLException;
    }

    public interface QueryCallback<T> {
        T process(ResultSet rs) throws SQLException;
    }
}
