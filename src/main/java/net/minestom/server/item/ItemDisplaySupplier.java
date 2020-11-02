package net.minestom.server.item;

import net.minestom.server.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * Used by {@link ItemStack#setCustomDisplaySupplier(ItemDisplaySupplier)}.
 */
@FunctionalInterface
public interface ItemDisplaySupplier {

    /**
     * Retrieve an {@link ItemDisplay} to show something specific to {@code player}.
     *
     * @param player the player needing to receive the item
     * @return an item display
     */
    ItemDisplay getDisplay(@Nullable Player player);
}
