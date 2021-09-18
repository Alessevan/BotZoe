package fr.bakaaless.botzoe.bot.music;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import fr.bakaaless.botzoe.bot.Bot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class MusicChannel {

    private final long channelId;
    private long messageId;

    private final AudioPlayerManager playerManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler tracks;
    private List<SearchMessage> searchResultMessages;

    public MusicChannel(final long channelId) {
        this.channelId = channelId;
        this.playerManager = new DefaultAudioPlayerManager();
        this.playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);

        this.audioPlayer = this.playerManager.createPlayer();

        this.tracks = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.tracks);
        Bot.get().getJda().getGuilds().forEach(guild -> guild.getAudioManager().closeAudioConnection());

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
        if (this.channelId == 0L)
            return;
        final TextChannel channel = Bot.get().getJda().getTextChannelById(this.channelId);
        if (channel == null)
            return;

        final Member user = channel.getGuild().getMemberById(authorId);
        Bot.get().getJda().getTextChannelById(this.channelId).getGuild().getAudioManager().setSendingHandler(new AudioPlayerSendHandler(this.audioPlayer));
        this.playerManager.loadItemOrdered(this.playerManager, link, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(final AudioTrack track) {
                play(channel, track, authorId);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                play(channel, firstTrack, authorId);
            }

            @Override
            public void noMatches() {
                final MessageEmbed embed = new EmbedBuilder()
                        .setAuthor("Erreur")
                        .setColor(Color.RED)
                        .setDescription("Impossible de trouver une musique avec le lien " + link + ".")
                        .setTimestamp(Instant.now())
                        .setFooter(user != null ? user.getEffectiveName() : "", user != null ? user.getUser().getAvatarUrl() : "")
                        .build();
                channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                final MessageEmbed embed = new EmbedBuilder()
                        .setAuthor("Erreur")
                        .setColor(Color.RED)
                        .setDescription("Impossible de charger la piste. ")
                        .addField("Sortie : ", exception.getMessage(), true)
                        .setTimestamp(Instant.now())
                        .setFooter(user != null ? user.getEffectiveName() : "", user != null ? user.getUser().getAvatarUrl() : "")
                        .build();
                channel.sendMessageEmbeds(embed).queue(message -> message.delete().queueAfter(10L, TimeUnit.SECONDS));
                exception.printStackTrace();
            }
        });
    }

    private void play(final TextChannel channel, final AudioTrack track, final long authorId) {
        if (this.audioPlayer.getPlayingTrack() != null) {
            final Member user = channel.getGuild().getMemberById(authorId);
            final MessageEmbed embed = new EmbedBuilder()
                    .setAuthor("Ajout d'une piste", track.getInfo().uri)
                    .setDescription(track.getInfo().title + " - " + track.getInfo().author + System.lineSeparator())
                    .setColor(Color.ORANGE)
                    .setTimestamp(Instant.now())
                    .setFooter(user != null ? user.getEffectiveName() : "", user != null ? user.getUser().getAvatarUrl() : "")
                    .build();
            channel.sendMessageEmbeds(embed).queue();
        }
        this.tracks.queue(track);
    }

    public long getChannelId() {
        return this.channelId;
    }

    public void generateMessage(final AudioTrack track) {
        if (this.channelId != 0L) {
            final TextChannel channel = Bot.get().getJda().getTextChannelById(this.channelId);
            if (channel != null) {
                final MessageEmbed embed = new EmbedBuilder()
                        .setAuthor(track.getInfo().title + " - " + track.getInfo().author, track.getInfo().uri)
                        .setColor(Color.GREEN)
                        .setTimestamp(Instant.now())
                        .build();
                channel.sendMessageEmbeds(embed).queue(message -> this.messageId = message.getIdLong());
            }
        }
    }

    public void resetMessageId() {
        if (this.channelId != 0L) {
            final TextChannel channel = Bot.get().getJda().getTextChannelById(this.channelId);
            if (channel != null) {
                channel.retrieveMessageById(this.messageId).queue(message -> message.delete().queue());
                this.messageId = 0L;
            }
        }
    }

    public void skip() {
        this.tracks.nextTrack();
    }

    public LinkedList<AudioTrack> getTracks() {
        return this.tracks.getTracks();
    }
}
