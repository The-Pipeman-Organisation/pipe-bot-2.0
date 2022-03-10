package org.pipeman.pipebot.commands;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.pipeman.pipebot.util.music.AudioUtil;

import java.util.Objects;

public class CommandPlay extends ListenerAdapter {

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (!event.getName().equals("play") || event.getMember() == null) return;

        if (event.getMember().getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("Please connect to a voice channel.").setEphemeral(true).queue();
            return;
        }

        String song = Objects.requireNonNull(event.getOption("query")).getAsString();
        //noinspection HttpUrlsUsage
        if (!song.startsWith("https://") && !song.startsWith("http://")) {
            AudioUtil.loadAndPlay(event.getMember(), event, "ytsearch:\"" + song + "\"");
        } else {
            AudioUtil.loadAndPlay(event.getMember(), event, song);
        }
        //Sound Cloud Search: scsearch:
    }
}
