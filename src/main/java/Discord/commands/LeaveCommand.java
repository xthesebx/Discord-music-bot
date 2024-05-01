package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class LeaveCommand extends BasicCommand {

    public LeaveCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        // Checks if the bot is connected to a voice channel.
        if (!server.audioManager.isConnected()) {
            event.reply("Currently not connected to a Voice-Channel").queue();
            return;
        }
        // Disconnect from the channel.
       server.audioManager.closeAudioConnection();
        // Notify the user.
        event.reply("Disconnected from the voice channel!").queue();
        server.player.stopTrack();
    }
}
