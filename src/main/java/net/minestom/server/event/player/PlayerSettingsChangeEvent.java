package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called after the player signals the server that his settings has been modified.
 * <p>
 * You can retrieve the new player settings with {@link Player#getSettings()}.
 */
public class PlayerSettingsChangeEvent extends PlayerEvent {

    public PlayerSettingsChangeEvent(@NotNull Player player) {
        super(player);
    }

}
