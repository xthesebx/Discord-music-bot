package Discord.commands;

import Discord.Server;
import Discord.playerHandlers.TrackScheduler;
import dev.arbjerg.lavalink.client.player.Track;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.text.DecimalFormat;

/**
 * command /queue shows the current queue
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class QueueCommand extends BasicCommand {

    TrackScheduler trackScheduler;
    /**
     * in case of /command
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public QueueCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.trackScheduler = server.getTrackScheduler();
        if (trackScheduler.queue.isEmpty() && trackScheduler.queue2.isEmpty()) {
            event.reply("Queue is empty").queue();
            return;
        }
        int size = trackScheduler.queue.size() - trackScheduler.i + trackScheduler.queue2.size();
        String[] titles = new String[size];
        String[] authors = new String[size];
        String[] length = new String[size];
        int i = 0;
        for (Track e : trackScheduler.queue2) {
            titles[i] = e.getInfo().getTitle();
            authors[i] = e.getInfo().getAuthor();
            long duration = e.getInfo().getLength() / 1000;
            long minutes = (long) Math.floor((double) duration / 60);
            DecimalFormat format = new DecimalFormat("00");
            long seconds = (long) Math.floor(duration % 60);
            length[i] = minutes + ":" + format.format(seconds);
            i++;
        }
        int j = 0;
        for (Track e : trackScheduler.queue) {
            if (j < trackScheduler.i) {
                j++;
                continue;
            }
            titles[i] = e.getInfo().getTitle();
            authors[i] = e.getInfo().getAuthor();
            long duration = e.getInfo().getLength() / 1000;
            long minutes = (long) Math.floor((double) duration / 60);
            DecimalFormat format = new DecimalFormat("00");
            long seconds = (long) Math.floor(duration % 60);
            length[i] = minutes + ":" + format.format(seconds);
            i++;
        }
        StringBuilder result = new StringBuilder("```");
        result.append("Currently are " + size + " songs in queue\n");
        for (j = 0; j < titles.length; j++) {
            StringBuilder temp = new StringBuilder();
            temp.append(titles[j]).append(" by ").append(authors[j]).append(" ").append(length[j]).append("\n");
            if (result.length() + temp.length() > 1990) {
                break;
            } else {
                result.append(temp);
            }
        }
        result.append("```");
        String response = result.toString();
        event.reply(response).queue();
    }
}
