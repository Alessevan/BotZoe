package fr.bakaaless.botzoe.bot.music;

import java.util.regex.Pattern;

public class MusicModule {

    private static MusicModule instance;

    public static MusicModule get() {
        return instance;
    }

    private final MusicChannel channel;
    private final Pattern youtubeURL = Pattern.compile("https?://(www.)?youtu(?:.be/|be.com/watch\\?v=)");

    public MusicModule(final MusicChannel channel) {
        this.channel = channel;
        instance = this;
    }

    public MusicChannel getChannel() {
        return this.channel;
    }

    public Pattern getYoutubeURL() {
        return this.youtubeURL;
    }
}
