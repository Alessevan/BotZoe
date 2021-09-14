package fr.bakaaless.botzoe.bot.commands.music;

import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class JoinCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!super.canSendHere(event))
            return;
    }

}
