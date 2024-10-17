package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * <p>RepeatCommand class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class RepeatCommand extends BasicCommand {
    /**
     * <p>Constructor for BasicCommand.</p>
     *
     * @param event  received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public RepeatCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        switch (server.getTrackScheduler().toggleRepeat()) {
            case NO_REPEAT -> event.reply("Not Repeating anymore").queue();
            case REPEAT_QUEUE -> event.reply("Repeating the whole Queue").queue();
            case REPEAT_SINGLE -> event.reply("Repeating the single Song").queue();
        }
    }
}
