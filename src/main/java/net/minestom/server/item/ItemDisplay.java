package net.minestom.server.item;

import net.minestom.server.chat.ColoredText;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ItemDisplay {

    private ColoredText displayName;
    private ArrayList<ColoredText> lore;

    public ItemDisplay(@Nullable ColoredText displayName, @Nullable ArrayList<ColoredText> lore) {
        this.displayName = displayName;
        this.lore = lore;
    }

    /**
     * Gets the item display name.
     *
     * @return the item display name, can be null
     */
    @Nullable
    public ColoredText getDisplayName() {
        return displayName;
    }

    public boolean hasDisplayName() {
        return displayName != null;
    }

    /**
     * Gets the item lore.
     *
     * @return the item lore, can be null
     */
    @Nullable
    public ArrayList<ColoredText> getLore() {
        return lore;
    }

    public boolean hasLore() {
        return lore != null;
    }
}
