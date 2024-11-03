package Discord.twitchIntegration;

/**
 * <p>StreamerFeedback class.</p>
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public enum StreamerFeedback {
    /**
     * activated streamermode
     */
    ACTIVATED,
    /**
     * someone else already uses streamermode
     */
    RUNNING_BY_SOMEONE,
    /**
     * no streamerRole setup
     */
    NO_STREAMER_ROLE,
    /**
     * you arent a streamer
     */
    NO_STREAMER,
    /**
     * deactivated streamermode
     */
    DEACTIVATED,
    /**
     * error
     */
    ERROR
}
