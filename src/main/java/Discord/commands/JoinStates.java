package Discord.commands;

/**
 * enum for the different join-states
 *
 * @author sebas
 * @version $Id: $Id
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
