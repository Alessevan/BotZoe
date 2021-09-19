package fr.bakaaless.botzoe.bot.listeners.music;

import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;

public class RemoveListener extends ListenerAdapter {

    @Override
    public void onButtonClick(final @NotNull ButtonClickEvent event) {
        if (event.getButton() == null || event.getButton().getId() == null || event.getMember() == null || event.getGuild().getAudioManager().getConnectedChannel() != event.getMember().getVoiceState().getChannel())
            return;
        final String[] buttonName = event.getButton().getId().split(":");
        if (!buttonName[0].equalsIgnoreCase("removeMusic") || !buttonName[1].equalsIgnoreCase(event.getMember().getId()))
            return;
        final EmbedBuilder embed = new EmbedBuilder()
                .setColor(Color.BLACK)
                .setFooter(event.getMember().getEffectiveName(), event.getMember().getUser().getAvatarUrl())
                .setTimestamp(Instant.now());
        if (buttonName[2].equalsIgnoreCase("all")) {
            MusicModule.get().getChannel().getTracks().clear();
            embed.setAuthor("La liste d'attente des musiques a été entièrement vidée");
        } else {
            if (!buttonName[2].contains("-")) {
                for (int i = 0; i < Integer.parseInt(buttonName[2]) && MusicModule.get().getChannel().getTracks().size() > 0; i++) {
                    MusicModule.get().getChannel().getTracks().removeFirst();
                }
                embed.setAuthor("La liste d'attente a été vidée de ces " + buttonName[2] + " premières musiques");
            } else {
                final int start = Integer.parseInt(buttonName[2].split("-")[0]);
                final int end = Integer.parseInt(buttonName[2].split("-")[1]);
                for (int i = 0; i < end - start && MusicModule.get().getChannel().getTracks().size() >= start; i++) {
                    MusicModule.get().getChannel().getTracks().remove(start - 1);
                }
                embed.setAuthor("La liste d'attente a été vidée des musiques entre les positions " + start + " et " + end);
            }
        }
        event.replyEmbeds(embed.build()).queue();
        event.getMessage().delete().queue();
    }

}
