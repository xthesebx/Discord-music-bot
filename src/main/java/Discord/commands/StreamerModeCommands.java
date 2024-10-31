package Discord.commands;

import Discord.Server;
import Discord.twitchIntegration.StreamerFeedback;
import com.hawolt.logger.Logger;
import com.seb.io.Reader;
import net.dv8tion.jda.api.entities.Member;
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
        switch (setStreamer(server, event.getMember(), event.getOption("twitchaccount").getAsString())) {
            case NO_STREAMER_ROLE -> event.reply("setup streamerrole first").queue();
            case NO_STREAMER -> event.reply("you are not a streamer").queue();
            case DEACTIVATED -> event.reply("deactivated StreamerMode").queue();
            case RUNNING_BY_SOMEONE -> event.reply("streamermode already in use by someone else").queue();
            case ERROR -> event.reply("cant activate StreamerMode").queue();
            case ACTIVATED -> event.reply("activated StreamerMode").queue();
        }
    }

    public static StreamerFeedback setStreamer(Server server, Member member, String twitchacc) {
        String roleid = Reader.read(new File("roles/" + server.getGuild().getId()));
        if (roleid == null) {
            return StreamerFeedback.NO_STREAMER_ROLE;
        }
        if (!member.getRoles().contains(server.getGuild().getRoleById(roleid))) {
            return StreamerFeedback.NO_STREAMER;
        }
        if (server.getStreamer() != null) {
            if (server.getStreamer().equals(member)) {
                server.setStreamer(null);
                server.getChatBotListener().disconnect();
                return StreamerFeedback.DEACTIVATED;
            }
            return StreamerFeedback.RUNNING_BY_SOMEONE;
        }
        server.setStreamer(member);
        server.join(member.getVoiceState().getChannel());
        try {
            server.getChatBotListener().connect(twitchacc);
        } catch (IOException | NullPointerException e) {
            if (e instanceof NullPointerException) {
                server.setStreamer(null);
                return StreamerFeedback.ERROR;
            }
            if (e instanceof ConnectException) {
                server.setStreamer(null);
                return StreamerFeedback.ERROR;
            }
            Logger.error(e);
        }
        new Thread(server.getChatBotListener()).start();
        return StreamerFeedback.ACTIVATED;
    }
}
