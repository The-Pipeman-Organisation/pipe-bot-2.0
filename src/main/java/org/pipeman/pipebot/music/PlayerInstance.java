package org.pipeman.pipebot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.pipeman.pipebot.Main;
import org.pipeman.pipebot.util.music.PlayerInterfaceUtil;
import org.pipeman.pipebot.util.music.InterfaceMode;
import org.pipeman.pipebot.util.music.LoopMode;
import org.pipeman.pipebot.webinterface.WeebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PlayerInstance extends AudioEventAdapter {
    public final AudioPlayer player;
    public final ArrayList<AudioTrack> queue;
    public Message playerGUIMessage;
    private final Logger logger = LoggerFactory.getLogger(PlayerInstance.class);
    private Runnable onInterfaceSent;
    int positionInQueue = 0;
    public final long lastUpdateTimestamp = System.currentTimeMillis();
    LoopMode loopMode = LoopMode.OFF;
    InterfaceMode interfaceMode = InterfaceMode.NOTHING;
    public String sessionID;
    WeebSocketServer wss = Main.weebSocketServer;
    public Guild guild;

    public PlayerInstance(AudioPlayerManager manager, Guild guild) {
        this.queue = new ArrayList<>();
        player = manager.createPlayer();
        player.addListener(this);
        manager.getConfiguration().setFilterHotSwapEnabled(true);
        sessionID = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 16);
        System.out.println(sessionID);
        this.guild = guild;
    }

    private MessageEmbed createInterfaceEmbed(AudioTrack track) {
        EmbedBuilder eb = PlayerInterfaceUtil.genNormalEmbed(
                "https://img.youtube.com/vi/"
                        + track.getInfo().identifier
                        + "/hqdefault.jpg", track, interfaceMode != InterfaceMode.NOTHING);

        switch (interfaceMode) {
            case NOTHING -> {
            }

            case QUEUE -> {
                List<AudioTrack> tmp = queue.subList(positionInQueue + 1, Math.min(queue.size(), positionInQueue + 4));
                StringBuilder out = new StringBuilder();
                if (tmp.size() == 0) {
                    out.append("This is the end of the queue");
                } else {
                    for (AudioTrack t : tmp) {
                        out.append(PlayerInterfaceUtil.formatTitle(t.getInfo().title, track.getInfo().uri)).append("\n");
                        int duration = (int) t.getDuration() / 1000;
                        out.append(String.format("%d:%02d ", duration / 60, duration % 60)).append("\n\n");
                    }

                    if (queue.size() - (positionInQueue + 3) - 1 > 0) {
                        out.append(queue.size() - (positionInQueue + 3) - 1).append(" more");
                    } else {
                        out.append("This is the end of the queue");
                    }
                }
                eb.addField("Queue", out.toString(), false);
            }
            case HISTORY -> eb.addField("History", "No history available", false);
        }

        return eb.build();
    }

    private void updateEmbedInternal(AudioTrack track) {
        if (playerGUIMessage != null) {
            (track == null ? playerGUIMessage.editMessageEmbeds(PlayerInterfaceUtil.genNothingPlayingEmbed(
                    interfaceMode != InterfaceMode.NOTHING)) :

                    playerGUIMessage.editMessageEmbeds(createInterfaceEmbed(track))
                            .setActionRows(Arrays.asList(ActionRow.of(Arrays.asList(
                                    Button.secondary("back", "<<"),
                                    Button.secondary("fb", "-10s"),
                                    Button.secondary("pause", player.isPaused() ? "▶" : "||"),
                                    Button.secondary("ff", "+10s"),
                                    Button.secondary("skip", ">>")

                            )), ActionRow.of(Arrays.asList(
                                    Button.secondary("lqh", "Interface: " + interfaceMode.toString()),
                                    Button.secondary("loop", "Looping: " + loopMode.toString()),
                                    Button.danger("leave", "➜")
                            ))))).queue();
        } else {
            setRunnableToExecuteWhenEmbedWasSent(() -> updateEmbedInternal(track));
        }
    }

    public void updateEmbed() {
        updateEmbed(player.getPlayingTrack());
    }

    public void updateEmbed(AudioTrack track) {
        updateEmbedInternal(track);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return new AudioPlayerSendHandler(player);
    }

    public void queue(AudioTrack track) { // update queue & PI
        queue.add(track);
        player.startTrack(track, true);
        wss.updateQueue(this);
        wss.updatePI(this);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            switch (loopMode) {
                case OFF -> {
                    if (positionInQueue < queue.size() - 1) {
                        positionInQueue++;
                        player.playTrack(queue.get(positionInQueue).makeClone());
                    }
                }
                case SONG -> player.playTrack(queue.get(positionInQueue).makeClone());

                case QUEUE -> {
                    positionInQueue = (positionInQueue + 1 >= queue.size() ? 0 : positionInQueue + 1);
                    player.playTrack(queue.get(positionInQueue).makeClone());
                }
            }
        }
        if (endReason != AudioTrackEndReason.STOPPED) {
            updateEmbed();
        } // update queue & PI
        wss.updateQueue(this);
        wss.updatePI(this);
    }

    public void setRunnableToExecuteWhenEmbedWasSent(Runnable r) {
        onInterfaceSent = r;
    }

    public void sendEmbed(SlashCommandEvent event) {
        event.getChannel().sendMessageEmbeds(PlayerInterfaceUtil.genSearchEmbed()).queue(response -> {
            playerGUIMessage = response;
            if (onInterfaceSent != null) {
                onInterfaceSent.run();
                onInterfaceSent = null;
            }});
    }

    public void togglePaused() { // update PI
        player.setPaused(!player.isPaused());
        updateEmbed();
        wss.updatePI(this);
    }

    public void setPaused(boolean paused) { // update PI
        player.setPaused(paused);
        updateEmbed();
        wss.updatePI(this);
    }

    public void jumpToTrack(int index) { // update PI & queue
        if (index < 0 || index >= queue.size()) {
            return;
        }
        positionInQueue = index;
        player.playTrack(queue.get(index).makeClone());
        wss.updatePI(this);
        wss.updateQueue(this);
        updateEmbed();
    }

    public void skip() { // update PI
        if (positionInQueue < queue.size() - 1) {
            positionInQueue++;
            player.playTrack(queue.get(positionInQueue).makeClone());
            wss.updatePI(this);
        }
    }

    public void back() { // update PI
        if (positionInQueue > 0) {
            positionInQueue--;
            player.playTrack(queue.get(positionInQueue).makeClone());
            wss.updatePI(this);
        }
    }

    public void ffOrFb(long time) { // update PI
        if (player.getPlayingTrack() == null) return;
        player.getPlayingTrack().setPosition(player.getPlayingTrack().getPosition() + time);
        logger.info("Seeking " + time);
        updateEmbed();
        wss.updatePI(this);
    }

    public void lqhButtonClicked() { // update PI
        switch (interfaceMode) {
            case NOTHING -> interfaceMode = InterfaceMode.QUEUE;
            case QUEUE -> interfaceMode = InterfaceMode.HISTORY;
            case HISTORY -> interfaceMode = InterfaceMode.NOTHING;
        }
        wss.updatePI(this);
        updateEmbed();
    }

    public void loopButtonClicked() { // update PI
        switch (loopMode) {
            case OFF -> loopMode = LoopMode.SONG;
            case SONG -> loopMode = LoopMode.QUEUE;
            case QUEUE -> loopMode = LoopMode.OFF;
        }
        wss.updatePI(this);
        updateEmbed();
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void disconnect() { // close session
        player.destroy();
        guild.getAudioManager().closeAudioConnection();
        Main.playerInstances.remove(guild.getIdLong());
        playerGUIMessage.delete().queue(response -> logger.info("GUI message has been deleted."));
        wss.closeSession(this);
    }
}
