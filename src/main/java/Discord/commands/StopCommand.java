package Discord.commands;

import Discord.App.AppInstance;
import Discord.Server;
import Discord.playerHandlers.RepeatState;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /stop
 * stops the player and clears queue
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class StopCommand extends BasicCommand {

    AudioPlayer player;

    /**
     * in case of /stop
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public StopCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.player = server.getPlayer();
        server.getTrackScheduler().repeating = RepeatState.NO_REPEAT;
        if (player.getPlayingTrack() == null) {
            event.reply("Player already stopped!").queue();
            return;
        }
        player.stopTrack();
        server.getDc().startTimer();
        if (server.getPlayer().isPaused()) server.getPlayer().setPaused(false);
        server.getAppInstances().forEach(AppInstance::setIdlePresence);
        event.reply("Stopped!").queue();
    }
}
