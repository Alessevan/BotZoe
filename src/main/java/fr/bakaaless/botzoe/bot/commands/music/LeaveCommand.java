package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class LeaveCommand extends MusicCommand {
    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!super.canSendHere(event) || event.getMember() == null)
            return;
        if (!super.isInChannel(event.getChannel(), event.getMember(), false))
            return;
        event.getMessage().delete().queue();
        MusicModule.get().getChannel().reset();
        event.getGuild().getAudioManager().closeAudioConnection();
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Le bot a quitté le salon")
                .setColor(Color.BLACK)
                .setFooter(event.getMember().getEffectiveName(), event.getAuthor().getAvatarUrl())
                .setTimestamp(Instant.now()).build();
        event.getChannel().sendMessageEmbeds(embed).queue();
    }
}
