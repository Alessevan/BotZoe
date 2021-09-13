package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.music.YoutubeSearch;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayCommand implements CommandExecutor {

    private final static Pattern url = Pattern.compile("https?://youtu(?:.be/|be.com/watch?v=)");

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {

        event.getMessage().delete().queue();
        if (arguments.size() == 0) {
            return;
        }
        final Matcher matcher = url.matcher(arguments.get(0));
        if (matcher.find()) {
            final String link = matcher.group();

            return;
        }
        final StringBuilder query = new StringBuilder();
        for (final String argument : arguments)
            query.append(argument);
        try {
            System.out.println("e1");
            YoutubeSearch.youtubeSearch(5, query.toString()).forEach(searchResult -> System.err.println(searchResult.getTitle()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
