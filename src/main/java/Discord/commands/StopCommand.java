package Discord.commands;

import Discord.App.AppInstance;
import Discord.NewMain;
import Discord.Server;
import Discord.playerHandlers.RepeatState;
import dev.arbjerg.lavalink.client.player.LavalinkPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /stop
 * stops the player and clears queue
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class StopCommand extends BasicCommand {

    LavalinkPlayer player;

    /**
     * in case of /stop
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public StopCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.player = NewMain.client.getOrCreateLink(server.getGuildId()).getCachedPlayer();
        server.getTrackScheduler().repeating = RepeatState.NO_REPEAT;
        if (player.getTrack() == null) {
            event.reply("Player already stopped!").queue();
            return;
        }
        player.stopTrack().subscribe();
        server.getDc().startTimer();
        server.getAppInstances().values().forEach(AppInstance::setIdlePresence);
        if (player.getPaused()) player.setPaused(false);
        server.getAppInstances().values().forEach(AppInstance::setIdlePresence);
        server.getTrackScheduler().clear();
        event.reply("Stopped!").queue();
    }
}
