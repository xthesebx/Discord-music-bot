package Discord.commands;

import Discord.Server;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /stop
 * stops the player and clears queue
 */
public class StopCommand extends BasicCommand {

    AudioPlayer player;

    /**
     * in case of /command
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public StopCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.player = server.getPlayer();
        if (player.getPlayingTrack() == null) return;
        player.stopTrack();
        server.getDc().startTimer();
        event.reply("Stopped!").queue();
    }
}
