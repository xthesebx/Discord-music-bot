package Discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer player;
	/**
	 * the queue for the server
	 */
	public final BlockingQueue<AudioTrack> queue;
	private final Server server;
	public boolean repeating;
	private int i;
	private List<AudioTrack> tracks;
	/**
	 * <p>Constructor for TrackScheduler.</p>
	 *
	 * @param server The server to get everything from
	 */
	public TrackScheduler(Server server) {
		this.player = server.getPlayer();
		this.queue = new LinkedBlockingQueue<>();
		this.server = server;
		this.tracks = new ArrayList<>();
	}
	
	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(AudioTrack track) {
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		if (!repeating) {
			if (!player.startTrack(track, true)) {
				queue.offer(track);
			}
		} else {
			tracks.add(track);
		}
	}
	
	/**
	 * Start the next track, skipping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		AudioTrack track;
		if (!repeating) {
			track = queue.poll();
			player.startTrack(track, false);
		} else {
			if (i < tracks.size() - 1) i++;
			else i = 0;
			track = tracks.get(i);
			player.startTrack(track.makeClone(), false);
		}
		if (track == null) {
			server.getDc().startTimer();
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		} else if (endReason.equals(AudioTrackEndReason.REPLACED)) {
			return;
		} else {
			queue.clear();
		}
	}

	public void Repeat() {
		if (repeating) {
			repeating = false;
		} else {
			repeating = true;
			tracks.add(player.getPlayingTrack());
			queue.drainTo(tracks);
		}
	}
}
