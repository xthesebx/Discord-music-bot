package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpCommand extends BasicCommand {
    public HelpCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("""
							```Command List:
							%play [name or link] - Play a song from youtube with link or search by name
							%bind - bind the discord bot to this text channel
							%unbind - unbind the discord bot from text channels
							%join - Join your current voice Channel (must be in voice Channel for this)
							%leave - leave the current voice Channel
							%pause - pause the current music
							%resume - resume the current music
							%stop - stops the music and empties the queue
							%info - shows current song and time in song
							%infospam - same as info but refrehsing every second
							%queue - show queue```""").queue();
    }
}

