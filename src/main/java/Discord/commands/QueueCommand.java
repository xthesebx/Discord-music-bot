package Discord.commands;

import Discord.Server;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class QueueCommand extends BasicCommand {
    public QueueCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        if (server.trackScheduler.queue.isEmpty()) {
            event.reply("Queue is empty").queue();
            return;
        }
        String[] titles = new String[server.trackScheduler.queue.size()];
        String[] authors = new String[server.trackScheduler.queue.size()];
        String[] length = new String[server.trackScheduler.queue.size()];
        int i = 0;
        for (AudioTrack e : server.trackScheduler.queue) {
            titles[i] = e.getInfo().title;
            authors[i] = e.getInfo().author;
            long duration = e.getDuration() / 1000;
            long minutes = (long) Math.floor((double) duration / 60);
            long seconds = (long) Math.floor(duration % 60);
            length[i] = minutes + ":" + seconds;
            i++;
        }
        StringBuilder result = new StringBuilder("```");
        for (int j = 0; j < titles.length; j++) {
            result.append(titles[j]).append(" by ").append(authors[j]).append(" ").append(length[j]).append("\n");
        }
        result.append("```");
        event.reply(result.toString()).queue();
    }
}