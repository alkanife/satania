package fr.alkanife.satania.commands;

import fr.alkanife.botcommons.CommandHandler;
import fr.alkanife.botcommons.Lang;
import fr.alkanife.satania.Satania;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Handler extends CommandHandler {

    public static int executed = 0;

    @Override
    public void success(SlashCommandEvent slashCommandEvent) {
        executed++;
    }

    @Override
    public void fail(SlashCommandEvent slashCommandEvent, Exception e) {
        Satania.getLogger().error("-----------------------");
        Satania.getLogger().error("Failed to execute a command");
        Satania.getLogger().error("Command name: " + slashCommandEvent.getName());
        Satania.getLogger().error("sub group " + slashCommandEvent.getSubcommandGroup());
        Satania.getLogger().error("sub name " + slashCommandEvent.getSubcommandName());

        for (OptionMapping optionMapping : slashCommandEvent.getOptions())
            Satania.getLogger().error("Option " + optionMapping.getName() + " -> " + optionMapping.getAsString());

        Satania.getLogger().error("Executed by " + slashCommandEvent.getUser().getName() + " (" + slashCommandEvent.getUser().getId() + ")");
        Satania.getLogger().error("In " + slashCommandEvent.getTextChannel().getName() + " (" + slashCommandEvent.getTextChannel().getId() + ")");

        Satania.getLogger().error(e.getCause().getMessage(), e);

        Satania.getLogger().error("-----------------------");

        slashCommandEvent.reply(Lang.t("command-error")).setEphemeral(true).queue();
    }
}