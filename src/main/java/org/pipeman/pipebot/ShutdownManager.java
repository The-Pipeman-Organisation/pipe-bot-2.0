package org.pipeman.pipebot;

import org.pipeman.pipebot.util.music.AudioUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownManager {
    private static final Logger logger = LoggerFactory.getLogger(ShutdownManager.class);

    public static void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(ShutdownManager::shutdown));
        logger.info("Started shutdown hook.");
    }

    public static void shutdown() {
        logger.info("Shutting down...");
        AudioUtil.shutdown();
        try {
            Main.weebSocketServer.close();
        } catch (Exception ignored) {}
        try {
            Thread.sleep(2000L);
        } catch (Exception ignored) {}
    }
}
