package net.minestom.server.event;

import net.minestom.server.entity.Entity;
import net.minestom.server.lock.Acquirable;
import org.jetbrains.annotations.NotNull;

public class EntityEvent extends Event {

    protected final Entity entity;

    public EntityEvent(@NotNull Entity entity) {
        this.entity = entity;
    }

    @NotNull
    public Acquirable<Entity> getAcquirableEntity() {
        return entity.getAcquiredElement();
    }

    /**
     * Gets the entity of this event.
     *
     * @return the entity
     */
    @Deprecated
    @NotNull
    public Entity getEntity() {
        return entity;
    }
}
