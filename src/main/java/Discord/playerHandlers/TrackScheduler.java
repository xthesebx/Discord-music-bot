package Discord.playerHandlers;

import Discord.App.AppInstance;
import Discord.NewMain;
import Discord.Server;
import com.hawolt.logger.Logger;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import dev.arbjerg.lavalink.client.player.Track;
import dev.arbjerg.lavalink.protocol.v4.Message;
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
public class TrackScheduler {

	/**
	 * the queue
	 */
	public final List<Track> queue;
	/**
	 * the queue for requests from twitch stream in streamermode
	 */
	public final BlockingQueue<Track> queue2;
	private LavalinkPlayer player;
	private final Server server;
	public boolean ready = false;

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
		this.player = server.getPlayer().isPresent() ? server.getPlayer().get() : null;
		this.queue = new ArrayList<>();
		this.queue2 = new LinkedBlockingQueue<>();
	}

	public void queue(List<Track> tracks) {
		server.getDc().stopTimer();
		queue.addAll(tracks);
		new Thread(() -> {
			for (Track track : tracks) {
				server.getAppInstances().values().forEach(instance -> instance.getAppQueue().addQueue(track));
			}
		}).start();
		if (server.getPlayer().isEmpty() || server.getPlayer().get().getTrack() == null) {
			server.getPlayer().ifPresentOrElse(player -> player.setTrack(tracks.get(0).makeClone()).setVolume(server.getVolume()).subscribe(), () -> {
				NewMain.client.getOrCreateLink(server.getGuildId()).createOrUpdatePlayer().setTrack(tracks.get(0)).setVolume(server.getVolume()).subscribe();
			});
			server.getAppInstances().values().forEach(instance -> instance.getAppQueue().nextQueue());
		}
	}
	
	/**
	 * Add the next track to queue or play right away if nothing is in the queue.
	 * if we are in repeating mode add it to tracks instead of queue
	 * queue2 is the queue for song requests in streamer mode
	 *
	 * @param track The track to play or add to queue.
	 */
	public void queue(Track track) {
		server.getDc().stopTimer();
		// Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
		// something is playing, it returns false and does nothing. In that case the player was already playing so this
		// track goes to the queue instead.
		queue.add(track);
		server.getAppInstances().values().forEach(instance -> instance.getAppQueue().addQueue(track));
		if (server.getPlayer().isEmpty() || server.getPlayer().get().getTrack() == null) {
			server.getPlayer().ifPresentOrElse(player -> player.setTrack(track.makeClone()).setVolume(server.getVolume()).subscribe(), () -> {
				NewMain.client.getOrCreateLink(server.getGuildId()).createOrUpdatePlayer().setTrack(track.makeClone()).setVolume(server.getVolume()).setPaused(false).subscribe();
			});
			server.getAppInstances().values().forEach(instance -> instance.getAppQueue().nextQueue());
		}
	}


	public void request(Track track) {
		if (server.getPlayer().isPresent() && server.getPlayer().get().getTrack() != null) {
			queue2.offer(track);
		} else server.getAppInstances().values().forEach(instance -> instance.getAppQueue().nextQueue());
		server.getAppInstances().values().forEach(instance -> instance.getAppQueue().insertQueue(track, String.valueOf(queue2.size() - 1)));
		server.getDc().stopTimer();
	}


	/**
	 * Start the next track, skipping the current one if it is playing.
	 */
	public void nextTrack() {
		// Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
		// giving null to startTrack, which is a valid argument and will simply stop the player.
		player = server.getPlayer().get();
		switch (repeating) {
			case NO_REPEAT -> {
				if (!queue2.isEmpty()) {
					player.setTrack(queue2.poll()).subscribe();
				} else if (i < queue.size()) {
					player.setTrack(queue.get(i).makeClone()).subscribe();
					i++;
				} else {
					player.stopTrack().subscribe();
					i++;
					server.getDc().startTimer();
					server.getAppInstances().values().forEach(AppInstance::setIdlePresence);
				}
			}
			case REPEAT_SINGLE -> {
				if (!queue2.isEmpty()) {
					player.setTrack(queue2.poll()).subscribe();
				} else {
					player.setTrack(queue.get(i - 1).makeClone()).subscribe();
					server.getAppInstances().values().forEach(AppInstance::repeat);
				}
				return;
			}
			case REPEAT_QUEUE -> {
				if (!queue2.isEmpty()) {
					player.setTrack(queue2.poll()).subscribe();
				} else if (i < queue.size()) {
					player.setTrack(queue.get(i).makeClone()).subscribe();
					i++;
				} else {
					i = 0;
					player.setTrack(queue.get(i).makeClone()).subscribe();
					server.getAppInstances().values().forEach(instance -> instance.getAppQueue().initQueue(true));
					i++;
				}
			}
		}
		server.getAppInstances().values().forEach(instance -> instance.getAppQueue().nextQueue());
	}
	
	/**
	 * {@inheritDoc}
	 * to start next track when track ended
	 */
	public void onTrackEnd(Track track, Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason endReason) {
		// Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
		//TODO: add a way to handle the error of failing to load something in a way that is okay for every source (requests, app, discord)
		//TODO: probably have to add more info to the track which could cause issues, not sure how to do it rn.
		if (endReason.getMayStartNext()) {
			if (endReason.equals(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.LOAD_FAILED)) {
				server.getGuild().getJDA().retrieveUserById(277064996264083456L).complete().openPrivateChannel().complete().sendMessage("MUSIC BOT DYING PLS HELP").queue();
				Logger.error("loading " + track.getInfo().getTitle() + " from source " + track.getInfo().getUri() +
						" failed. might be spotify issue or youtube dying again.");
			}
			nextTrack();
		} else if (endReason.equals(Message.EmittedEvent.TrackEndEvent.AudioTrackEndReason.REPLACED)) {
			return;
		} else {
			queue.clear();
			i = 1;
			server.getAppInstances().values().forEach(instance -> instance.getAppQueue().clearQueue());
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
		server.getAppInstances().values().forEach(instance -> instance.getAppQueue().repeat());
		return repeating;
	}

	/**
	 * <p>removeFromQueue.</p>
	 *
	 * @param id a {@link org.json.JSONArray} object
	 */
	public void removeFromQueue (JSONArray id) {
		Track[] temp2 = new Track[queue2.size()];
		Track[] temp = new Track[queue.size()];
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
		for (Track t : temp2) {
			if (t != null) queue2.offer(t);
		}
		for (Track t : temp) {
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
        List<Track> temp = new ArrayList<>(queue);
        List<Track> temp2 = new ArrayList<>(queue2);
		queue.clear();
		queue2.clear();
		Track track;
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
		server.getAppInstances().values().forEach(instance -> {
			instance.getAppQueue().insertQueue(queue.get(i), "0");
			instance.getAppQueue().insertQueue(queue.get(i + 1), "1");
		});
		nextTrack();
	}
}
