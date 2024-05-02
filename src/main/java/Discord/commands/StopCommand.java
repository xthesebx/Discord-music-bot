package Discord.commands;

import Discord.Server;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class StopCommand extends BasicCommand {

    AudioPlayer player;

    public StopCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.player = server.getPlayer();
        if (player.getPlayingTrack() == null) return;
        player.stopTrack();
        server.getDc().startTimer();
        event.reply("Stopped!").queue();
    }
}
