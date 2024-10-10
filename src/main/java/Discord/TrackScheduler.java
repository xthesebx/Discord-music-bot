package Discord;

import Discord.App.AppQueue;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class TrackScheduler extends AudioEventAdapter {

	/**
	 * the queue
	 */
	public final BlockingQueue<AudioTrack> queue, queue2;
	private final List<AudioTrack> tracks;
	private final AudioPlayer player;
	private final Server server;

	/**
	 * boolean if its repeating mode
	 */
	public boolean repeating;
	private int i;

	/**
	 * current track
	 */
	public AudioTrack track;
	/**
	 * <p>Constructor for TrackScheduler.</p>
	 *
	 * @param server The discord server related, to get everything from
	 */
	public TrackScheduler(Server server) {
		this.server = server;
		this.tracks = new ArrayList<>();
		this.player = server.getPlayer();
		this.queue = new LinkedBlockingQueue<>();
		this.queue2 = new LinkedBlockingQueue<>();
	}
	
	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 * if we are in repeating mode add it to tracks instead of queue
	 * queue2 is the queue for song requests in streamer mode
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
			} else {
				this.track = track.makeClone();
				server.getAppInstances().forEach(instance -> {
					instance.getAppQueue().nextQueue();
				});
			}
		} else {
			tracks.add(track);
			if (player.startTrack(track, true))
				this.track = track.makeClone();
		}
		server.getDc().stopTimer();
		server.getAppInstances().forEach(instance -> {
			instance.getAppQueue().addQueue(track);
		});
	}

	public void request(AudioTrack track) {
		if (!player.startTrack(track, true)) {
			queue2.offer(track);
		} else {
			this.track = track.makeClone();
		}
		server.getDc().stopTimer();
		server.getAppInstances().forEach(instance -> {
			instance.getAppQueue().insertQueue(track, String.valueOf(queue2.size() - 1));
		});
	}
	
	/**
	 * Start the next track, skipping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		if (!repeating) {
			if (!queue2.isEmpty()) {
				track = queue2.poll();
				player.startTrack(track.makeClone(), false);
				return;
			}
			if (queue.isEmpty()) {
				player.stopTrack();
				track = null;
			} else {
				track = queue.poll();
				player.startTrack(track.makeClone(), false);
			}
		} else {
			if (i < tracks.size() - 1) i++;
			else i = 0;
			track = tracks.get(i);
			player.startTrack(track.makeClone(), false);
		}
		if (track == null) {
			server.getDc().startTimer();
		}
		server.getAppInstances().forEach(instance -> {
			instance.getAppQueue().nextQueue();
		});
	}
	
	/**
	 * {@inheritDoc}
	 * to start next track when track ended
	 */
	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		if (endReason.mayStartNext) {
			nextTrack();
		} else if (endReason.equals(AudioTrackEndReason.REPLACED)) {
			return;
		} else {
			queue.clear();
			server.getAppInstances().forEach(instance -> {
				instance.getAppQueue().clearQueue();
			});
		}
	}

	/**
	 * toggles the repeat functionality
	 */
	public void toggleRepeat() {
		if (repeating) {
			repeating = false;
			tracks.clear();
		} else {
			repeating = true;
			tracks.add(player.getPlayingTrack());
			queue.drainTo(tracks);
		}
	}

	public void removeFromQueue (JSONArray id) {
		AudioTrack[] temp2 = new AudioTrack[queue2.size()];
		AudioTrack[] temp = new AudioTrack[queue.size()];
		queue2.toArray(temp2);
		queue2.clear();
		queue.toArray(temp);
		queue.clear();

		id.forEach(o -> {
					int i = (int) o;
					if (i < temp2.length) {
						temp2[i] = null;
					} else temp[i - temp2.length] = null;
				});
		for (AudioTrack t : temp2) {
			if (t != null) queue2.offer(t);
		}
		for (AudioTrack t : temp) {
			if (t != null) queue.offer(t);
		}
	}

	public void move(int from, int to) {
		List<AudioTrack> temp = new ArrayList<>();
		List<AudioTrack> temp2 = new ArrayList<>();
		temp.addAll(queue);
		temp2.addAll(queue2);
		queue.clear();
		queue2.clear();
		AudioTrack track;
		if (from < temp2.size()) {
			track = temp2.get(from);
			temp2.remove(from);
		} else {
			track = temp.get(from - temp2.size());
			temp.remove(from - temp2.size());
		}
		if (to < temp2.size()) {
			temp2.add(to, track);
		} else {
			temp.add(to - temp2.size(), track);
		}
		queue.addAll(temp);
		queue2.addAll(temp2);
	}
}
