package fr.bakaaless.botzoe.bot.listeners.music;

import fr.bakaaless.botzoe.bot.music.MusicModule;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SearchListener extends ListenerAdapter {

    @Override
    public void onButtonClick(final @NotNull ButtonClickEvent event) {
        final Message message = event.getMessage();
        if (!MusicModule.get().getChannel().isSearchResult(message.getIdLong()))
            return;
        if (event.getButton() == null || event.getButton().getId() == null || event.getMember() == null)
            return;
        final String[] buttonName = event.getButton().getId().split(":");
        if (!buttonName[0].equalsIgnoreCase("searchMusic") || !buttonName[1].equalsIgnoreCase(event.getMember().getId()))
            return;
        MusicModule.get().getChannel().addSearchedMessage(message.getIdLong(), Integer.parseInt(buttonName[2]));
    }

}
