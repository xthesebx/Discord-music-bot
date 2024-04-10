package Discord;

public class DisconnectTimer implements Runnable{
	
	Main main;
	String id;
	public DisconnectTimer(Main main, String id) {
		this.main = main;
		this.id = id;
	}
	@Override
	public void run () {
		try {
			Thread.sleep(300000);
			main.jda.getGuildById(id).getAudioManager().closeAudioConnection();
			Thread t = new Thread(new Shutdownhook(main, id));
			t.start();
		} catch (InterruptedException ignored) {
		
		}
	}
}
