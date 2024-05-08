package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SkipCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public SkipCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        server.getTrackScheduler().nextTrack();
        event.reply("Skipped the Track").queue();
    }
}
