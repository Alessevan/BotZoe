package fr.bakaaless.botzoe.bot.music;

import java.util.HashMap;
import java.util.List;

public class MusicChannel {

    private final long channelId;

    private long messageId;

    private HashMap<Long, Long> searchResultMessages;

    public MusicChannel(final long channelId) {
        this.channelId = channelId;
    }
}
