package Discord;

import Discord.App.AppInstance;

public class ShutdownHook implements Runnable {
    NewMain main;

    public ShutdownHook(NewMain main) {
        this.main = main;
    }

    @Override
    public void run() {
        for (String s : main.map.keySet()) {
            main.map.get(s).getAppInstances().forEach(AppInstance::close);
        }
    }
}
