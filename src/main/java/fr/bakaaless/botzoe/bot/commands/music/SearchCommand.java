package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.regex.Matcher;

public class SearchCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!super.canSendHere(event) || event.getMember() == null)
            return;
        if (!super.isInChannel(event.getChannel(), event.getMember(), true))
            return;

        event.getMessage().delete().queue();
        if (arguments.size() == 0) {
            return;
        }
        final Matcher matcher = MusicModule.get().getYoutubeURL().matcher(arguments.get(0));
        while (matcher.find()) {
            PlayCommand.run(event, arguments.get(0));
            return;
        }
    }

    public static void run(final GuildMessageReceivedEvent event, final List<String> arguments) {

    }

}
