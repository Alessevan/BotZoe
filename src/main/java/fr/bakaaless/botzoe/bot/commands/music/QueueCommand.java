package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class QueueCommand extends MusicCommand {

    @Override
    public void run(GuildMessageReceivedEvent event, String command, List<String> arguments) {
        if (!canSendHere(event) || event.getMember() == null)
            return;
        event.getMessage().delete().queue();
        final StringBuilder builder = new StringBuilder();
        MusicModule.get().getChannel().getTracks().forEach(track -> builder.append(track.getInfo().title).append(" - ").append(track.getInfo().author).append("\n"));
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Liste de la queue :")
                .setColor(Color.ORANGE)
                .setFooter(event.getMember().getEffectiveName(), event.getAuthor().getAvatarUrl())
                .setDescription(builder.toString())
                .setTimestamp(Instant.now()).build();
        event.getChannel().sendMessageEmbeds(embed).queue();
    }
}
