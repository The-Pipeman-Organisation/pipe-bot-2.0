package org.pipeman.pipebot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.pipeman.pipebot.music.PlayerInstance;
import org.pipeman.pipebot.util.music.EmbedUpdater;
import org.pipeman.pipebot.webinterface.WeebSocketServer;
import org.pipeman.pipebot.webinterface.webserver.WebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Map;

public class Main extends ListenerAdapter {
    public static JDA JDA;
    public static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    public static final Map<Long, PlayerInstance> playerInstances = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static final WeebSocketServer weebSocketServer = new WeebSocketServer();

    public static void main(String[] args) throws LoginException {
        if (args.length < 1) {
            logger.error("Please pass a bot token as first programm argument!");
            System.exit(1);
        }
        weebSocketServer.start();
        WebServer.startServer();

        JDA = JDABuilder.createDefault(args[0])
                .setActivity(Activity.listening("https://www.youtube.com/watch?v=Gleuqf10eB8"))
                .build();

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        AllCommands.addEventListeners(JDA);
        ShutdownManager.start();
        EmbedUpdater.start();
        JDA.upsertCommand("webinterface", "Sends the webinterface url").queue();
        JDA.upsertCommand(new CommandData("play", "Play a song and/or summons the bot to your VC.")
                .addOption(OptionType.STRING, "query", "Search query or url", true)).queue();
    }
}

