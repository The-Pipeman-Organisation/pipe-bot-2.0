package org.pipeman.pipebot.webinterface;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import org.java_websocket.WebSocket;
import org.json.JSONArray;
import org.json.JSONObject;
import org.pipeman.pipebot.Main;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.webinterface.webserver.WebServer;

import java.util.ArrayList;
import java.util.Map;

import static org.pipeman.pipebot.webinterface.webserver.paths.GetSearchSongs.trackToMap;

public class S2CPackets {
    public static void sendQueue(WebSocket socket, ArrayList<AudioTrack> queue) {
        JSONObject out = new JSONObject();
        JSONArray JSONQueue = new JSONArray();
        try {
            out.put("id", 1);
            for (AudioTrack t : queue) {
                JSONQueue.put(trackToMap(t));
            }
            out.put("queue", JSONQueue);
            socket.send(out.toString(4));
        } catch (Exception ignored) {}
    }

    public static void sendPlayerStatus(WebSocket socket, PlayerInstance pi) {
        AudioTrackInfo t = pi.player.getPlayingTrack().getInfo();
        JSONObject out = new JSONObject();
        try {
            out.put("id", 2);
            out.put("track_info", Map.of(
                    "track_url", t.uri,
                    "track_name", t.title,
                    "track_id", t.identifier,
                    "track_author", t.author,
                    "position_in_queue", pi.getPositionInQueue()
            ));
            out.put("play_time", Map.of(
                    "track_pos", pi.player.getPlayingTrack().getPosition(),
                    "track_duration", pi.player.getPlayingTrack().getDuration(),
                    "paused", pi.player.isPaused()
            ));
            socket.send(out.toString(4));
        } catch (Exception ignored) {}
    }

    public static void sendHistory(WebSocket socket) {
        // TODO make history even work
    }

    public static void closeSession(WebSocket socket) {
        socket.send("{\"id\": 0}");
    }
}
