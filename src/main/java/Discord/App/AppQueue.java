package Discord.App;

import Discord.Server;
import Discord.playerHandlers.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * <p>AppQueue class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class AppQueue {

    /** Constant <code>debouncer</code> */
    public static Debouncer debouncer = new Debouncer();
    AppInstance instance;
    TrackScheduler trackScheduler;
    JSONObject object = new JSONObject(), insert = new JSONObject();
    JSONArray queue = new JSONArray();
    Server server;

    /**
     * <p>Constructor for AppQueue.</p>
     *
     * @param server a {@link Discord.Server} object
     * @param instance a {@link Discord.App.AppInstance} object
     */
    public AppQueue(Server server, AppInstance instance) {
        this.trackScheduler = server.getTrackScheduler();
        this.instance = instance;
        this.server = server;
    }



    /**
     * <p>initQueue.</p>
     */
    public void initQueue() {
        repeat();
        volume();
        int size = trackScheduler.queue.size() - trackScheduler.i + trackScheduler.queue2.size() + 1;
        String[] titles = new String[size];
        String[] authors = new String[size];
        String[] length = new String[size];
        String[] urls = new String[size];
        titles[0] = server.getPlayer().getPlayingTrack().getInfo().title;
        authors[0] = server.getPlayer().getPlayingTrack().getInfo().author;
        length[0] = getLength(server.getPlayer().getPlayingTrack());
        urls[0] = server.getPlayer().getPlayingTrack().getInfo().uri;
        int i = 1;
        for (AudioTrack e : trackScheduler.queue2) {
            titles[i] = e.getInfo().title;
            authors[i] = e.getInfo().author;
            length[i] = getLength(e);
            urls[i] = e.getInfo().uri;
            i++;
        }
        int j = 0;
        for (AudioTrack e : trackScheduler.queue) {
            if (j < trackScheduler.i) {
                j++;
                continue;
            };
            titles[i] = e.getInfo().title;
            authors[i] = e.getInfo().author;
            length[i] = getLength(e);
            urls[i] = e.getInfo().uri;
            i++;
        }
        for (j = 0; j < size; j++) {
            queue.put(song(titles[j], authors[j], length[j], urls[j]));
        }
        object.put("queue", queue);
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
        nextQueue();
    }

    /**
     * <p>addQueue.</p>
     *
     * @param track a {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} object
     */
    public void addQueue(AudioTrack track) {
        String length = getLength(track);
        queue.put(song(track.getInfo().title, track.getInfo().author, length, track.getInfo().uri));
        object.put("queue", queue);
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }

    /**
     * <p>insertQueue.</p>
     *
     * @param track a {@link com.sedmelluq.discord.lavaplayer.track.AudioTrack} object
     * @param pos a {@link java.lang.String} object
     */
    public void insertQueue(AudioTrack track, String pos) {
        String length = getLength(track);
        insert.put(pos ,song(track.getInfo().title, track.getInfo().author, length, track.getInfo().uri));
        object.put("insert", insert);
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }

    /**
     * <p>nextQueue.</p>
     */
    public void nextQueue() {
        int i = 1;
        try {
            i = object.getInt("next") + 1;
        } catch (JSONException e) {

        }
        object.put("next", i);
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }

    /**
     * <p>clearQueue.</p>
     */
    public void clearQueue() {
        object.put("clear", "clear");
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }

    private JSONObject song(String title, String author, String duration, String url) {
        JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("author", author);
        object.put("duration", duration);
        object.put("url", url);
        return object;
    }

    private void send() {
        instance.out.println(object);
        object.clear();
        queue.clear();
        insert.clear();
    }

    private String getLength(AudioTrack track) {
        long duration = track.getDuration() / 1000;
        long minutes = (long) Math.floor((double) duration / 60);
        DecimalFormat format = new DecimalFormat("00");
        long seconds = (long) Math.floor(duration % 60);
        return (minutes + ":" + format.format(seconds));
    }

    /**
     * <p>repeat.</p>
     */
    public void repeat() {
        object.put("repeat", server.getTrackScheduler().repeating);
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }

    /**
     * <p>volume.</p>
     */
    public void volume() {
        object.put("volume", server.getPlayer().getVolume());
        debouncer.debounce("send", () -> send(), 1, TimeUnit.SECONDS);
    }
}
