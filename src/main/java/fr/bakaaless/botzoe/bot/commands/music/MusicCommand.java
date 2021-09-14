package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class MusicCommand implements CommandExecutor {

    public boolean canSendHere(final GuildMessageReceivedEvent event) {
        return event.getChannel().getIdLong() == MusicModule.get().getChannel().getChannelId();
    }
}
