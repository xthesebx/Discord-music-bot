package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * <p>RepeatCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class RepeatCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public RepeatCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        server.getTrackScheduler().toggleRepeat();
        if (!server.getTrackScheduler().repeating)
            event.reply("not repeating anymore").queue();
        else
            event.reply("repeating now").queue();
    }
}
