package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 * command /resume
 * resumes player after pause
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class ResumeCommand extends BasicCommand {

    /**
     * in case of /command
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public ResumeCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.reply("Resuming the Music").queue();
        server.getPlayer().setPaused(false);
    }
}
