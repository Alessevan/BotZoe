package fr.bakaaless.botzoe.bot.commands.elections;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.events.Elections;
import net.dv8tion.jda.api.entities.Category;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ElectionsCommand implements CommandExecutor {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {

        event.getMessage().delete().queue();
        final Category category = event.getChannel().getManager().getChannel().getParent();
        if (category == null)
            return;
        if (Elections.getCurrentElections() != null) {
            event.getChannel().sendMessage(":warning: Des élections sont déjà en cours.").queue(deleteMessage -> {
                try {
                    deleteMessage.delete().queueAfter(5, TimeUnit.SECONDS);
                } catch (Exception ignored) {}
            });
            return;
        }
        category.createTextChannel("elections").queue(textChannel -> {
            new Elections(event.getGuild().getIdLong(), textChannel.getIdLong());
        });
    }

}
