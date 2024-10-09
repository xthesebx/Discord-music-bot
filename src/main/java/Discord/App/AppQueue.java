package Discord.App;

import Discord.Server;
import Discord.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.JSONArray;
import org.json.JSONObject;

public class AppQueue {

    public static Debouncer debouncer = new Debouncer();
    AppInstance instance;
    TrackScheduler trackScheduler;

    public AppQueue(Server server, AppInstance instance) {
        this.trackScheduler = server.getTrackScheduler();
        this.instance = instance;
    }

    public void updateQueue() {
        int size = trackScheduler.queue.size() + trackScheduler.queue2.size();
        String[] titles = new String[size];
        String[] authors = new String[size];
        String[] length = new String[size];
        int i = 0;
        for (AudioTrack e : trackScheduler.queue2) {
            titles[i] = e.getInfo().title;
            authors[i] = e.getInfo().author;
            long duration = e.getDuration() / 1000;
            long minutes = (long) Math.floor((double) duration / 60);
            long seconds = (long) Math.floor(duration % 60);
            length[i] = minutes + ":" + seconds;
            i++;
        }
        for (AudioTrack e : trackScheduler.queue) {
            titles[i] = e.getInfo().title;
            authors[i] = e.getInfo().author;
            long duration = e.getDuration() / 1000;
            long minutes = (long) Math.floor((double) duration / 60);
            long seconds = (long) Math.floor(duration % 60);
            length[i] = minutes + ":" + seconds;
            i++;
        }
        JSONObject object = new JSONObject();
        JSONArray JSON = new JSONArray();
        for (int j = 0; j < size; j++) {
            JSON.put(song(titles[j], authors[j], length[j]));
        }
        object.put("queue", JSON);
        instance.out.println(object);
    }

    private JSONObject song(String title, String author, String duration) {
        JSONObject object = new JSONObject();
        object.put("title", title);
        object.put("author", author);
        object.put("duration", duration);
        return object;
    }
}
