package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class BasicCommand {
    String cid;
    TextChannel channel;
    SlashCommandInteractionEvent event;
    Server server;

    public BasicCommand(SlashCommandInteractionEvent event, Server server) {
        this.event = event;
        this.server = server;
        cid = event.getChannel().getId();
        channel = event.getChannel().asTextChannel();
    }
}
