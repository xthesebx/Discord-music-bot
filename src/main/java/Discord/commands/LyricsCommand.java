package Discord.commands;

import Discord.Server;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

public class LyricsCommand extends BasicCommand {

    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public LyricsCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        String lyrics = "```";
        try {
            List<AudioLyrics.Line> lines = server.getLyricsManager().loadLyrics(server.getTrackScheduler().track).getLines();
            for (AudioLyrics.Line line : lines) {
                if (line.getLine().equals("♪") || line.getLine().isEmpty()) continue;
                if (lyrics.length() >= 1800) {
                    event.getChannel().asTextChannel().sendMessage(lyrics + "```").queue();
                    lyrics = "```";
                }
                Logger.debug(line.getLine());
                lyrics += line.getLine() + "\n";
            }
            event.reply(lyrics + "```").queue();
        } catch (NullPointerException e) {
            event.reply("```No Lyrics found```").queue();
        }
    }
}
