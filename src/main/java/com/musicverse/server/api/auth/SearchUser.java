package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class SearchUser extends POSTRequestHandler {
    public SearchUser(Database db) {
        super(db);
    }

    private static final String getUsers = Util.loadResource("/com/musicverse/server/sql/get_users.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/searchUsers".equals(url)) return false;

        val request = HttpHelper.parseJSON(exchange);
        val input = request.getString("input");

        String regex = "(" + input + ")+";

        val users = db.query(getUsers,
                (ps) -> {
                    ps.setString(1, regex);
                    ps.setString(2, regex);
                },
                (rs) -> {
                    val filteredUsers = new ListNode();
                    while(rs.next()){
                        val result = new ObjectNode();
                        result.set("id", rs.getInt("id"));
                        result.set("nick_name", rs.getString("nickname"));
                        result.set("email", rs.getString("email"));
                        result.set("access_level", rs.getInt("access_level"));
                        filteredUsers.add(result);
                    }
                    return filteredUsers;
                });

        val response = new ObjectNode();
        response.set("status", "ok");
        response.set("users", users);
        HttpHelper.respondWithJson(exchange, 200, response);
        return true;
    }
}
