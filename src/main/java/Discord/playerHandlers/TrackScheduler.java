package Discord.playerHandlers;

import Discord.Server;
import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
	public final List<AudioTrack> queue;
	/**
	 * the queue for requests from twitch stream in streamermode
	 */
	public final BlockingQueue<AudioTrack> queue2;
	private final AudioPlayer player;
	private final Server server;

	/**
	 * boolean if its repeating mode
	 */
	public RepeatState repeating = RepeatState.NO_REPEAT;
	/**
	 * the current position in queue
	 */
	public int i = 1;
	/**
	 * <p>Constructor for TrackScheduler.</p>
	 *
	 * @param server The discord server related, to get everything from
	 */
	public TrackScheduler(Server server) {
		this.server = server;
		this.player = server.getPlayer();
		this.queue = new ArrayList<>();
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
		queue.add(track);
		server.getAppInstances().forEach(instance -> instance.getAppQueue().addQueue(track));
		if (player.startTrack(track.makeClone(), true)) {
			server.getAppInstances().forEach(instance -> instance.getAppQueue().nextQueue());
		}
	}


	/**
	 * Song requests from twitch chat
	 * get added to sperate queue
	 *
	 * @param track a {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} object
	 */
	public void request(AudioTrack track) {
		if (!player.startTrack(track, true)) {
			queue2.offer(track);
		} else server.getAppInstances().forEach(instance -> instance.getAppQueue().nextQueue());
		server.getAppInstances().forEach(instance -> {
			instance.getAppQueue().insertQueue(track, String.valueOf(queue2.size() - 1));
		});
		server.getDc().stopTimer();
	}


	/**
	 * Start the next track, skipping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		switch (repeating) {
			case NO_REPEAT -> {
				if (!queue2.isEmpty()) {
					player.startTrack(queue2.poll(), false);
				} else if (i < queue.size()) {
					player.startTrack(queue.get(i).makeClone(), false);
					i++;
				} else {
					player.stopTrack();
					i++;
					server.getDc().startTimer();
				}
			}
			case REPEAT_SINGLE -> {
				if (!queue2.isEmpty()) {
					player.startTrack(queue2.poll(), false);
				} else player.startTrack(queue.get(i - 1).makeClone(), false);
			}
			case REPEAT_QUEUE -> {
				if (!queue2.isEmpty()) {
					player.startTrack(queue2.poll(), false);
				} else if (i < queue.size()) {
					player.startTrack(queue.get(i).makeClone(), false);
					i++;
				} else {
					i = 0;
					player.startTrack(queue.get(i).makeClone(), false);
					i++;
				}
			}
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
		//TODO: add a way to handle the error of failing to load something in a way that is okay for every source (requests, app, discord)
		//TODO: probably have to add more info to the track which could cause issues, not sure how to do it rn.
		if (endReason.mayStartNext) {
			if (endReason.equals(AudioTrackEndReason.LOAD_FAILED))
				Logger.error("loading " + track.getInfo().title +  " from source " + track.getInfo().uri +
						" failed. might be spotify issue or youtube dying again.");
			nextTrack();
		} else if (endReason.equals(AudioTrackEndReason.REPLACED)) {
			return;
		} else {
			queue.clear();
			i = 1;
			server.getAppInstances().forEach(instance -> {
				instance.getAppQueue().clearQueue();
			});
		}
	}

	/**
	 * toggles the repeat functionality
	 *
	 * @return a {@link Discord.playerHandlers.RepeatState} object
	 */
	public RepeatState toggleRepeat() {
		switch (repeating) {
			case NO_REPEAT -> repeating = RepeatState.REPEAT_QUEUE;
			case REPEAT_QUEUE -> repeating = RepeatState.REPEAT_SINGLE;
			case REPEAT_SINGLE -> repeating = RepeatState.NO_REPEAT;
		}
		server.getAppInstances().forEach(instance -> instance.getAppQueue().repeat());
		return repeating;
	}

	/**
	 * <p>removeFromQueue.</p>
	 *
	 * @param id a {@link org.json.JSONArray} object
	 */
	public void removeFromQueue (JSONArray id) {
		AudioTrack[] temp2 = new AudioTrack[queue2.size()];
		AudioTrack[] temp = new AudioTrack[queue.size()];
		queue2.toArray(temp2);
		queue2.clear();
		queue.toArray(temp);
		queue.clear();
		AtomicInteger j = new AtomicInteger();
		id.forEach(o -> {
					int i = (int) o;
					if (i < temp2.length) {
						temp2[i + j.get()] = null;
					} else temp[i - temp2.length + j.get() + this.i] = null;
					j.getAndIncrement();
				});
		for (AudioTrack t : temp2) {
			if (t != null) queue2.offer(t);
		}
		for (AudioTrack t : temp) {
			if (t != null) queue.add(t);
		}
	}

	/**
	 * <p>move.</p>
	 *
	 * @param from a int
	 * @param to a int
	 */
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
			track = temp.get(from - temp2.size() + i);
			temp.remove(from - temp2.size() + i);
		}
		if (to < temp2.size()) {
			temp2.add(to, track);
		} else {
			temp.add(to - temp2.size() + i, track);
		}
		queue.addAll(temp);
		queue2.addAll(temp2);
	}

	/**
	 * <p>previousTrack.</p>
	 */
	public void previousTrack() {
		if (i < 2) return;
		i--;
		i--;
		server.getAppInstances().forEach(instance -> {
			instance.getAppQueue().insertQueue(queue.get(i), "0");
			instance.getAppQueue().insertQueue(queue.get(i + 1), "1");
		});
		nextTrack();
	}
}
