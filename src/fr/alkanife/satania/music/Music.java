package fr.alkanife.satania.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.alkanife.botcommons.Lang;
import fr.alkanife.botcommons.Utils;
import fr.alkanife.satania.Satania;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Music {

    public static void loadAndPlay(SlashCommandEvent slashCommandEvent, final String url, boolean priority) {
        slashCommandEvent.deferReply().queue();
        Satania.getPlayerManager().loadItemOrdered(Satania.getPlayer(), url, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                Satania.addAddedMusics();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle(Lang.t("jukebox-command-play-added-title") + " " + (priority ? Lang.t("jukebox-command-priority") : ""));
                embedBuilder.setDescription("[" + track.getInfo().title + "](" + track.getInfo().uri + ") " + Lang.t("jukebox-by")
                        + " [" + track.getInfo().author + "](" + track.getInfo().uri + ") [" + Utils.musicDuration(track.getDuration()) + "]" +
                        (priority ? "" : ("\n\n" + (Lang.t("jukebox-command-play-added-position") + " `" + (Satania.getTrackScheduler().getQueue().size() + 1) + "`"))));
                embedBuilder.setThumbnail("https://img.youtube.com/vi/" + track.getIdentifier() + "/0.jpg");

                slashCommandEvent.getHook().sendMessageEmbeds(embedBuilder.build()).queue();

                connectAndPlay(slashCommandEvent, track, priority);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null)
                    firstTrack = playlist.getTracks().get(0);

                connectAndPlay(slashCommandEvent, firstTrack, priority);

                if (url.startsWith("ytsearch")) {
                    Satania.addAddedMusics();

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(Lang.t("jukebox-command-play-added-title") + " " + (priority ? Lang.t("jukebox-command-priority") : ""));
                    embedBuilder.setDescription("[" + firstTrack.getInfo().title + "](" + firstTrack.getInfo().uri + ") " + Lang.t("jukebox-by")
                            + " [" + firstTrack.getInfo().author + "](" + firstTrack.getInfo().uri + ") [" + Utils.musicDuration(firstTrack.getDuration()) + "]" +
                            (priority ? "" : ("\n\n" + (Lang.t("jukebox-command-play-added-position") + " `" + (Satania.getTrackScheduler().getQueue().size() + 1) + "`"))));
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + firstTrack.getIdentifier() + "/0.jpg");

                    slashCommandEvent.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
                } else {
                    Satania.addAddedPlaylists();

                    Satania.getTrackScheduler().queuePrioritizePlaylist(playlist);

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(Lang.t("jukebox-command-play-playlist-added") + " " + (priority ? Lang.t("jukebox-command-priority") : ""));
                    embedBuilder.setDescription("[" + playlist.getName() + "](" + firstTrack.getInfo().uri + ")\n\n" +
                            Lang.t("jukebox-command-play-playlist-entries") + " `" + playlist.getTracks().size() + "`\n" +
                            Lang.t("jukebox-command-play-playlist-newtime") + " `" + Utils.musicDuration(Satania.getTrackScheduler().getQueueDuration()) + "`");

                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + firstTrack.getIdentifier() + "/0.jpg");

                    slashCommandEvent.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
                }
            }

            @Override
            public void noMatches() {
                slashCommandEvent.getHook().sendMessage(Lang.t("jukebox-command-play-nomatches")).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                slashCommandEvent.getHook().sendMessage(Lang.t("jukebox-command-play-error")).queue();
                //Amiria.getLogger().error("could not play", exception);
            }
        });
    }

    public static void connectAndPlay(SlashCommandEvent slashCommandEvent, AudioTrack audioTrack, boolean priority) {
        if (!slashCommandEvent.getGuild().getAudioManager().isConnected()
                && !slashCommandEvent.getGuild().getAudioManager().isAttemptingToConnect()) {

            Member member = slashCommandEvent.getMember();

            if (member != null) {
                if (member.getVoiceState() != null) {
                    if (member.getVoiceState().getChannel() != null) {
                        slashCommandEvent.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(Satania.getPlayer()));
                        slashCommandEvent.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
                        Satania.setPlayingGuild(slashCommandEvent.getGuild());
                    } else {
                        connectToFirst(slashCommandEvent);
                    }
                } else {
                    connectToFirst(slashCommandEvent);
                }
            } else {
                connectToFirst(slashCommandEvent);
            }
        }

        Satania.getTrackScheduler().queue(audioTrack, priority);
    }

    private static void connectToFirst(SlashCommandEvent slashCommandEvent) {
        for (VoiceChannel voiceChannel : slashCommandEvent.getGuild().getVoiceChannels()) {
            slashCommandEvent.getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(Satania.getPlayer()));
            slashCommandEvent.getGuild().getAudioManager().openAudioConnection(voiceChannel);
            Satania.setPlayingGuild(slashCommandEvent.getGuild());
            break;
        }
    }

    public static void reset() {
        disable();
        initialize();
    }

    public static void disable() {
        Satania.getPlayer().stopTrack();
        Satania.getPlayer().destroy();

        Satania.getPlayingGuild().getAudioManager().closeAudioConnection();
    }

    public static void initialize() {
        Satania.setPlayerManager(new DefaultAudioPlayerManager());
        AudioSourceManagers.registerRemoteSources(Satania.getPlayerManager());
        AudioSourceManagers.registerLocalSource(Satania.getPlayerManager());

        Satania.setPlayer(Satania.getPlayerManager().createPlayer());
        Satania.setTrackScheduler(new TrackScheduler(Satania.getPlayer()));
        Satania.getPlayer().addListener(Satania.getTrackScheduler());
    }

}
