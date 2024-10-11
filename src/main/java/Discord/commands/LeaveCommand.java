package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * /leave command leaves voicechat and clears queue
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class LeaveCommand extends BasicCommand {

    AudioManager audioManager;
    /**
     * <p>Constructor for LeaveCommand.</p>
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public LeaveCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        if (!server.leave()) event.reply("Currently not connected to a Voice-Channel").queue();
        else event.reply("Disconnected from the voice channel!").queue();

    }
}
