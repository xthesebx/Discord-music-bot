package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Abstract class for same code in every Command
 */
public abstract class BasicCommand {
    String cid;
    TextChannel channel;
    SlashCommandInteractionEvent event;
    Server server;
    ButtonInteractionEvent buttonEvent;

    /**
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public BasicCommand(SlashCommandInteractionEvent event, Server server) {
        this.event = event;
        this.server = server;
        cid = event.getChannel().getId();
        channel = event.getChannel().asTextChannel();
    }
    public BasicCommand(ButtonInteractionEvent event, Server server) {
        this.buttonEvent = event;
        this.server = server;
        cid = event.getChannel().getId();
        channel = event.getChannel().asTextChannel();
    }
}
