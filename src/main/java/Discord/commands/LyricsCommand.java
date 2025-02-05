package Discord.commands;

import Discord.Server;
import com.github.topi314.lavalyrics.lyrics.AudioLyrics;
import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.List;

/**
 * <p>LyricsCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class LyricsCommand extends BasicCommand {

    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public LyricsCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        StringBuilder lyrics = new StringBuilder("```");
        try {
            List<AudioLyrics.Line> lines = server.getLyricsManager().loadLyrics(server.getPlayer().getPlayingTrack()).getLines();
            assert lines != null;
            for (AudioLyrics.Line line : lines) {
                if (line.getLine().equals("♪") || line.getLine().isEmpty()) continue;
                if (lyrics.length() >= 1800) {
                    event.reply(lyrics + "```").queue();
                    lyrics = new StringBuilder("```");
                }
                lyrics.append(line.getLine()).append("\n");
            }
            channel.sendMessage(lyrics + "```").queue();
        } catch (NullPointerException e) {
            event.reply("```No Lyrics found```").queue();
        }
    }
}
