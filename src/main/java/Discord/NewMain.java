package Discord;

import Discord.App.AppListener;
import com.hawolt.logger.Logger;
import com.seb.io.Reader;
import com.seb.io.Writer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.VoiceDispatchInterceptor;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

/**
 * new Main Class after old one got deprecated
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class NewMain extends ListenerAdapter implements VoiceDispatchInterceptor {
    private final JDA jda;
    /**
     * map for the discord servers
     */
    public final HashMap<String, Server> map = new HashMap<>();
    /** Constant <code>clientid</code>
     * Constant <code>clientsecret</code>
     * Constant <code>spdc</code>
     * Constant <code>apikey</code> */
    /** Constant <code>clientsecret=""</code> */
    /** Constant <code>spdc=""</code> */
    /** Constant <code>apikey=""</code> */
    /** Constant <code>clientsecret=""</code> */
    /** Constant <code>spdc=""</code> */
    /** Constant <code>apikey=""</code> */
    public static String clientid, clientsecret, spdc, apikey;
    /** Constant <code>APP_LISTENER</code> */
    public static final AppListener APP_LISTENER;

    static {
        try {
            APP_LISTENER = new AppListener();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Main function
     *
     * @param args args
     * @throws java.lang.InterruptedException because of main throwing it
     * @throws java.io.IOException if any.
     */
    public static void main (String[] args) throws InterruptedException, IOException {
        String filepath = "slf4j/" + Date.from(Instant.now()).toString().replace(" ", "_").replace(":", "_") + ".txt";
        File log = new File("log.txt");
        if (log.exists() && !Reader.read(log).isEmpty()) {
            Writer.write(Reader.read(log), new File(filepath));
        }
        new NewMain();
    }

    /**
     * basically just setting up jda
     *
     * @throws java.lang.InterruptedException because of awaitReady
     * @throws java.io.IOException if any.
     */
    public NewMain() throws InterruptedException, IOException {
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"));
        String original = Reader.read(new File("spotify.env"));
        clientid = original.substring(0, original.indexOf("\n"));
        clientsecret = original.substring(original.indexOf("\n") + 1).substring(0, original.indexOf("\n"));
        spdc = original.substring(original.indexOf("\n") + 1).substring(original.indexOf("\n") + 1);
        apikey = Reader.read(new File("apikey.env"));
        jda = JDABuilder.createDefault(apikey.strip()).enableIntents(GatewayIntent.GUILD_MESSAGES).enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING).setStatus(OnlineStatus.OFFLINE).setVoiceDispatchInterceptor(this).build();
        jda.addEventListener(this);
        jda.awaitReady();
        for (Guild guild : jda.getGuilds()) {
            Logger.debug(guild.getName());
            map.put(guild.getId(), new Server(guild));
        }
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("some banger music!"));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(this)));
    }


    /**
     * {@inheritDoc}
     *
     * giving the command to corresponding server object
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        super.onSlashCommandInteraction(event);
        assert event.getGuild() != null;
        map.get(event.getGuild().getId()).onSlashCommandInteraction(event);
    }

    /**
     * {@inheritDoc}
     *
     * giving the buttoninteraction to the corresponding server object
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        super.onButtonInteraction(event);
        assert event.getGuild() != null;
        map.get(event.getGuild().getId()).onButtonInteraction(event);
    }

    /**
     * {@inheritDoc}
     *
     * adding new server object to map to add new guilds
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        super.onGuildJoin(event);
        try {
            map.put(event.getGuild().getId(), new Server(event.getGuild()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate update) {

        map.get(update.getGuild().getId()).onVoiceServerUpdate(update);
    }

    @Override
    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate update) {
        return map.get(update.getGuild().getId()).onVoiceStateUpdate(update);
    }
}
