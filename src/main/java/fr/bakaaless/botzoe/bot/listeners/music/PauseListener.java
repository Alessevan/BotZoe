package fr.bakaaless.botzoe.bot.listeners.music;

import fr.bakaaless.botzoe.bot.commands.music.MusicCommand;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class PauseListener extends ListenerAdapter {

    @Override
    public void onButtonClick(final @NotNull ButtonClickEvent event) {
        if (event.getButton() == null || event.getButton().getId() == null || event.getMember() == null || !MusicCommand.isInChannel(event.getTextChannel(), event.getMember(), false))
            return;
        final String buttonName = event.getButton().getId();
        if (!buttonName.equalsIgnoreCase("pauseMusic"))
            return;
        MusicModule.get().getChannel().pause();
        final MessageEmbed embed = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setFooter(event.getMember().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                .setColor(Color.BLACK)
                .setTitle(MusicModule.get().getChannel().isPaused() ? "La musique a été mise en pause" : "La musique est de nouveau en cours de lecture")
                .build();
        event.replyEmbeds(embed).queue();
    }
}
