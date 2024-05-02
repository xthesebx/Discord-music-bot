package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * /leave command leaves voicechat and clears queue
 *
 * @author sebas
 * @version $Id: $Id
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
        this.audioManager = server.getAudioManager();
        // Checks if the bot is connected to a voice channel.
        if (!audioManager.isConnected()) {
            event.reply("Currently not connected to a Voice-Channel").queue();
            return;
        }
        // Disconnect from the channel.
       audioManager.closeAudioConnection();
        // Notify the user.
        event.reply("Disconnected from the voice channel!").queue();
        server.getPlayer().stopTrack();
        server.getDc().stopTimer();
    }
}
