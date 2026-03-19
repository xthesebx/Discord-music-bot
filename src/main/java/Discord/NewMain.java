package Discord;

import Discord.App.AppListener;
import com.hawolt.logger.Logger;
import com.seb.io.Reader;
import com.seb.io.Writer;
import dev.arbjerg.lavalink.client.*;
import dev.arbjerg.lavalink.client.event.*;
import dev.arbjerg.lavalink.client.loadbalancing.builtin.VoiceRegionPenaltyProvider;
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
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.*;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * new Main Class after old one got deprecated
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class NewMain extends ListenerAdapter implements VoiceDispatchInterceptor {
    /**
     * map for the discord servers
     */
    public final HashMap<Long, Server> map = new HashMap<>();
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
    public static LavalinkClient client;
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

    JDA jda;
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    public long statTimestamp;

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
        createLavalink();

        scheduler.scheduleAtFixedRate(() -> {
            if (!client.getNodes().get(0).getAvailable()) reconnectLavalink();
        }, 1,1, TimeUnit.SECONDS);
        jda = JDABuilder.createDefault(apikey.strip()).enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MEMBERS).setStatus(OnlineStatus.OFFLINE).setMemberCachePolicy(MemberCachePolicy.ALL).enableCache(CacheFlag.ACTIVITY, CacheFlag.ONLINE_STATUS).setVoiceDispatchInterceptor(this).build();
        jda.addEventListener(this);
        jda.awaitReady();
        for (Guild guild : jda.getGuilds()) {
            Logger.debug(guild.getName());
            map.put(guild.getIdLong(), new Server(guild, client));
        }
        jda.getPresence().setPresence(OnlineStatus.ONLINE, Activity.playing("some banger music!"));
        Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook(this)));


        //map.get(event.getGuildId()).getAppInstances().values().forEach(instance -> );
    }


    public void reconnectLavalink() {
        Logger.error("reconnect");
        map.values().forEach(server -> server.leave());
        client.close();
        createLavalink();

    }

    private void createLavalink() {
        client = new LavalinkClient(Helpers.getUserIdFromToken(apikey.strip()));

        client.getLoadBalancer().addPenaltyProvider(new VoiceRegionPenaltyProvider());
        JSONObject nodes = Reader.readJSON(new File("node.json"));
        nodes.getJSONArray("nodes").forEach(node -> client.addNode(new NodeOptions.Builder()
                .setName(((JSONObject) node).getString("name"))
                .setServerUri(((JSONObject) node).getString("url"))
                .setPassword(((JSONObject) node).getString("password")).build()).on(TrackStartEvent.class).subscribe((event -> {
            final LavalinkNode node1 = event.getNode();
            Logger.info("{}: track started: {}",
                    node1.getName(),
                    event.getTrack().getInfo());
        })));
        client.on(ClientEvent.class).subscribe(event -> {
            if (event instanceof StatsEvent) {
                statTimestamp = System.currentTimeMillis();
            }
        });

        client.on(PlayerUpdateEvent.class).subscribe(Logger::debug);
        client.on(TrackEndEvent.class).subscribe(event -> Optional.ofNullable(map.get(event.getGuildId())).ifPresent(
                guild -> guild.getTrackScheduler().onTrackEnd(event.getTrack(), event.getEndReason())
        ));

        map.values().forEach(server -> server.setLavalink(client));

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
        map.get(event.getGuild().getIdLong()).onSlashCommandInteraction(event);
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
        map.get(event.getGuild().getIdLong()).onButtonInteraction(event);
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
            map.put(event.getGuild().getIdLong(), new Server(event.getGuild(), client));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onVoiceServerUpdate(@NotNull VoiceDispatchInterceptor.VoiceServerUpdate update) {
        map.get(update.getGuild().getIdLong()).onVoiceServerUpdate(update);
    }

    @Override
    public boolean onVoiceStateUpdate(@NotNull VoiceDispatchInterceptor.VoiceStateUpdate update) {
        return map.get(update.getGuild().getIdLong()).onVoiceStateUpdate(update);
    }
/*
    @Override
    public void onUserUpdateActivities(@NotNull UserUpdateActivitiesEvent event) {
        Logger.error(event);
    }

    @Override
    public void onUserActivityStart(@NotNull UserActivityStartEvent event) {
        Logger.error(event);
    }

    @Override
    public void onUserActivityEnd(@NotNull UserActivityEndEvent event) {
        Logger.error(event);
    }

    @Override
    public void onUserUpdateActivityOrder(@NotNull UserUpdateActivityOrderEvent event) {
        Logger.error(event);
    }

    @Override
    public void onGenericUser(@NotNull GenericUserEvent event) {
        Logger.error(event);
    }

    @Override
    public void onUserUpdateOnlineStatus(@NotNull UserUpdateOnlineStatusEvent event) {
        Logger.error(event);
    }

    @Override
    public void onGenericUserPresence(@NotNull GenericUserPresenceEvent event) {
        Logger.error(event);
    }*/
}
