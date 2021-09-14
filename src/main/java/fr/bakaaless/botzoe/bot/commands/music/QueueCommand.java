package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.util.List;

public class QueueCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!super.canSendHere(event))
            return;
    }
}
