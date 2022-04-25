package com.musicverse.server.api.auth;

import com.falsepattern.json.node.ListNode;
import com.falsepattern.json.node.ObjectNode;
import com.musicverse.server.HttpHelper;
import com.musicverse.server.Util;
import com.musicverse.server.api.POSTRequestHandler;
import com.musicverse.server.db.Database;
import com.sun.net.httpserver.HttpExchange;
import lombok.val;

public class GetRequests extends POSTRequestHandler {
    public GetRequests(Database db) {
        super(db);
    }
    private static final String getRequests = Util.loadResource("/com/musicverse/server/sql/get_requests.sql");

    @Override
    public boolean handlePostRequest(String url, String params, HttpExchange exchange) throws Throwable {
        if (!"/getRequests".equals(url)) return false;
        val requests = db.query(getRequests,
                (ps) -> {
                    ps.setInt(1, 0);
                },
                (rs) -> {
                    val allRequests = new ListNode();
                    while (rs.next()){
                        val request = new ObjectNode();
                        request.set("id", rs.getInt("id"));
                        request.set("name", rs.getString("name"));
                        allRequests.add(request);
                    }
                    return allRequests;
                });

        if (requests != null) {
            val response = new ObjectNode();
            response.set("status", "ok");
            response.set("requests", requests);
            HttpHelper.respondWithJson(exchange, 200, response);
        } else {
            HttpHelper.respondWithErrorString(exchange, 500, "Could not get data");
        }

        return true;

    }
}
