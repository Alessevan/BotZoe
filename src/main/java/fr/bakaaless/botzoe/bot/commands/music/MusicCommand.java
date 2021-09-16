package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.Bot;
import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

public abstract class MusicCommand implements CommandExecutor {

    public boolean canSendHere(final GuildMessageReceivedEvent event) {
        return event.getChannel().getIdLong() == MusicModule.get().getChannel().getChannelId();
    }

    public boolean isInChannel(final Member member, final boolean join) {
        if (member.getVoiceState() == null)
            return false;
        if (member.getVoiceState().inVoiceChannel()) {
            final Guild guild = Bot.get().getJda().getGuildById(member.getGuild().getIdLong());
            if (guild == null)
                return false;
            final Member self = guild.getSelfMember();
            if (self.getVoiceState() == null)
                return false;
            if (self.getVoiceState().inVoiceChannel()) {
                return member.getVoiceState().getChannel().getIdLong() == self.getVoiceState().getChannel().getIdLong();
            } else if (join) {
                guild.getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
                return true;
            }
        }
        return false;
    }
}
