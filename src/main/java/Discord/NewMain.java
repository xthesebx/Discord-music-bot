package Discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;

/**
 * new Main Class after old one got deprecated
 */
public class NewMain extends ListenerAdapter {

    private final JDA jda;
    private final HashMap<String, Server> map = new HashMap<>();

    public static void main (String[] args) throws InterruptedException {
        new NewMain();
    }

    /**
     * basically just setting up jda
     * @throws InterruptedException because of awaitReady
     */
    public NewMain() throws InterruptedException {
        File env = new File("apikey.env");
        jda = JDABuilder.createDefault(read(env).strip()).enableIntents(GatewayIntent.MESSAGE_CONTENT).enableIntents(GatewayIntent.GUILD_MESSAGES).enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING).setStatus(OnlineStatus.OFFLINE).build();
        jda.addEventListener(this);
        jda.awaitReady();
        for (Guild guild : jda.getGuilds()) {
            map.put(guild.getId(), new Server(guild));
        }
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.customStatus("Playing some Banger Music"));
    }

    /**
     * reads text of a file
     * @param file the file to read from
     * @return String builder with text from the File
     */
    public static String read (File file) {
        if (!file.exists()) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
                writer.write("{}");
                writer.close();
            } catch (IOException ignored) {
            }
        }
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file.getAbsoluteFile()));
            String temp;
            while (true) {
                temp = reader.readLine();
                if (temp == null) break;
                text.append(temp);
            }
            reader.close();
        } catch (IOException ignored) {
        }
        return text.toString();
    }

    /**
     * write text to a file
     * @param text text to write
     * @param file file to write the text to
     */
    public static void write(String text, File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
            writer.write(text);
            writer.close();
        } catch (IOException ignored) {

        }
    }

    /**
     * giving the command to corresponding server object
     * @param event the event coming in from the command
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        assert event.getGuild() != null;
        map.get(event.getGuild().getId()).onSlashCommandInteraction(event);

    }

    /**
     * giving the buttoninteraction to the corresponding server object
     * @param event the event coming in from the button click
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        assert event.getGuild() != null;
        map.get(event.getGuild().getId()).onButtonInteraction(event);
    }

    /**
     * adding new server object to map to add new guilds
     * @param event event of guild joining to add guild to map
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        super.onGuildJoin(event);
        map.put(event.getGuild().getId(), new Server(event.getGuild()));
    }
}
