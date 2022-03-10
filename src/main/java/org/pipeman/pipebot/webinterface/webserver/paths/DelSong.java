package org.pipeman.pipebot.webinterface.webserver.paths;

import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.SendableException;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import spark.Route;

public class DelSong {
    public static Route getRoute() {
        return (request, response) -> {
            try {
                PlayerInstance pi = WebServer.getPiForSession(request);
                int index;
                if (pi == null) throw new SendableException("Header 'sid' is missing or the session has expired.");
                index = Integer.parseInt(request.params("index"));
                if (index < 0 || index >= pi.queue.size()) throw new SendableException("Song index invalid.");
                pi.queue.remove(index);
            } catch (Exception e) {
                response.status(400);
                if (e instanceof SendableException) {
                    return "{\"message\": \"" + e.getMessage() + "\"}";
                }

                if (e instanceof NumberFormatException)
                    return "{\"message\": \"Song index invalid.\"}";
            }
            return "";
        };
    }
}
