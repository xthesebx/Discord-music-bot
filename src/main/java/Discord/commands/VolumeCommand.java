package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.File;

public class VolumeCommand extends BasicCommand {
    public VolumeCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        OptionMapping optionMapping = event.getOption("volume");
        assert optionMapping != null;
        String volString = optionMapping.getAsString();
        int volumeInt = Integer.parseInt(volString);
        event.reply("Setting the volume to " + volString).queue();
        server.player.setVolume(volumeInt);
        server.volume = volumeInt;
        server.write(volString, new File("volumes/" + server.guildId));
    }
}
