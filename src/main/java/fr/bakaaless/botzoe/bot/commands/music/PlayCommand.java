package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.regex.Matcher;

public class PlayCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!canSendHere(event) || event.getMember() == null)
            return;
        if (!isInChannel(event.getChannel(), event.getMember(), true))
            return;

        event.getMessage().delete().queue();
        if (arguments.size() == 0) {
            return;
        }
        final Matcher matcher = MusicModule.get().getYoutubeURL().matcher(arguments.get(0));
        String link = arguments.get(0);
        if (!matcher.find())
            link = "ytsearch:" + String.join(" ", arguments);
        MusicModule.get().getChannel().addMusicYoutubeLink(link, event.getAuthor().getIdLong());
    }

}
