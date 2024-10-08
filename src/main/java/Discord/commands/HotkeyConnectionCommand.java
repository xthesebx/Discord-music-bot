package Discord.commands;

import Discord.Server;
import Discord.StreamerHotkeyListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.UUID;

public class HotkeyConnectionCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public HotkeyConnectionCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        UUID uuid = UUID.randomUUID();
        StreamerHotkeyListener.auth.put(uuid, server);
        event.reply("send in private chat").queue();
        event.getUser().openPrivateChannel().complete().sendMessage(uuid.toString()).queue();
    }
}
