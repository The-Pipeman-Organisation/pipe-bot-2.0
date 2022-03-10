package org.pipeman.pipebot.webinterface.webserver.paths;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.SendableException;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import spark.Route;

public class PatchSong {
    public static Route getRoute() {
        return (request, response) -> {
            try {
                PlayerInstance pi = WebServer.getPiForSession(request);
                if (pi == null) throw new SendableException("Header 'sid' is missing or the session has expired.");
                if (request.queryParams("index") == null) throw new SendableException("Parameter 'index' is missing");
                if (request.queryParams("new_index") == null) throw new SendableException("Parameter 'new_index' is missing");
                int index = Integer.parseInt("index");
                int new_index = Integer.parseInt("new_index");
                if (index < 0 || index >= pi.queue.size()) throw new SendableException("Parameter 'index' is out of range.");
                if (new_index < 0 || new_index >= pi.queue.size()) throw new SendableException("Parameter 'new_index' is out of range.");

                AudioTrack value = pi.queue.get(index);
                pi.queue.remove(index);
                pi.queue.add(new_index, value);
            } catch (Exception e) {
                response.status(400);
                if (e instanceof SendableException) {
                    return "{\"message\": \"" + e.getMessage() + "\"}";
                }

                if (e instanceof NumberFormatException)
                    return "{\"message\": \"Parameter 'id' has to be an integer.\"}";
            }

            return "";
        };
    }
}
