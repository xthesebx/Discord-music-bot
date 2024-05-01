package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ResumeCommand extends BasicCommand {
    public ResumeCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("Resuming the Music").queue();
        server.player.setPaused(false);
    }
}
