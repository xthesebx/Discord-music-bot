package Discord;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.Stack;

public class Shutdownhook extends Thread{
	Main main;
	String id;
	
	public Shutdownhook(Main main, String id) {
		this.main = main;
		this.id = id;
	}
	@Override
	public void run () {
		while (!((Stack<String>) Main.map.get(id).get("ids")).isEmpty()) {
			TextChannel textChannel = main.jda.getTextChannelById(((Stack<String>) Main.map.get(id).get("channelIds")).pop());
			assert textChannel != null;
			textChannel.deleteMessageById(((Stack<String>) Main.map.get(id).get("ids")).pop()).complete();
		}
	}
}
