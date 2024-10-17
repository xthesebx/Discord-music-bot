package Discord.commands;

import Discord.App.AppQueue;
import Discord.Server;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>ShuffleCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class ShuffleCommand extends BasicCommand {

    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public ShuffleCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        shuffle(server);
        event.reply("Shuffled the queue").queue();
    }

    public static void shuffle(Server server) {
        List<AudioTrack> tracks = new ArrayList<>();
        int size = server.getTrackScheduler().queue.size();
        for (int i = 0; i < size; i ++) {
            tracks.add(server.getTrackScheduler().queue.get(0));
            server.getTrackScheduler().queue.remove(0);
        }
        Collections.shuffle(tracks);
        for (AudioTrack track : tracks) {
            server.getTrackScheduler().queue.add(track);
        }
        server.getAppInstances().forEach(instance -> {
            instance.getAppQueue().clearQueue();
            instance.getAppQueue().initQueue();
        });
    }
}
