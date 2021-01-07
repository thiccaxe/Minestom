package net.minestom.server.event.player;

import net.minestom.server.entity.Player;
import net.minestom.server.event.CancellableEvent;
import net.minestom.server.event.PlayerEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PlayerItemDropEvent extends PlayerEvent implements CancellableEvent {

    private final ItemStack itemStack;

    private boolean cancelled;

    public PlayerItemDropEvent(@NotNull Player player, @NotNull ItemStack itemStack) {
        super(player);
        this.itemStack = itemStack;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
