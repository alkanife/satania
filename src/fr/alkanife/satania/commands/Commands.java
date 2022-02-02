package fr.alkanife.satania.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.alkanife.botcommons.Command;
import fr.alkanife.botcommons.Lang;
import fr.alkanife.botcommons.Utils;
import fr.alkanife.satania.Satania;
import fr.alkanife.satania.music.Music;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Commands {

    private String offsetToString(OffsetDateTime offsetDateTime) {
        return new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss").format(new Date(offsetDateTime.toInstant().toEpochMilli()));
    }

    @Command(name = "satania")
    public void satania(SlashCommandEvent slashCommandEvent) {
        slashCommandEvent.reply(Lang.t("satania-command")).setEphemeral(true).queue();
    }

    @Command(name = "copy")
    public void copy(SlashCommandEvent slashCommandEvent) {
        try {
            OptionMapping copyOption = slashCommandEvent.getOption("input");

            String copyURL = copyOption.getAsString();

            if (!Utils.isURL(copyURL)) {
                slashCommandEvent.reply(Lang.t("copy-command-error-noturl")).setEphemeral(true).queue();
                return;
            } // 6

            String[] args = copyURL.split("/"); // 4 5 6

            String serverId = args[4];
            String channelId = args[5];
            String messageId = args[6];

            /*Satania.getLogger().info(serverId);
            Satania.getLogger().info(channelId);
            Satania.getLogger().info(messageId);*/

            Guild guild = slashCommandEvent.getJDA().getGuildById(serverId);

            if (guild == null) {
                slashCommandEvent.reply(Lang.t("copy-command-error-guild")).setEphemeral(true).queue();
                return;
            }

            TextChannel textChannel = guild.getTextChannelById(channelId);

            if (textChannel == null) {
                slashCommandEvent.reply(Lang.t("copy-command-error-channel")).setEphemeral(true).queue();
                return;
            }

            textChannel.retrieveMessageById(messageId).queue(message -> {
                if (message == null) {
                    slashCommandEvent.reply(Lang.t("copy-command-error-message")).setEphemeral(true).queue();
                    return;
                }

                /*Satania.getLogger().info(guild.getName());
                Satania.getLogger().info(textChannel.getName());
                Satania.getLogger().info(message.getAuthor().getName()); display*/

                slashCommandEvent.reply("``` " + message.getContentDisplay() + "```").setEphemeral(true).queue();
            });


        } catch (Exception exception) {
            slashCommandEvent.reply(Lang.t("copy-command-error")).setEphemeral(true).queue();
        }
    }

    @Command(name = "serverinfo")
    public void serverinfo(SlashCommandEvent slashCommandEvent) {
        Guild guild = slashCommandEvent.getGuild();
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(guild.getName());

        StringBuilder description = new StringBuilder();
        description.append("**").append(Lang.t("serverinfo-command-members")).append("** ").append(guild.getMemberCount()).append("\n");

        if (guild.getOwner() != null)
            description.append("**").append(Lang.t("serverinfo-command-owner")).append("** ").append(guild.getOwner().getAsMention()).append("\n");

        description.append("**").append(Lang.t("serverinfo-command-creation-date")).append("** ").append(offsetToString(guild.getTimeCreated())).append("\n");
        description.append("\n");

        if (guild.getIconUrl() != null) {
            embedBuilder.setThumbnail(guild.getIconUrl());
            description.append("[Icon url](").append(guild.getIconUrl()).append(")\n");
        }

        if (guild.getBannerUrl() != null)
            description.append("[Banner url](").append(guild.getBannerUrl()).append(")\n");

        if (guild.getSplashUrl() != null)
            description.append("[Splash url](").append(guild.getSplashUrl()).append(")\n");

        if (guild.getVanityUrl() != null)
            description.append("[Vanity url](").append(guild.getVanityUrl()).append(")\n");

        embedBuilder.setDescription(description);
        embedBuilder.setThumbnail(guild.getIconUrl());

        slashCommandEvent.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    @Command(name = "emoteinfo")
    public void emoteinfo(SlashCommandEvent slashCommandEvent) {
        OptionMapping optionMapping = slashCommandEvent.getOption("input");
        String input = optionMapping.getAsString();
        String[] args = input.split(":");

        if (args.length < 3) {
            slashCommandEvent.reply(Lang.t("emoteinfo-command-error")).setEphemeral(true).queue();
            return;
        }

        String emoteID = args[2].replaceAll(">", "");

        Emote emote = slashCommandEvent.getJDA().getEmoteById(emoteID);

        if (emote == null) {
            slashCommandEvent.reply(Lang.t("emoteinfo-command-error")).setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(emote.getName());
        embedBuilder.setThumbnail(emote.getImageUrl());

        StringBuilder stringBuilder = new StringBuilder();

        if (emote.getGuild() != null)
            stringBuilder.append("**").append(Lang.t("emoteinfo-command-guild")).append("** ").append(emote.getGuild().getName()).append("\n");

        stringBuilder.append("**").append(Lang.t("emoteinfo-command-creation-date")).append("** ").append(offsetToString(emote.getTimeCreated())).append("\n");
        stringBuilder.append("\n[URL](").append(emote.getImageUrl()).append(")");

        embedBuilder.setDescription(stringBuilder);

        slashCommandEvent.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    @Command(name = "memberinfo")
    public void memberinfo(SlashCommandEvent slashCommandEvent) {
        OptionMapping optionMapping = slashCommandEvent.getOption("input");
        User user = optionMapping.getAsUser();

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setThumbnail(user.getAvatarUrl());
        embedBuilder.setTitle(user.getName());
        embedBuilder.setDescription(user.getAsMention() + "\n" +
                "\n" +
                "**" + Lang.t("memberinfo-command-joined") + "** " + offsetToString(user.getTimeCreated()));

        slashCommandEvent.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    @Command(name = "jukebox")
    public void music(SlashCommandEvent slashCommandEvent) {
        if (!Satania.getAllowed().contains(slashCommandEvent.getUser().getId())) {
            slashCommandEvent.reply(Lang.t("command-no-perms")).queue();
            return;
        }

        Satania.setLastCommandChannel(slashCommandEvent.getTextChannel());

        String subCommand = slashCommandEvent.getSubcommandName();

        switch (subCommand) {
            case "play":
                String url = slashCommandEvent.getOption("input").getAsString();

                if (url.equalsIgnoreCase("default"))
                    url = Satania.getDefaultPlaylist();
                else
                if (!Utils.isURL(url))
                    url = "ytsearch: " + url;

                Music.loadAndPlay(slashCommandEvent, url, false);
                break;

            case "play_next":
                String url1 = slashCommandEvent.getOption("input").getAsString();

                if (url1.equalsIgnoreCase("default"))
                    url1 = Satania.getDefaultPlaylist();
                else
                if (!Utils.isURL(url1))
                    url1 = "ytsearch: " + url1;

                Music.loadAndPlay(slashCommandEvent, url1, true);
                break;

            case "remove":
                if (Satania.getPlayer().getPlayingTrack() == null) {
                    slashCommandEvent.reply(Lang.t("jukebox-command-no-current")).queue();
                    return;
                }

                OptionMapping removeOption = slashCommandEvent.getOption("input");

                long remove = 1;

                if (removeOption != null) {
                    remove = removeOption.getAsLong();

                    if (remove >= Satania.getTrackScheduler().getQueue().size()) {
                        slashCommandEvent.reply(Lang.t("jukebox-command-notenough")).queue();
                        return;
                    }
                }

                List<AudioTrack> aTracks = new ArrayList<>(Satania.getTrackScheduler().getQueue());

                try {
                    AudioTrack t = aTracks.get(((int) remove)-1);

                    aTracks.remove(t);

                    BlockingQueue<AudioTrack> newBlockingQueue = new LinkedBlockingQueue<>();

                    for (AudioTrack audioTrack : aTracks)
                        newBlockingQueue.offer(audioTrack);

                    Satania.getTrackScheduler().setQueue(newBlockingQueue);

                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle(Lang.t("jukebox-command-remove-title"));
                    embedBuilder.setDescription("[" + t.getInfo().title + "](" + t.getInfo().uri + ")"
                            + " " + Lang.t("jukebox-by") + " [" + t.getInfo().author + "](" + t.getInfo().uri + ")");
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + t.getIdentifier() + "/0.jpg");

                    slashCommandEvent.replyEmbeds(embedBuilder.build()).queue();
                } catch (Exception e) {
                    slashCommandEvent.reply(Lang.t("jukebox-command-remove-error")).queue();
                }

                /*Collections.shuffle(audioTracks);

                BlockingQueue<AudioTrack> blockingQueue = new LinkedBlockingQueue<>();

                for (AudioTrack audioTrack : audioTracks)
                    blockingQueue.offer(audioTrack);

                Satania.getTrackScheduler().setQueue(blockingQueue);*/


                break;

            case "skip":
                if (Satania.getPlayer().getPlayingTrack() == null) {
                    slashCommandEvent.reply(Lang.t("jukebox-command-no-current")).queue();
                    return;
                }

                OptionMapping skipSize = slashCommandEvent.getOption("input");

                int skip = 0;

                if (skipSize != null) {
                    long skipLong = skipSize.getAsLong();

                    if (skipLong >= Satania.getTrackScheduler().getQueue().size()) {
                        slashCommandEvent.reply(Lang.t("jukebox-command-notenough")).queue();
                        return;
                    }

                    for (skip = 0; skip < skipLong; skip++)
                        Satania.getTrackScheduler().getQueue().remove();
                }

                Satania.getTrackScheduler().nextTrack();

                if (skipSize == null)
                    slashCommandEvent.reply(Lang.t("jukebox-command-skip-one")).queue();
                else
                    slashCommandEvent.reply(Lang.t("jukebox-command-skip-mult", String.valueOf(skip))).queue();

                break;

            case "stop":
                slashCommandEvent.reply(Lang.t("jukebox-command-stop")).queue();
                Satania.getPlayingGuild().getAudioManager().closeAudioConnection();
                break;

            case "shuffle":
                List<AudioTrack> audioTracks = new ArrayList<>(Satania.getTrackScheduler().getQueue());

                Collections.shuffle(audioTracks);

                BlockingQueue<AudioTrack> blockingQueue = new LinkedBlockingQueue<>();

                for (AudioTrack audioTrack : audioTracks)
                    blockingQueue.offer(audioTrack);

                Satania.getTrackScheduler().setQueue(blockingQueue);

                slashCommandEvent.reply(Lang.t("jukebox-command-shuffle")).queue();
                break;

            case "clear":
                Satania.getTrackScheduler().setQueue(new LinkedBlockingQueue<>());
                slashCommandEvent.reply(Lang.t("jukebox-command-clear")).queue();
                break;

            case "queue":
                AudioTrack current = Satania.getPlayer().getPlayingTrack();

                if (current == null) {
                    slashCommandEvent.reply(Lang.t("jukebox-command-no-current")).queue();
                    return;
                }

                List<AudioTrack> tracks = new ArrayList<>(Satania.getTrackScheduler().getQueue());

                int tracksSize = tracks.size();
                int pages = 0;

                if (!endsWithZero(tracksSize)) {
                    for (int i = 0; i < 11; i++) {
                        if (endsWithZero(tracksSize))
                            break;

                        tracksSize++;
                    }
                }

                pages = tracksSize / 10;

                OptionMapping pageOption = slashCommandEvent.getOption("input");
                int page = 0;

                if (pageOption != null)
                    page = ((int) pageOption.getAsLong()) - 1;

                if (page < 0)
                    page = 0;

                if ((page-1) > pages) { //todo bug
                    slashCommandEvent.reply(Lang.t("jukebox-command-queue-outofrange")).queue();
                    return;
                }

                slashCommandEvent.deferReply().queue();

                EmbedBuilder embedBuilder = new EmbedBuilder();
                String desc = "";
                if (tracks.size() == 0) {
                    embedBuilder.setTitle(Lang.t("jukebox-command-queue-now-playing"));
                    embedBuilder.setThumbnail("https://img.youtube.com/vi/" + current.getIdentifier() + "/0.jpg");
                    desc += "**[" + current.getInfo().title + "](" + current.getInfo().uri + ")** [" + Utils.musicDuration(current.getDuration()) + "]";
                } else {                                                                           // '~' because String.valueOf don't work?
                    embedBuilder.setTitle(Lang.t("jukebox-command-queue-queued-title", "~"+Satania.getTrackScheduler().getQueue().size()));
                    embedBuilder.setThumbnail(Lang.t("jukebox-command-plgif"));
                    desc = "__" + Lang.t("jukebox-command-queue-queued-now-playing") + "__\n" +
                            "**[" + current.getInfo().title + "](" + current.getInfo().uri + ")** [" + Utils.musicDuration(current.getDuration()) + "]\n" +
                            "\n" +
                            "__" + Lang.t("jukebox-command-queue-queued-incoming") + "__\n";

                    for (int i = (page*10); i < ((page*10)+10); i++) {
                        try{
                            AudioTrack audioTrack = tracks.get(i);
                            desc += "`" + (i+1) + ".` [" + audioTrack.getInfo().title + "](" + audioTrack.getInfo().uri + ") [" + Utils.musicDuration(audioTrack.getDuration()) + "]\n";
                        }catch (Exception e) {
                            break;
                        }
                    }

                    desc += "\n__" + Lang.t("jukebox-command-queue-queued-time") + "__ `" + Utils.musicDuration(Satania.getTrackScheduler().getQueueDuration()) + "`\n\n" +
                            "**PAGE " + (page+1) + " / " + pages + "**\n\n";
                }

                embedBuilder.setDescription(desc);

                slashCommandEvent.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
                break;
        }
    }

    private boolean endsWithZero(int i) {
        return Integer.toString(i).endsWith("0");
    }
}
