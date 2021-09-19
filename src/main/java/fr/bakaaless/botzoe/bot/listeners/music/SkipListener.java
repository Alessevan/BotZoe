package fr.bakaaless.botzoe.bot.listeners.music;

import fr.bakaaless.botzoe.bot.commands.music.MusicCommand;
import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class SkipListener  extends ListenerAdapter {

    @Override
    public void onButtonClick(final @NotNull ButtonClickEvent event) {
        if (event.getButton() == null || event.getButton().getId() == null || event.getMember() == null || !MusicCommand.isInChannel(event.getTextChannel(), event.getMember(), false))
            return;
        final String[] buttonName = event.getButton().getId().split(":");
        if (!buttonName[0].equalsIgnoreCase("skipMusic"))
            return;
        if (buttonName.length == 1) {
            final MessageEmbed embed = new EmbedBuilder()
                    .setTimestamp(Instant.now())
                    .setFooter(event.getMember().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                    .setColor(Color.BLACK)
                    .setTitle("Passer la musique")
                    .build();
            final Button button = Button.danger("skipMusic:" + event.getMember().getId(), "Confirmation");
            event.replyEmbeds(embed).setEphemeral(true).addActionRow(button).queue();
            return;
        } else if (!buttonName[1].equalsIgnoreCase(event.getMember().getId()))
            return;
        final MessageEmbed embed = new EmbedBuilder()
                .setTimestamp(Instant.now())
                .setFooter(event.getMember().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                .setColor(Color.BLACK)
                .setTitle("La prochaine musique va être jouée")
                .build();
        event.replyEmbeds(embed).queue();
        MusicModule.get().getChannel().skip();
    }
}
