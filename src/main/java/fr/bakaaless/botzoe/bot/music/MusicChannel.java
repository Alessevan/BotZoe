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
import net.dv8tion.jda.api.interactions.components.Button;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

public class MusicChannel {

    private final long channelId;
    private long messageId;

    private final AudioPlayerManager playerManager;
    private final AudioPlayer audioPlayer;
    private final TrackScheduler tracks;
    private final HashMap<AudioTrack, Long> musicExecutor;

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

        this.musicExecutor = new HashMap<>();
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
                if (track.getInfo().isStream) {
                    loadFailed(new FriendlyException("La vidéo est un stream en cours", FriendlyException.Severity.COMMON, null));
                    return;
                }
                play(channel, track, authorId);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                if (firstTrack.getInfo().isStream) {
                    loadFailed(new FriendlyException("La vidéo est un stream en cours", FriendlyException.Severity.COMMON, null));
                    return;
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
                if (!exception.getMessage().equalsIgnoreCase("La vidéo est un stream en cours"))
                    exception.printStackTrace();
            }
        });
    }

    private void play(final TextChannel channel, final AudioTrack track, final long authorId) {
        this.musicExecutor.put(track, authorId);
        if (this.audioPlayer.getPlayingTrack() != null) {
            final Member user = channel.getGuild().getMemberById(authorId);
            final Matcher matcher = MusicModule.get().getYoutubeURL().matcher(track.getInfo().uri);
            final EmbedBuilder embed = new EmbedBuilder()
                    .setAuthor("Ajout d'une piste")
                    .addField("Titre", track.getInfo().title, true)
                    .addField("Auteur", track.getInfo().author.replace(" - Topic", ""), true)
                    .addField("Durée", fromDuration(track.getDuration()), true)
                    .setColor(Color.ORANGE)
                    .setTimestamp(Instant.now())
                    .setFooter(user != null ? user.getEffectiveName() : "", user != null ? user.getUser().getAvatarUrl() : "")
                    ;
            if (matcher.find()) {
                final String youtubeId = matcher.group(2);
                embed.setAuthor("Joue maintenant : " + track.getInfo().title, track.getInfo().uri)
                        .setImage("https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg");
            }
            channel.sendMessageEmbeds(embed.build()).queue();
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
                final Matcher matcher = MusicModule.get().getYoutubeURL().matcher(track.getInfo().uri);
                final EmbedBuilder embed = new EmbedBuilder()
                        .setAuthor("Joue maintenant : " + track.getInfo().title)
                        .setDescription("Informations sur la musique :")
                        .addField("Durée", fromDuration(track.getDuration()), true)
                        .addField("Artiste", track.getInfo().author.replace(" - Topic", ""), true)
                        .setColor(Color.GREEN)
                        .setTimestamp(Instant.now())
                        ;
                if (this.musicExecutor.containsKey(track)) {
                    final long authorId = this.musicExecutor.remove(track);
                    final Member user = channel.getGuild().getMemberById(authorId);
                    embed.setFooter(user != null ? user.getEffectiveName() : "", user != null ? user.getUser().getAvatarUrl() : "");
                }
                if (matcher.find()) {
                    final String youtubeId = matcher.group(2);
                    embed.setAuthor(track.getInfo().title + " - " + track.getInfo().author, track.getInfo().uri)
                            .setImage("https://img.youtube.com/vi/" + youtubeId + "/maxresdefault.jpg");
                }
                final Button pause = Button.primary("pauseMusic", "Mettre en pause");
                final Button stop = Button.danger("stopMusic", "Arrêter la musique");
                final Button skip = Button.primary("skipMusic", "Passer la musique");
                channel.sendMessageEmbeds(embed.build()).setActionRow(pause, stop, skip).queue(message -> this.messageId = message.getIdLong());
            }
        }
    }

    private String fromDuration(final long duration) {
        final int seconds = (int) (duration / 1000) % 60;
        final int minutes = (int) (duration / 1000D) / 60;
        return (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
    }

    public void pause() {
        this.audioPlayer.setPaused(!this.audioPlayer.isPaused());
    }

    public boolean isPaused() {
        return this.audioPlayer.isPaused();
    }

    public void resetMessageId() {
        if (this.channelId != 0L) {
            final TextChannel channel = Bot.get().getJda().getTextChannelById(this.channelId);
            if (channel != null && this.messageId != 0L) {
                channel.retrieveMessageById(this.messageId).queue(message -> message.delete().queue());
                this.messageId = 0L;
            }
        }
    }

    public void reset() {
        this.tracks.getTracks().clear();
        this.audioPlayer.stopTrack();
        this.resetMessageId();
        Bot.get().getJda().getPresence().setActivity(null);
    }

    public void skip() {
        this.tracks.nextTrack();
    }

    public LinkedList<AudioTrack> getTracks() {
        return this.tracks.getTracks();
    }
}
