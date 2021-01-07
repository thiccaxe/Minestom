package net.minestom.server.event;

import net.minestom.server.entity.Player;
import net.minestom.server.lock.Acquirable;
import org.jetbrains.annotations.NotNull;

public class PlayerEvent extends Event {

    protected final Player player;

    public PlayerEvent(@NotNull Player player) {
        this.player = player;
    }

    @NotNull
    public Acquirable<Player> getAcquirablePlayer() {
        return player.getAcquiredElement();
    }

    /**
     * Gets the player.
     *
     * @return the player
     * @deprecated use {@link #getAcquirablePlayer()} instead
     */
    @Deprecated
    @NotNull
    public Player getPlayer() {
        return player;
    }
}
