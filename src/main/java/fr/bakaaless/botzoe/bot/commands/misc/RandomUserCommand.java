package fr.bakaaless.botzoe.bot.commands.misc;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class RandomUserCommand implements CommandExecutor {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        final List<Member> members = event.getGuild().getMembers().stream().filter(member -> !member.getUser().isBot()).collect(Collectors.toList());
        final StringBuilder message = new StringBuilder();
        for (final String arg : arguments) {
            message.append(arg).append(" ");
        }
        event.getChannel().sendMessage(event.getMember().getEffectiveName() + " : " + message).queue(messages -> event.getChannel().sendMessage("---->     " + members.get(new Random().nextInt(arguments.size() + members.size()) % members.size()).getEffectiveName()).queue());
        event.getMessage().delete().queue();
    }

}
