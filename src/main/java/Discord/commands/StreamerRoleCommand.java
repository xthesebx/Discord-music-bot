package Discord.commands;

import Discord.NewMain;
import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.File;

public class StreamerRoleCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public StreamerRoleCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        NewMain.write(event.getOption("streamerrole").getAsRole().getId(), new File("roles/" + event.getGuild().getId()));
        event.reply("set streamer role").queue();
    }
}
