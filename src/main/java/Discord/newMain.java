package Discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
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

public class newMain extends ListenerAdapter {

    JDA jda;
    HashMap<String, Server> map = new HashMap<>();

    public static void main (String[] args) throws InterruptedException {
        new newMain();
    }

    public newMain() throws InterruptedException {
        File env = new File("apikey.env");
        jda = JDABuilder.createDefault(read(env).toString().strip()).enableIntents(GatewayIntent.MESSAGE_CONTENT).enableIntents(GatewayIntent.GUILD_MESSAGES).enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING).build();
        jda.addEventListener(this);
        jda.awaitReady();
        jda.getPresence().setActivity(Activity.customStatus("Playing some Banger Music"));
        for (Guild guild : jda.getGuilds()) {
            map.put(guild.getId(), new Server(guild));
        }
    }

    public static StringBuilder read (File file) {
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
        return text;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        map.get(event.getGuild().getId()).onSlashCommandInteraction(event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        map.get(event.getGuild().getId()).onButtonInteraction(event);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        super.onGuildJoin(event);
        map.put(event.getGuild().getId(), new Server(event.getGuild()));
    }
}
