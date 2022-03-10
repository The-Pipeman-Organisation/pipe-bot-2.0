package org.pipeman.pipebot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.pipeman.pipebot.Main;
import org.pipeman.pipebot.music.PlayerInstance;

import java.awt.*;

public class CommandSession extends ListenerAdapter {
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (!event.getName().equals("webinterface") || event.getMember() == null || event.getGuild() == null) return;
        PlayerInstance pi = Main.playerInstances.get(event.getGuild().getIdLong());

        if (event.getMember().getVoiceState() != null &&
                event.getMember().getVoiceState().getChannel() != null &&
                pi != null &&
                event.getMember().getVoiceState().getChannel().equals(
                        event.getGuild().getAudioManager().getConnectedChannel())) {

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Webinterface");
            eb.setColor(new Color(114, 137, 218));
            eb.addField("This url will be regenerated every time the bot joins a voice channel.",
                    "https://pipeman.org/pbi/" + pi.sessionID, true);
            event.replyEmbeds(eb.build()).setEphemeral(true).queue();

            return;
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("Webinterface");
        eb.setColor(new Color(231, 76, 60));
        eb.addField("ERROR", "You are not in a voice channel together with me!", true);
        event.replyEmbeds(eb.build()).setEphemeral(true).queue();
    }
}
