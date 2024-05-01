package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopCommand extends BasicCommand {
    public StopCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        if (server.player.getPlayingTrack() == null) return;
        server.player.stopTrack();
        server.dcThread.start();
        event.reply("Stopped!").queue();
    }
}
