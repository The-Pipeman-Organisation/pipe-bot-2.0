package org.pipeman.pipebot.webinterface.webserver;

import org.pipeman.pipebot.Main;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.paths.*;
import spark.Request;
import spark.Spark;

import java.util.Map;

public class WebServer {
    public static void main(String[] args) {
        startServer();
    }

    public static void startServer() {
        Spark.port(12001);

        Spark.notFound((request, response) -> {
            response.status(404);
            return "{\"message\": \"404: Not found (" + request.url() + ")\"}";
        });

        Spark.options("/*",
                (request, response) -> {
                    String accessControlRequestHeaders = request
                            .headers("Access-Control-Request-Headers");
                    if (accessControlRequestHeaders != null) {
                        response.header("Access-Control-Allow-Headers",
                                accessControlRequestHeaders);
                    }
                    String accessControlRequestMethod = request
                            .headers("Access-Control-Request-Method");
                    if (accessControlRequestMethod != null) {
                        response.header("Access-Control-Allow-Methods",
                                accessControlRequestMethod);
                    }
                    return "OK";
                });
        Spark.before((request, response) -> response.header("Access-Control-Allow-Origin", "*"));

        Spark.post("/player_action/", PostPlayerAction.getRoute());
        Spark.get("/yt_search/:query", GetSearchSongs.getRoute());
        Spark.post("/queue/add_first/:query", PostQueue.getRoute());
        Spark.delete("/queue/:index", DelSong.getRoute());
        Spark.patch("/queue/", PatchSong.getRoute());
    }

    public static PlayerInstance getPiForSession(Request request) {
        if (request.headers("auth") == null) return null;

        for (Map.Entry<Long, PlayerInstance> e : Main.playerInstances.entrySet()) {
            if (e.getValue().sessionID.equals(request.headers("auth"))) {
                return e.getValue();
            }
        }
        return null;
    }

    public static PlayerInstance getPiForSession(String sid) {
        for (Map.Entry<Long, PlayerInstance> e : Main.playerInstances.entrySet()) {
            if (e.getValue().sessionID.equals(sid)) {
                return e.getValue();
            }
        }
        return null;
    }
}
