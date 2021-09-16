package fr.bakaaless.botzoe.bot.music;

import fr.bakaaless.botzoe.starter.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MusicChannel {

    private final long channelId;

    private long messageId;

    private List<SearchMessage> searchResultMessages;

    public MusicChannel(final long channelId) {
        this.channelId = channelId;
        this.searchResultMessages = new ArrayList<>();
    }

    public boolean isSearchResult(final long messageId) {
        return this.searchResultMessages.stream().anyMatch(message -> message.getId() == messageId);
    }

    public void addSearchedMessage(final long messageId, final int musicIndex) {
        final Optional<SearchMessage> searchMessageOptional = this.searchResultMessages.stream().filter(message -> message.getId() == messageId).findFirst();
        if (searchMessageOptional.isEmpty())
            return;
        final SearchMessage searchMessage = searchMessageOptional.get();
        final String link = searchMessage.getLinks()[musicIndex];
        this.addMusicYoutubeLink(link, searchMessage.getAuthor());
        this.searchResultMessages.remove(searchMessage);
    }

    /**
     * Add a music to the queue.
     * @param link The music's link
     * @param authorId the user's authorId who ask to add the music
     */
    public void addMusicYoutubeLink(final String link, final long authorId) {

    }

    public long getChannelId() {
        return this.channelId;
    }
}
