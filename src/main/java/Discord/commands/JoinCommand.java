package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * Command /join
 */
public class JoinCommand extends BasicCommand {
    /**
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public JoinCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        switch (server.join(event)) {
            case ALREADYCONNECTED -> {
                event.reply("Already connected to that Voice-Channel").queue();
                return;
            }
            case NOTINVOICE -> {
                event.reply("You must be in a Voice-Channel for me to join your Channel!").queue();
                return;
            }
            case NOPERMS -> {
                event.reply("I do not have permissions to join a voice channel!").queue();
                return;
            }
            case JOINED -> event.reply("Connected to the voice channel!").queue();
        }
        if (server.getPlayer().getPlayingTrack() != null) return;
        server.getDc().startTimer();
    }
}
