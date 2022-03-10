package org.pipeman.pipebot.webinterface.webserver.paths;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.json.JSONArray;
import spark.Route;

import java.util.Map;

import static org.pipeman.pipebot.Main.playerManager;

public class GetSearchSongs {
    public static Route getRoute() {
        return (request, response) -> {
            if (request.params("query") == null) return "";

            return search(request.params("query")).toString(2);
        };
    }

    private static JSONArray search(String query) {
        //noinspection HttpUrlsUsage
        if (!query.startsWith("https://") && !query.startsWith("http://")) {
            query = "ytsearch:\"" + query + "\"";
        }

        JSONArray out = new JSONArray();
        try {
            playerManager.loadItem(query, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    out.put(trackToMap(track));
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    for (AudioTrack t : playlist.getTracks()) {
                        out.put(trackToMap(t));
                    }
                }

                @Override
                public void noMatches() {}

                @Override
                public void loadFailed(FriendlyException exception) {}
            }).get();
        } catch (Exception ignored) {}

        return out;
    }

    public static Map<String, ?> trackToMap(AudioTrack track) {
        AudioTrackInfo trackInfo = track.getInfo();
        return Map.of("duration", track.getDuration(),
                "identifier", trackInfo.identifier,
                "title", trackInfo.title,
                "author", trackInfo.author);
    }
}
