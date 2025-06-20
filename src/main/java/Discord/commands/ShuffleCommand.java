package Discord.commands;

import Discord.Server;
import Discord.playerHandlers.PlayMethods;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /**
     * <p>shuffle.</p>
     *
     * @param server a {@link Discord.Server} object
     */
    public static void shuffle(Server server) {
        while (PlayMethods.servers.contains(server)) {
          try {
            Thread.sleep(10);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
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
        server.getTrackScheduler().i = 0;
        server.getAppInstances().forEach(instance -> {
            instance.getAppQueue().clearQueue();
            instance.getAppQueue().initQueue(false);
        });
    }
}
