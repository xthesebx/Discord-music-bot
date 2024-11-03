package Discord;


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
        for (String s : main.map.keySet()) {
            main.map.get(s).getAppInstances().forEach(instance -> {
                instance.close();
            });
        }
    }
}
