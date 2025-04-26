package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Abstract class for same code in every Command
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public abstract class BasicCommand {
    protected String cid;
    protected GuildMessageChannel channel;
    protected SlashCommandInteractionEvent event;
    protected Server server;

    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public BasicCommand(SlashCommandInteractionEvent event, Server server) {
        this.event = event;
        this.server = server;
        cid = event.getChannel().getId();
        channel = event.getChannel().asGuildMessageChannel();
    }
}
