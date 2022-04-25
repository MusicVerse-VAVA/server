package com.musicverse.server.db;

import com.falsepattern.json.node.JsonNode;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An abstraction layer on top of the raw jdbc database driver to simplify queries
 */
public class Database implements AutoCloseable {
    private final String url;
    private final String user;
    private final String pass;

    private final AtomicReference<Connection> c = new AtomicReference<>(null);
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    /**
     * Connect to a database. Requires the information from the secrets.json file's <code>database</code> entry.
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
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    private void connect() throws SQLException {
        c.set(DriverManager.getConnection(url, user, pass));
    }
    /**
     * Insert data into the database safely.
     * The SQL logic is done using a "prepare + execute" combo to avoid SQL injections.
     * @param statement The prepare-formatted statement string
     * @param prepareCallback A callback for setting the arguments of the prepared statement
     * @return The amount of lines modified
     */
    public int update(String statement, PrepareCallback prepareCallback) {
        @Cleanup("unlock") val lock = rwLock.writeLock();
        lock.lock();
        try {
            @Cleanup val ps = c.get().prepareStatement(statement);
            prepareCallback.process(ps);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query database", e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Query the database safely and process the results in a callback.
     * The SQL logic is done using a "prepare + execute" combo to avoid SQL injections.
     * @param queryString The prepare-formatted query string
     * @param prepareCallback A callback for setting the arguments of the prepared statement
     * @param queryCallback A callback for processing the result set of the query
     * @return The return value of <code>queryCallback</code>
     */
    public <T> T query(String queryString, PrepareCallback prepareCallback, QueryCallback<T> queryCallback) {
        T result;
        @Cleanup("unlock") val lock = rwLock.readLock();
        lock.lock();
        try {
            @Cleanup val ps = c.get().prepareStatement(queryString);
            prepareCallback.process(ps);
            @Cleanup val rs = ps.executeQuery();
            result = queryCallback.process(rs);
        } catch (SQLException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to query database", e);
        }
        return result;
    }

    @Override
    @SneakyThrows
    public void close() {
        running.set(false);
        c.get().close();
    }

    public interface PrepareCallback {
        void process(PreparedStatement ps) throws SQLException, NoSuchAlgorithmException;
    }

    public interface QueryCallback<T> {
        T process(ResultSet rs) throws SQLException;
    }
}
