package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /pause
 */
public class PauseCommand extends BasicCommand {
    /**
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public PauseCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("Pausing the Music").queue();
        server.getPlayer().setPaused(true);
    }
}
