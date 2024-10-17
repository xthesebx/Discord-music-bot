package Discord;

/**
 * enum for the different join-states
 *
 * @author xXTheSebXx
 * @version 1.0-SNAPSHOT
 */
public enum JoinStates {
    /**
     * No permissions to join
     */
    NOPERMS,
    /**
     * User not in voice channel
     */
    NOTINVOICE,
    /**
     * bot already connected
     */
    ALREADYCONNECTED,
    /**
     * joined successfully
     */
    JOINED
}
