package Discord.commands;

import Discord.Server;
import com.seb.io.Reader;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.File;

/**
 * <p>ToggleRequestCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class ToggleRequestCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public ToggleRequestCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        String roleid = Reader.read(new File("roles/" + event.getGuild().getId()));
        if (roleid.isEmpty()) {
            event.reply("setup streamerrole first").queue();
            return;
        }
        if (!event.getMember().getRoles().contains(server.getGuild().getRoleById(roleid))) {
            event.reply("you are not a streamer").queue();
            return;
        }
        if (server.getStreamer() != null) {
            if (server.getStreamer().equals(event.getMember())) {
                server.getChatBotListener().requests = !server.getChatBotListener().requests;
                event.reply("toggled requests").queue();
                return;
            }
            event.reply("streamermode already in use by someone else").queue();
        }
    }
}
