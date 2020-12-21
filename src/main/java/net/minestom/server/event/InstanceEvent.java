package net.minestom.server.event;

import net.minestom.server.instance.Instance;
import net.minestom.server.lock.Acquirable;
import org.jetbrains.annotations.NotNull;

public class InstanceEvent extends Event {

    protected final Instance instance;

    public InstanceEvent(@NotNull Instance instance) {
        this.instance = instance;
    }

    @NotNull
    public Acquirable<Instance> getAcquirablePlayer() {
        return instance.getAcquiredElement();
    }

    /**
     * Gets the instance.
     *
     * @return instance
     */
    @Deprecated
    @NotNull
    public Instance getInstance() {
        return instance;
    }
}