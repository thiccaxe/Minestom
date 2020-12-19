package net.minestom.server.lock;

import org.jetbrains.annotations.NotNull;

/**
 * Represents an element that have a {@link AcquirableElement} linked to it.
 * <p>
 * Useful if you want to provide an access point to an object without risking to compromise
 * the thread-safety of your code.
 *
 * @param <T> the element type
 */
public interface LockedElement<T> {

    /**
     * Gets the {@link AcquirableElement} of this locked element.
     * <p>
     * Should be a constant.
     *
     * @return the acquirable element linked to this object
     */
    @NotNull
    AcquirableElement<T> getAcquiredElement();

}
