package net.minestom.server;

/**
 * Represents an element which should be ticked at a regular interval.
 * <p>
 * Should in most case, not be used by the end-user but only internally in the tick system.
 */
public interface Tickable {

    /**
     * Updates the element, should be called every tick.
     *
     * @param time the update time in milliseconds
     */
    void tick(long time);

}
