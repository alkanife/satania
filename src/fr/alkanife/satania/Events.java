package fr.alkanife.satania;

import fr.alkanife.botcommons.Lang;
import fr.alkanife.botcommons.Utils;
import fr.alkanife.botcommons.YMLReader;
import fr.alkanife.satania.music.Music;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class Events extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent readyEvent) {
        Satania.getLogger().info("Connected to Discord");

        Music.initialize();

        //commands(readyEvent);

        Satania.getLogger().info("Ready!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent messageReceivedEvent) {
        if (!messageReceivedEvent.getChannelType().equals(ChannelType.PRIVATE))
            return;

        if (!messageReceivedEvent.getAuthor().getId().equalsIgnoreCase(Satania.getCreatorID()))
            return;

        String message = messageReceivedEvent.getMessage().getContentRaw().toLowerCase(Locale.ROOT);

        switch (message) {
            case "reboot":
                messageReceivedEvent.getMessage().reply("Rebooooooooooting").queue(message1 -> {
                    Satania.getLogger().info("Shutting down");
                    Satania.getJda().shutdownNow();
                    System.exit(0);
                });
                break;

            case "reload":
                Satania.getLogger().info("Reloading");

                try {
                    Satania.setConfigurationValues(YMLReader.read("configuration"));
                } catch (FileNotFoundException e) {
                    messageReceivedEvent.getMessage().reply("Failed @ configuration").queue();
                    e.printStackTrace();
                    return;
                }

                Object allowed = Satania.getConfigurationValues().get("allowed");

                if (allowed == null) {
                    Satania.getLogger().error("Invalid allowed IDs");
                    messageReceivedEvent.getMessage().reply("Failed @ allowed ids").queue();
                    return;
                }

                Satania.setAllowed(new ArrayList<>());
                Satania.getAllowed().add(Satania.getCreatorID());
                String allowedFull = String.valueOf(allowed);
                String[] alloweds = allowedFull.split(", ");

                Satania.getAllowed().addAll(Arrays.asList(alloweds));

                Satania.getLogger().info(Satania.getAllowed().size() + " allowed users");

                try {
                    Lang.load();
                } catch (Exception e) {
                    messageReceivedEvent.getMessage().reply("Failed @ lang").queue();
                    e.printStackTrace();
                    return;
                }

                Satania.getLogger().info(Lang.getTranslations().size() + " loaded translations");
                Satania.getLogger().info("Reload complete");

                messageReceivedEvent.getMessage().reply("Reload complete - " + Satania.getAllowed().size() + " allowed, " + Lang.getTranslations().size() + " translations").queue();
                break;

            case "info":
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setThumbnail(messageReceivedEvent.getJDA().getSelfUser().getAvatarUrl());

                StringBuilder description = new StringBuilder();
                description.append("updays : `").append(Utils.getUpDays()).append("`\n");
                description.append("version : `").append(Satania.getVersion()).append("`\n");
                description.append("[default playlist](").append(Satania.getDefaultPlaylist()).append(")\n");
                description.append("creator-id : `").append(Satania.getCreatorID()).append("`\n");
                description.append("allowed IDs :\n");
                for (String a : Satania.getAllowed())
                    description.append(" - `").append(a).append("`\n");

                embedBuilder.setDescription(description);

                messageReceivedEvent.getMessage().reply(embedBuilder.build()).queue();
                break;

            default:
                messageReceivedEvent.getMessage().reply("`Commandes : reboot, reload, info`").queue();
                break;
        }
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Satania.getHandler().handle(event);
    }

    private void commands(ReadyEvent readyEvent) {
        CommandData jukebox = new CommandData("jukebox", "Commandes du jukebox");

        SubcommandData jukeboxPlay = new SubcommandData("play", "Rejoindre un salon vocal et démarrer le jukebox")
                .addOption(OptionType.STRING, "input", "Un lien YT ou un titre", true);

        SubcommandData jukeboxPlaynext = new SubcommandData("play_next", "Ajoute l'entrée à la toute première position dans la file d'attente")
                .addOption(OptionType.STRING, "input", "Un lien YT ou un titre", true);

        SubcommandData jukeboxSkip = new SubcommandData("skip", "Passer une ou plusieurs musiques")
                .addOption(OptionType.INTEGER, "input", "Nombre de musiques à passer", false);

        SubcommandData jukeboxShuffle = new SubcommandData("shuffle", "Mélanger la file d'attente");
        SubcommandData jukeboxQueue = new SubcommandData("queue", "Voir la file d'attente");
        SubcommandData jukeboxStop = new SubcommandData("stop", "Arrêter le jukebox");
        SubcommandData jukeboxClear = new SubcommandData("clear", "Vider la file d'attente");
        jukebox.addSubcommands(jukeboxPlay, jukeboxPlaynext, jukeboxSkip, jukeboxShuffle, jukeboxQueue, jukeboxStop, jukeboxClear);

        CommandData serverinfo = new CommandData("serverinfo", "Donne des infos sympa sur le serveur");
        CommandData memberinfo = new CommandData("memberinfo", "Donne des infos sympa sur un membre")
                .addOption(OptionType.USER, "input", "Membre concerné", true);
        CommandData emoteinfo = new CommandData("emoteinfo", "Donne des infos sympa sur une emote")
                .addOption(OptionType.STRING, "input", "L'emote", true);

        readyEvent.getJDA().updateCommands().addCommands(jukebox, serverinfo, memberinfo, emoteinfo).queue();
    }

}