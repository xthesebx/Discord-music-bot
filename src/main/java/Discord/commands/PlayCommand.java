package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.managers.AudioManager;

/**
 * command /play plays the song mentioned or searches on yt
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class PlayCommand extends BasicCommand {

    AudioManager audioManager;

    /**
     * in case of /command
     *
     * @param event received event to reply to it and handle the options
     * @param server the server it came from to make things easier
     */
    public PlayCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        this.audioManager = server.getAudioManager();
        OptionMapping optionMapping = event.getOption("name");
        assert optionMapping != null;
        String link = optionMapping.getAsString();
        if (event.getMember().getVoiceState().getChannel() == null) {
            event.reply("Connect to the voice Channel to add Music to the queue.").queue();
            return;
        }
        if (!audioManager.isConnected() || !audioManager.getConnectedChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
            switch (server.join(event)) {
                case NOTINVOICE -> {
                    event.reply("You must be in a Voice-Channel for me to join your Channel!").queue();
                    return;
                }
                case NOPERMS -> {
                    event.reply("I do not have permissions to join a voice channel!").queue();
                    return;
                }
            }
        }
        event.deferReply().queue();
        server.play(link, event, 0);
    }
}
