package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class PauseCommand extends BasicCommand {

    public PauseCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("Pausing the Music").queue();
        server.player.setPaused(true);
    }
}
