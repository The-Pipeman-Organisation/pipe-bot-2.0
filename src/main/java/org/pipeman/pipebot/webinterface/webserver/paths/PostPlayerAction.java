package org.pipeman.pipebot.webinterface.webserver.paths;

import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.SendableException;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import spark.Route;

import java.util.Collections;

public class PostPlayerAction {
    public static Route getRoute() {
        return (request, response) -> {
            /*
             * 0: seeking
             * 1: pause
             * 2: resume
             * 3: skip
             * 4: back
             * 5: loop
             * 6: shuffle
             * 7: choose song from history (index)
             * 8: choose song from queue (index)
             * 9: disconnect
             */
            try {
                PlayerInstance pi = WebServer.getPiForSession(request);
                if (pi == null) throw new SendableException("Header 'auth' is missing or the session has expired.");

                if (request.queryParams("id") == null) throw new SendableException("Parameter 'id' is missing");
                switch (Integer.parseInt(request.queryParams("id"))) {
                    case 0 -> {
                        if (request.queryParams("time") == null) throw new SendableException("Parameter 'time' is missing");
                        pi.player.getPlayingTrack().setPosition(Long.parseLong(request.queryParams("time")));
                    }

                    case 1 -> pi.setPaused(true);
                    case 2 -> pi.setPaused(false);
                    case 3 -> pi.skip();
                    case 4 -> pi.back();
                    case 5 -> pi.loopButtonClicked();
                    case 6 -> Collections.shuffle(pi.queue);
                    case 7 -> {
                        if (request.queryParams("index") == null) throw new SendableException("Parameter 'index' is missing");
                        pi.player.getPlayingTrack().setPosition(Long.parseLong(request.queryParams("index")));
                    }
                    case 8 -> pi.jumpToTrack(1);
                    case 9 -> pi.disconnect();
                }

            } catch (Exception e) {
                response.status(400);
                if (e instanceof SendableException) {
                    return "{\"message\": \"" + e.getMessage() + "\"}";
                }

                if (e instanceof NumberFormatException)
                    return "{\"message\": \"Parameter 'id' and 'time' have to be an integer.\"}";
            }
            return "";
        };

//        Spark.post("/pbi/player_action/", (request, response) -> {
//            /*
//             * 0: seeking
//             * 1: pause
//             * 2: resume
//             * 3: skip
//             * 4: back
//             * 5: loop
//             * 6: shuffle
//             * 7: choose song from history (index)
//             * 8: choose song fomr queue (index)
//             * 9: disconnect
//             */
//            try {
//                PlayerInstance pi = Server.getPiForSession(request);
//                if (pi == null) throw new SendableException("Header 'sid' is missing or the session has expired.");
//
//                if (request.queryParams("id") == null) throw new SendableException("Parameter 'id' is missing");
//                switch (Integer.parseInt(request.queryParams("id"))) {
//                    case 0 -> {
//
//                    }
//
//                    case 1 -> pi.setPaused(true);
//                    case 2 -> pi.setPaused(false);
//                    case 3 -> pi.skip();
//                    case 4 -> pi.back();
//                    case 5 -> pi.loopButtonClicked();
//                    case 6 -> Collections.shuffle(pi.queue);
//                }
//
//            } catch (Exception e) {
//                response.status(400);
//                if (e instanceof SendableException) {
//                    return "{\"message\": \"" + e.getMessage() + "\"}";
//                }
//
//                if (e instanceof NumberFormatException)
//                    return "{\"message\": \"Parameter 'id' has to be an integer.\"}";
//            }
//            return "";
//        });
//    }
    }
}
