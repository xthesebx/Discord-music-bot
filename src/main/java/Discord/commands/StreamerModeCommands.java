package Discord.commands;

import Discord.NewMain;
import Discord.Server;
import com.hawolt.logger.Logger;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

/**
 * Guild specific command, can be used in combination with my Twitch request bot to create a bot that plays song requests
 */

public class StreamerModeCommands extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public StreamerModeCommands(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        String roleid = NewMain.read(new File("roles/" + event.getGuild().getId()));
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
                server.setStreamer(null);
                event.reply("deactivated StreamerMode").queue();
                server.getChatBotListener().disconnect();
                return;
            }
            event.reply("streamermode already in use by someone else").queue();
        }
        server.setStreamer(event.getMember());
        server.join(event);
        try {
            server.getChatBotListener().connect(event.getOption("twitchaccount").getAsString());
        } catch (IOException | NullPointerException e) {
            if (e instanceof NullPointerException) {
                event.reply("cant activate StreamerMode").queue();
                return;
            }
            if (e instanceof ConnectException) {
                server.setStreamer(null);
                return;
            }
            Logger.error(e);
        }
        event.reply("activated StreamerMode").queue();
        new Thread(server.getChatBotListener()).start();
    }
}
