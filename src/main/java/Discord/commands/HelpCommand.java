package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /help
 * sends all commands
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class HelpCommand extends BasicCommand {

    /**
     * <p>Constructor for HelpCommand.</p>
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     * just sends the command list as response
     */
    public HelpCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("""
							```Command List:
							/play [name or link] - Play a song from youtube with link or search by name
							/join - Join your current voice Channel (must be in voice Channel for this)
							/leave - leave the current voice Channel
							/pause - pause the current music
							/resume - resume the current music
							/stop - stops the music and empties the queue
							/info - shows current song and time in song
							/queue - show queue
							/repeat - repeats the queue
							/skip - skips the current song
							/previous - plays the previous song
							/volume - sets the volume
							/shuffle - shuffles the queue
							/lyrics - prints the lyrics of current song
							/appconnect - to connect to the app
							for the app go to https://github.com/xthesebx/musicbot-app/releases/latest 
							download the zip, extract it and execute the .exe
							/streamermode - for connection with the twitch chat for requests```""").queue();
    }
}

