package fr.bakaaless.botzoe.bot.music;

public class SearchMessage {

    private final long id;
    private final long author;
    private final String[] links;

    public SearchMessage(final long id, final long author, final String[] links) {
        this.id = id;
        this.author = author;
        this.links = links;
    }

    public long getId() {
        return this.id;
    }

    public long getAuthor() {
        return this.author;
    }

    public String[] getLinks() {
        return this.links;
    }
}
