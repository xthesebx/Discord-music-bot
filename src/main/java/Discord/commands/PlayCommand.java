package Discord.commands;

import Discord.Server;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlayCommand extends BasicCommand {

    public PlayCommand(SlashCommandInteractionEvent event, Server server) {
        super(event, server);
        event.deferReply().queue();
        OptionMapping optionMapping = event.getOption("name");
        assert optionMapping != null;
        String link = optionMapping.getAsString();
        if (!server.audioManager.isConnected() || !server.audioManager.getConnectedChannel().getId().equals(event.getMember().getVoiceState().getChannel().getId())) {
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
        server.play(link, event);
    }
}
