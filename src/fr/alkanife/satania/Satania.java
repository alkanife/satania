package fr.alkanife.satania;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import fr.alkanife.botcommons.Lang;
import fr.alkanife.botcommons.Utils;
import fr.alkanife.botcommons.YMLReader;
import fr.alkanife.satania.commands.Commands;
import fr.alkanife.satania.commands.Handler;
import fr.alkanife.satania.music.TrackScheduler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Satania {

    private static String version = "prod-1.1.0";

    private static HashMap<String, Object> configurationValues;
    private static String creatorID;
    private static String defaultPlaylist;
    private static List<String> allowed;

    private static Logger logger;
    private static JDA jda;
    private static Handler handler;

    private static Guild playingGuild;
    private static TextChannel lastCommandChannel;
    private static AudioPlayerManager playerManager;
    private static AudioPlayer player;
    private static TrackScheduler trackScheduler;

    public static void main(String[] args) {
        Utils.clearTerminal();

        logger = LoggerFactory.getLogger(Satania.class);
        handler = new Handler();

        logger.info("–––––^^^^^^^^–––––––––––––––––––––––––––––––");
        logger.info("–––––vvvvvvvv–––––––––––––––––––––––––––––––");
        logger.info("Starting Satania " + version);

        try {
            logger.info("Reading configuration file");

            configurationValues = YMLReader.read("configuration");
            if (configurationValues == null) {
                logger.error("Configuration file not found");
                return;
            }

            // READING TOKEN
            Object token = configurationValues.get("token");

            if (token == null) {
                logger.error("Invalid token");
                return;
            }

            logger.info("The token was found");
            // --------------

            // READING CREATOR-ID
            Object creatorID = configurationValues.get("creator-id");

            if (creatorID == null) {
                logger.error("Invalid creator-id");
                return;
            }

            Satania.creatorID = String.valueOf(creatorID);
            logger.info("The creator-id was found");
            // --------------

            // READING DEFAULT-PLAYLIST
            Object defaultPlaylist = configurationValues.get("default-playlist");

            if (defaultPlaylist == null) {
                logger.error("Invalid default-playlist");
                return;
            }

            Satania.defaultPlaylist = String.valueOf(defaultPlaylist);
            logger.info("The default playlist was found");
            // --------------

            // READING ALLOWED
            Object allowed = configurationValues.get("allowed");

            if (allowed == null) {
                logger.error("Invalid allowed IDs");
                return;
            }

            Satania.allowed = new ArrayList<>();
            Satania.allowed.add(Satania.getCreatorID());
            String allowedFull = String.valueOf(allowed);
            String[] alloweds = allowedFull.split(", ");

            Satania.allowed.addAll(Arrays.asList(alloweds));

            logger.info(Satania.allowed.size() + " allowed users");
            // --------------

            handler.registerCommands(new Commands());
            logger.info(handler.getCommands().size() + " commands loaded");

            Lang.load();
            logger.info(Lang.getTranslations().size() + " loaded translations");

            logger.info("Starting JDA");
            JDABuilder jdaBuilder = JDABuilder.createDefault(String.valueOf(token));
            jdaBuilder.setRawEventsEnabled(true);
            jdaBuilder.setStatus(OnlineStatus.DO_NOT_DISTURB);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_MEMBERS);
            jdaBuilder.enableIntents(GatewayIntent.GUILD_VOICE_STATES);
            jdaBuilder.setMemberCachePolicy(MemberCachePolicy.ALL);
            jdaBuilder.addEventListeners(new Events());
            jda = jdaBuilder.build();

        } catch (Exception exception) {
            logger.error("Failed to start", exception);
        }
    }

    public static String getVersion() {
        return version;
    }

    public static void setConfigurationValues(HashMap<String, Object> configurationValues) {
        Satania.configurationValues = configurationValues;
    }

    public static HashMap<String, Object> getConfigurationValues() {
        return configurationValues;
    }

    public static String getCreatorID() {
        return creatorID;
    }

    public static String getDefaultPlaylist() {
        return defaultPlaylist;
    }

    public static Handler getHandler() {
        return handler;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static JDA getJda() {
        return jda;
    }

    public static List<String> getAllowed() {
        return allowed;
    }

    public static void setAllowed(List<String> allowed) {
        Satania.allowed = allowed;
    }

    public static AudioPlayerManager getPlayerManager() {
        return playerManager;
    }

    public static void setPlayerManager(AudioPlayerManager playerManager) {
        Satania.playerManager = playerManager;
    }

    public static AudioPlayer getPlayer() {
        return player;
    }

    public static void setPlayer(AudioPlayer player) {
        Satania.player = player;
    }

    public static TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public static void setTrackScheduler(TrackScheduler trackScheduler) {
        Satania.trackScheduler = trackScheduler;
    }

    public static Guild getPlayingGuild() {
        return playingGuild;
    }

    public static void setPlayingGuild(Guild playingGuild) {
        Satania.playingGuild = playingGuild;
    }

    public static TextChannel getLastCommandChannel() {
        return lastCommandChannel;
    }

    public static void setLastCommandChannel(TextChannel lastCommandChannel) {
        Satania.lastCommandChannel = lastCommandChannel;
    }
}
