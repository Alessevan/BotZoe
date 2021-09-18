package fr.bakaaless.botzoe.bot.commands.music;

import fr.bakaaless.botzoe.bot.Bot;
import fr.bakaaless.botzoe.bot.commands.CommandExecutor;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public abstract class MusicCommand implements CommandExecutor {

    public boolean canSendHere(final GuildMessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == MusicModule.get().getChannel().getChannelId()) {
            return true;
        }
        if (event.getMember() != null)
            notInTextChannel(event.getChannel(), event.getMember());
        return false;
    }

    public boolean isInChannel(final TextChannel channel, final Member member, final boolean join) {
        if (member.getVoiceState() != null && member.getVoiceState().inVoiceChannel()) {
            final Guild guild = Bot.get().getJda().getGuildById(member.getGuild().getIdLong());
            if (guild == null)
                return false;
            final Member self = guild.getSelfMember();
            if (self.getVoiceState() != null && self.getVoiceState().inVoiceChannel()) {
                if (member.getVoiceState().getChannel().getIdLong() == self.getVoiceState().getChannel().getIdLong())
                    return true;
                this.alreadyInChannel(channel, member);
                return false;
            } else if (join) {
                try {
                    guild.getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
                    return true;
                } catch (InsufficientPermissionException ignored) {
                    this.cannotJoinChannel(channel, member);
                    return false;
                }
            } else {
                this.selfNotInChannel(channel, member);
                return false;
            }
        }
        this.notInChannel(channel, member);
        return false;
    }

    public void notInTextChannel(final TextChannel channel, final Member member) {
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Erreur")
                .setColor(Color.RED)
                .setDescription("Vous ne pouvez utiliser les commandes de musique que dans le salon <#" + MusicModule.get().getChannel().getChannelId() + ">.")
                .setTimestamp(Instant.now())
                .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();
        channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
    }

    public void notInChannel(final TextChannel channel, final Member member) {
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Erreur")
                .setColor(Color.RED)
                .setDescription("Vous n'êtes pas dans un salon vocal.")
                .setTimestamp(Instant.now())
                .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();
        channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
    }

    public void selfNotInChannel(final TextChannel channel, final Member member) {
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Erreur")
                .setColor(Color.RED)
                .setDescription("Le bot n'est pas dans un salon vocal.")
                .setTimestamp(Instant.now())
                .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();
        channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
    }

    public void alreadyInChannel(final TextChannel channel, final Member member) {
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Erreur")
                .setColor(Color.RED)
                .setDescription("Le bot est déjà dans un salon vocal différent du vôtre.")
                .setTimestamp(Instant.now())
                .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();
        channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
    }

    public void cannotJoinChannel(final TextChannel channel, final Member member) {
        final MessageEmbed embed = new EmbedBuilder()
                .setAuthor("Erreur")
                .setColor(Color.RED)
                .setDescription("Le bot ne peut pas aller dans votre salon.")
                .setTimestamp(Instant.now())
                .setFooter(member.getEffectiveName(), member.getUser().getAvatarUrl())
                .build();
        channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
    }
}
