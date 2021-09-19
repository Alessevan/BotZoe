package fr.bakaaless.botzoe.bot.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import fr.bakaaless.botzoe.bot.Bot;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Class excerpt from https://github.com/sedmelluq/lavaplayer/blob/master/demo-jda/src/main/java/com/sedmelluq/discord/lavaplayer/demo/jda/TrackScheduler.java
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private final AudioPlayer player;
    private final LinkedList<AudioTrack> queue;

    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(final AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedList<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(final AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        } else {
            MusicModule.get().getChannel().generateMessage(track);
            Bot.get().getJda().getPresence().setActivity(Activity.listening(track.getInfo().title));
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        final AudioTrack toPlay = this.queue.poll();
        if (this.player.startTrack(toPlay, false)) {
            MusicModule.get().getChannel().generateMessage(toPlay);
            Bot.get().getJda().getPresence().setActivity(Activity.listening(toPlay.getInfo().title));
        } else {
            Bot.get().getJda().getPresence().setPresence(OnlineStatus.ONLINE, null, true);
        }

    }

    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        MusicModule.get().getChannel().resetMessageId();
        if (endReason.mayStartNext) {
            this.nextTrack();
        }
    }

    public LinkedList<AudioTrack> getTracks() {
        return this.queue;
    }
}
