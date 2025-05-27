package Discord.commands;

import Discord.Server;
import com.hawolt.logger.Logger;
import com.seb.io.Writer;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.io.File;

/**
 * command /volume
 * sets the volume for the server
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class VolumeCommand extends BasicCommand {

    /**
     * in case of /volume
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     *
     * changes the volume of the player
     */
    public VolumeCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        OptionMapping optionMapping = event.getOption("volume");
        assert optionMapping != null;
        String volString = optionMapping.getAsString();
        try {
            int volumeInt = Integer.parseInt(volString);
            event.reply("Setting the volume to " + volString).queue();
            setVolume(server, volumeInt);
        } catch (NumberFormatException e) {
            event.reply("Failed to set volume to " + volString + ".\nPlease use a legal Integer Value! (max 2147483647)").queue();
        } catch (Exception e) {
            Logger.error(e);
            event.reply("Failed to set volume to " + volString + ".").queue();
            event.getJDA().retrieveUserById("277064996264083456").complete().openPrivateChannel().complete().sendMessage(e.toString()).queue();
        }
    }

    /**
     * <p>setVolume.</p>
     *
     * @param server a {@link Discord.Server} object
     * @param volume a int
     */
    public static void setVolume(Server server, int volume) {
        server.getPlayer().setVolume(volume);
        server.setVolume(volume);
        server.getAppInstances().forEach(appInstance -> appInstance.getAppQueue().volume());
        File f = new File("volumes/" + server.getGuildId());
        Logger.debug(f.getAbsolutePath());
        Writer.write(String.valueOf(volume), f);
        server.getAppInstances().forEach(instance -> instance.getAppQueue().volume());
    }
}
