package org.pipeman.pipebot.webinterface.webserver.paths;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import spark.Route;

import static org.pipeman.pipebot.Main.playerManager;

public class PostQueue {
    public static Route getRoute() {
        return (request, response) -> {
            if (request.params("query") == null) return "{\"message\": \"Parameter query is missing\"}";

            PlayerInstance pi = WebServer.getPiForSession(request);
            if (pi == null) {
                return "{\"message\": \"Header 'sid' is missing or the session has expired.\"}";
            } else {
                addSong(request.params("query"), pi);
            }
            return "";
        };
    }

    private static void addSong(String searchQuery, PlayerInstance pi) {
        playerManager.loadItemOrdered(pi, searchQuery, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                pi.queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {}

            @Override
            public void noMatches() {}

            @Override
            public void loadFailed(FriendlyException exception) {}
        });
    }
}
