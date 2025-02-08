package Discord.commands;

import Discord.Server;
import Discord.App.AppListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.UUID;

/**
 * <p>AppConnectionCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class AppConnectionCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public AppConnectionCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        UUID uuid = UUID.randomUUID();
        AppListener.auth.put(uuid, server);
        event.reply("send in private chat").queue();
        event.getUser().openPrivateChannel().complete().sendMessage(uuid.toString()).queue();
        server.members.put(uuid, event.getMember().getId());
    }
}
