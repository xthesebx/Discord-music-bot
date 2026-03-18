package Discord;


import Discord.App.AppInstance;

/**
 * <p>ShutdownHook class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public class ShutdownHook implements Runnable {
    NewMain main;

    /**
     * <p>Constructor for ShutdownHook.</p>
     *
     * @param main a {@link Discord.NewMain} object
     */
    public ShutdownHook(NewMain main) {
        this.main = main;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        for (Long s : main.map.keySet()) {
            main.map.get(s).getAppInstances().values().forEach(AppInstance::close);
            if (NewMain.client.getLinkIfCached(s) != null)
                NewMain.client.getLinkIfCached(s).destroy();
            main.map.get(s).leave();
        }
    }
}
