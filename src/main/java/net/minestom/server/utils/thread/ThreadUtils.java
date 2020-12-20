package net.minestom.server.utils.thread;

import org.jetbrains.annotations.Nullable;

/**
 * Helpers class for {@link Thread}.
 */
public final class ThreadUtils {

    /**
     * Gets if two threads are the same.
     * <p>
     * Used for readability.
     *
     * @param t1 the first thread
     * @param t2 the second thread
     * @return true if {@code t1} and {@code t2} represent the same OS thread
     */
    public static boolean areSame(@Nullable Thread t1, @Nullable Thread t2) {
        return t1 == t2;
    }

}
