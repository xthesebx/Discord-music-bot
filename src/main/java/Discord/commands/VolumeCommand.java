package Discord.commands;

import Discord.NewMain;
import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.File;

/**
 * command /volume
 * sets the volume for the server
 */
public class VolumeCommand extends BasicCommand {

    /**
     * in case of /command
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public VolumeCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        OptionMapping optionMapping = event.getOption("volume");
        assert optionMapping != null;
        String volString = optionMapping.getAsString();
        int volumeInt = Integer.parseInt(volString);
        event.reply("Setting the volume to " + volString).queue();
        server.getPlayer().setVolume(volumeInt);
        server.setVolume(volumeInt);
        NewMain.write(volString, new File("volumes/" + server.getGuildId()));
    }
}
