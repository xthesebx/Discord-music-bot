package Discord;

import com.hawolt.logger.Logger;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


public class Main extends ListenerAdapter {
	JDA jda;
	JSONObject binds;
	JSONObject volumes;
	static HashMap<String, HashMap<String, Object>> map = new HashMap<>();
	public static void main (String[] args) throws InterruptedException {
		new Main();
	}
	
	public Main() throws InterruptedException {
		File env = new File("apikey.env");
		jda = JDABuilder.createDefault(read(env).toString().strip()).enableIntents(GatewayIntent.MESSAGE_CONTENT).enableIntents(GatewayIntent.GUILD_MESSAGES).enableIntents(GatewayIntent.GUILD_MESSAGE_TYPING).build();
		jda.addEventListener(this);
		jda.awaitReady();
		jda.getPresence().setActivity(Activity.customStatus("Playing some Banger Music"));
		binds = readBinds("bind");
		volumes = readBinds("volume");
	}
	
	@Override
	public void onButtonInteraction(ButtonInteractionEvent event) {
		AudioPlayerManager playerManager = (AudioPlayerManager) map.get(event.getGuild().getId()).get("playerManager");
		TrackScheduler trackScheduler = (TrackScheduler) map.get(event.getGuild().getId()).get("trackScheduler");
		TextChannel channel = event.getChannel().asTextChannel();
		String guildId = event.getGuild().getId();
		playerManager.loadItem(((String[]) map.get(event.getGuild().getId()).get("ytResults"))[Integer.parseInt(event.getButton().getId())], new AudioLoadResultHandler() {
			@Override
			public void trackLoaded (AudioTrack audioTrack) {
				trackScheduler.queue(audioTrack);
				event.editMessage(new MessageEditBuilder().setContent("```Added " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " to Queue```").setReplace(true).build()).queue();
				((Thread) map.get(guildId).get("dcThread")).interrupt();
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist audioPlaylist) {
				for (AudioTrack track : audioPlaylist.getTracks()) {
					trackScheduler.queue(track);
					event.editMessage(new MessageEditBuilder().setContent("```Added " + track.getInfo().title + " by " + track.getInfo().author + " to Queue```").setReplace(true).build()).queue();
				}
				((Thread) map.get(guildId).get("dcThread")).interrupt();
			}
			
			@Override
			public void noMatches() {
				String[][] ytResults = searchYT(event.getMessage().getContentRaw().substring(6));
				MessageCreateAction message = channel.sendMessage("Which one?");
				int i = 0;
				String[] urls = new String[5];
				for (String[] s : ytResults) {
					urls[i] = s[0];
					String title = s[1];
					if (title.length() > 80)
						message.addActionRow(Button.primary(String.valueOf(i), s[1].substring(0, 79)));
					else
						message.addActionRow(Button.primary(String.valueOf(i), s[1]));
					i++;
				}
				map.get(guildId).put("ytResults", urls);
				message.queue();
							/*
							channel.sendMessage("Nothing matching found").queue();*/
			}

			@Override
			public void loadFailed(FriendlyException e) {
				Logger.error(e);
				event.editMessage(e.getMessage()).setReplace(true).queue();
			}
		});
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
		if (!jda.getSelfUser().getName().equals(event.getAuthor().getName())) if (event.getAuthor().isBot()) return;
		if (event.isFromType(ChannelType.PRIVATE)) {
			if (jda.getSelfUser().getName().equals(event.getAuthor().getName()))
				System.out.println("Dispatched " + event.getMessage().getContentRaw() + " To " + event.getMessage().getChannel().asPrivateChannel().getName());
		} else {
			if (jda.getSelfUser().getName().equals(event.getAuthor().getName()))
				System.out.println("Dispatched " + event.getMessage().getContentRaw() + " On " + event.getGuild().getName());
			else {
				AudioPlayer player;
				AudioPlayerManager playerManager;
				TrackScheduler trackScheduler;
				AudioPlayerHandler audioPlayerHandler;
				InfoSpammer spammer;
				Thread spammerThread;
				String guildId = event.getGuild().getId();
				String bind = "";
				String volume = "";
				if (!map.containsKey(guildId)) {
					if (binds.has(guildId)) bind = binds.getString(guildId);
					HashMap<String, Object> serverMap = new HashMap<>();
					AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
					AudioSourceManagers.registerRemoteSources(audioPlayerManager);
					player = audioPlayerManager.createPlayer();
					audioPlayerHandler = new AudioPlayerHandler(player);
					trackScheduler = new TrackScheduler(player, this, guildId);
					player.addListener(trackScheduler);
					serverMap.put("playerManager", audioPlayerManager);
					serverMap.put("player", player);
					serverMap.put("audioPlayerHandler", audioPlayerHandler);
					serverMap.put("trackScheduler", trackScheduler);
					serverMap.put("spammer", new InfoSpammer());
					serverMap.put("spammerThread", new Thread((InfoSpammer) serverMap.get("spammer")));
					serverMap.put("bind", bind);
					serverMap.put("volume", volume);
					map.put(guildId, serverMap);
				}
				playerManager = (AudioPlayerManager) map.get(guildId).get("playerManager");
				spammer = (InfoSpammer) map.get(guildId).get("spammer");
				spammerThread = (Thread) map.get(guildId).get("spammerThread");
				player = (AudioPlayer) map.get(guildId).get("player");
				if (!volume.isEmpty()) player.setVolume(Integer.parseInt(volume));
				audioPlayerHandler = (AudioPlayerHandler) map.get(guildId).get("audioPlayerHandler");
				trackScheduler = (TrackScheduler) map.get(guildId).get("trackScheduler");
				bind = (String) map.get(guildId).get("bind");
				String message = event.getMessage().getContentRaw().toLowerCase();
				TextChannel channel = event.getChannel().asTextChannel();
				if (message.equals("%bind")) {
					bind = channel.getName();
					map.get(guildId).put("bind", bind);
					channel.sendMessage("bound Bot to this channel").queue();
					binds.put(guildId, channel.getName());
					writeBinds(binds.toString(), "bind");
				} else if (message.equals("%unbind")) {
					map.get(guildId).put("bind", "");
					channel.sendMessage("unbound Bot from certain channels").queue();
					binds.remove(guildId);
					writeBinds(binds.toString(), "bind");
				} else if (bind.isEmpty() || channel.getName().equals(bind)) {
					if (message.equals("%join")) {
						try {
							join(event, audioPlayerHandler);
							if (player.getPlayingTrack() != null) return;
							Thread dcThread = new Thread(new DisconnectTimer(this, event.getGuild().getId()));
							dcThread.start();
							map.get(guildId).put("dcThread", dcThread);
						} catch (ChannelNotFoundException e) {
							channel.sendMessage("You must be in a Voice-Channel for me to join your Channel!").queue();
						}
					} else if (message.equals("%leave")) {
						leave(event, player);
					} else if (message.equals("%pause")) {
						channel.sendMessage("Pausing the Music").queue();
						player.setPaused(true);
					} else if (message.equals("%resume")) {
						channel.sendMessage("Resuming the Music").queue();
						player.setPaused(false);
					} else if (message.contains("%volume")) {
						try {
							Integer.parseInt(message.substring(8));
							channel.sendMessage("Setting the volume to " + message.substring(8)).queue();
							player.setVolume(Integer.parseInt(message.substring(8)));
							volume = message.substring(8);
							map.get(guildId).put("volume", volume);
							volumes.put(guildId, message.substring(8));
							writeBinds(volumes.toString(), "volume");
						} catch (Exception e) {
							channel.sendMessage("Please type a correct Volume Number between -2147483648 and 2147483647 after the %volume command").queue();
						}
					} else if (message.equals("%info")) {
						if (player.getPlayingTrack() == null) {
							channel.sendMessage("Nothing is currently playing").queue();
							return;
						}
						long poshour = (long) Math.floor((double) player.getPlayingTrack().getPosition() / 1000 / 60 / 60);
						long posmin = (long) Math.floor(((double) player.getPlayingTrack().getPosition() / 1000 / 60) % 60);
						long possec = (long) Math.floor(((double) player.getPlayingTrack().getPosition() / 1000) % 60);
						long durhour = (long) Math.floor((double) player.getPlayingTrack().getDuration() / 1000 / 60 / 60);
						long durmin = (long) Math.floor(((double) player.getPlayingTrack().getDuration() / 1000 / 60) % 60);
						long dursec = (long) Math.floor(((double) player.getPlayingTrack().getDuration() / 1000) % 60);
						String poshours = String.valueOf(poshour);
						String posmins = String.valueOf(posmin);
						String possecs = String.valueOf(possec);
						String durhours = String.valueOf(durhour);
						String durmins = String.valueOf(durmin);
						String dursecs = String.valueOf(dursec);
						if (possecs.length() == 1) possecs = "0" + possecs;
						if (dursecs.length() == 1) dursecs = "0" + dursecs;
						if (durmins.length() > posmins.length()) posmins = "0" + posmins;
						if (durhours.length() > poshours.length()) poshours = "0" + poshours;
						String time;
						if (durhour > 0)
							time = poshours + ":" + posmins + ":" + possecs + "/" + durhours + ":" + durmins + ":" + dursecs + "```";
						else if (durmin > 0) time = posmins + ":" + possecs + "/" + durmins + ":" + dursecs + "```";
						else time = possecs + "/" + dursecs + "```";
						channel.sendMessage("```Currently playing: " + player.getPlayingTrack().getInfo().title +
								" by: " + player.getPlayingTrack().getInfo().author +
								" " + time).queue();
					} else if (message.equals("%queue")) {
						if (trackScheduler.queue.isEmpty()) {
							channel.sendMessage("Queue is empty").queue();
							return;
						}
						String[] titles = new String[trackScheduler.queue.size()];
						String[] authors = new String[trackScheduler.queue.size()];
						String[] length = new String[trackScheduler.queue.size()];
						int i = 0;
						for (AudioTrack e : trackScheduler.queue) {
							titles[i] = e.getInfo().title;
							authors[i] = e.getInfo().author;
							long duration = e.getDuration() / 1000;
							long minutes = (long) Math.floor((double) duration / 60);
							long seconds = (long) Math.floor(duration % 60);
							length[i] = minutes + ":" + seconds;
							i++;
						}
						StringBuilder result = new StringBuilder("```");
						for (int j = 0; j < titles.length; j++) {
							result.append(titles[j]).append(" by ").append(authors[j]).append(" ").append(length[j]).append("\n");
						}
						result.append("```");
						channel.sendMessage(result.toString()).queue();
					} else if (message.equals("%stop")) {
						if (player.getPlayingTrack() == null) return;
						player.stopTrack();
						Thread dcThread = new Thread(new DisconnectTimer(this, event.getGuild().getId()));
						dcThread.start();
						map.get(guildId).put("dcThread", dcThread);
					} else if (message.equals("%infospam")) {
						if (spammer.isRunning) {
							spammer.stop();
							spammerThread.interrupt();
						} else {
							spammerThread = new Thread(spammer);
							spammer.init(channel, player);
							spammerThread.start();
						}
					} else if (message.equals("%commands")) {
						PrivateChannel privateChannel = event.getAuthor().openPrivateChannel().complete();
						privateChannel.sendMessage("""
                                ```Command List:
                                %play [name or link] - Play a song from youtube with link or search by name
                                %bind - bind the discord bot to this text channel
                                %unbind - unbind the discord bot from text channels
                                %join - Join your current voice Channel (must be in voice Channel for this)
                                %leave - leave the current voice Channel
                                %pause - pause the current music
                                %resume - resume the current music
                                %stop - stops the music and empties the queue
                                %info - shows current song and time in song
                                %infospam - same as info but refrehsing every second
                                %queue - show queue```""").queue();
					} else if (message.contains("%play")) {
						try {
							event.getMessage().getContentRaw().substring(6);
						} catch (StringIndexOutOfBoundsException e) {
							channel.sendMessage("You must put a youtube link or song name behind the %play command").queue();
							return;
						}
						TrackScheduler finalTrackScheduler = trackScheduler;
						try {
							join(event, audioPlayerHandler);
						} catch (ChannelNotFoundException e) {
							channel.sendMessage("You must be in a Voice-Channel for me to join your Channel!").queue();
						}
						playerManager.loadItem(event.getMessage().getContentRaw().substring(6), new AudioLoadResultHandler() {
							@Override
							public void trackLoaded (AudioTrack audioTrack) {
								finalTrackScheduler.queue(audioTrack);
								channel.sendMessage("```Added " + audioTrack.getInfo().title + " by " + audioTrack.getInfo().author + " to Queue```").queue();
								((Thread) map.get(guildId).get("dcThread")).interrupt();
							}
							@Override
							public void playlistLoaded (AudioPlaylist audioPlaylist) {
								for (AudioTrack track : audioPlaylist.getTracks()) {
									finalTrackScheduler.queue(track);
									channel.sendMessage("```Added " + track.getInfo().title + " by " + track.getInfo().author + " to Queue```").queue();
								}
								((Thread) map.get(guildId).get("dcThread")).interrupt();
							}
							@Override
							public void noMatches() {
								String[][] ytResults = searchYT(event.getMessage().getContentRaw().substring(6));
								MessageCreateAction message = channel.sendMessage("Which one?");
								int i = 0;
								String[] urls = new String[5];
								for (String[] s : ytResults) {
									urls[i] = s[0];
									String title = s[1];
									if (title.length() > 80)
										message.addActionRow(Button.primary(String.valueOf(i), s[1].substring(0, 79)));
									else
										message.addActionRow(Button.primary(String.valueOf(i), s[1]));
									i++;
								}
								map.get(guildId).put("ytResults", urls);
								message.queue();
								/*
								channel.sendMessage("Nothing matching found").queue();*/
							}
							@Override
							public void loadFailed (FriendlyException e) {
								Logger.error(e);
								channel.sendMessage(e.getMessage()).queue();
							}
						});
					}
				}
			}
		}
	}
	
	private void join (MessageReceivedEvent event, AudioPlayerHandler audioPlayerHandler) throws ChannelNotFoundException {
		TextChannel channel = event.getChannel().asTextChannel();
		if (!event.getGuild().getSelfMember().hasPermission(channel, Permission.VOICE_CONNECT)) {
			// The bot does not have permission to join any voice channel. Don't forget the .queue()!
			
			channel.sendMessage("I do not have permissions to join a voice channel!").queue();
			return;
		}
		// Creates a variable equal to the channel that the user is in.
		if (event.getMember().getVoiceState().getChannel() == null) {
			throw new ChannelNotFoundException();
		}
		VoiceChannel connectedChannel = event.getMember().getVoiceState().getChannel().asVoiceChannel();
		// Checks if they are in a channel -- not being in a channel means that the variable = null.
		// Gets the audio manager.
		AudioManager audioManager = event.getGuild().getAudioManager();
		if (audioManager.isConnected() && audioManager.getConnectedChannel().asVoiceChannel().equals(connectedChannel))
			return;
		audioManager.setSendingHandler(audioPlayerHandler);
		// Connects to the channel.
		audioManager.openAudioConnection(connectedChannel);
		// Obviously people do not notice someone/something connecting.
		
		channel.sendMessage("Connected to the voice channel!").queue();
	}
	
	
	public void leave (MessageReceivedEvent event, AudioPlayer player) {
		TextChannel channel = event.getChannel().asTextChannel();
		// Checks if the bot is connected to a voice channel.
		if (!event.getGuild().getAudioManager().isConnected()) return;
		// Disconnect from the channel.
		event.getGuild().getAudioManager().closeAudioConnection();
		// Notify the user.
		
		channel.sendMessage("Disconnected from the voice channel!").queue();
		player.stopTrack();
	}
	
	
	private JSONObject readBinds (String fileName) {
		File file = new File(fileName + ".json");
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
		return (new JSONObject(text.toString()));
	}
	
	private void writeBinds (String binds, String fileName) {
		File file = new File(fileName + ".json");
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			writer.write(binds);
			writer.close();
		} catch (IOException ignored) {
		
		}
	}
	
	private String[][] searchYT (String s) {
		try {
			s = s.replaceAll(" ", "+");
			URL url = new URL("https://www.youtube.com/results?search_query=" + s);
			HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
			BufferedInputStream inputStream = new BufferedInputStream(httpURLConnection.getInputStream());
			byte[] dataBuffer = new byte[1024];
			StringBuilder r = new StringBuilder();
			while (inputStream.read(dataBuffer, 0, 1024) != -1) {
				r.append(new String(dataBuffer));
			}
			String[][] list = new String[5][3];
			r = new StringBuilder(r.toString().replaceAll("\\\\u0026", "&"));
			r = new StringBuilder(r.toString().replaceAll("&amp;", "&"));
			String temp = r.toString();
			for (int i = 0; i < 5; i++) {
				temp = temp.substring(temp.indexOf("/watch?"));
				if (i > 0) {
					while (list[i - 1][0].contains(temp.substring(0, temp.indexOf("&pp")))) {
						temp = temp.substring(temp.indexOf("\""));
						temp = temp.substring(temp.indexOf("/watch?"));
					}
				}
				list[i][0] = "https://www.youtube.com" + temp.substring(0, temp.indexOf("\""));
				URL tempurl = new URL(list[i][0]);
				HttpURLConnection con = (HttpURLConnection) tempurl.openConnection();
				inputStream = new BufferedInputStream(con.getInputStream());
				dataBuffer = new byte[1024];
				StringBuilder t = new StringBuilder();
				while (inputStream.read(dataBuffer, 0, 1024) != -1) {
					t.append(new String(dataBuffer));
				}
				t = new StringBuilder(t.toString().replaceAll("\\\\u0026", "&"));
				t = new StringBuilder(t.toString().replaceAll("&amp;", "&"));
				t = new StringBuilder(t.toString().replaceAll("&#39;", "'"));
				list[i][1] = t.substring(t.indexOf("<title>") + 7, t.indexOf("</title>") - 10);
				temp = temp.substring(temp.indexOf("\""));
			}
			inputStream.close();
			return (list);
		} catch (Exception e) {
			Logger.error(e);
		}
		return null;
	}

	private static StringBuilder read (File file) {
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
}