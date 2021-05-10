package net.minestom.server.extras.placeholder.component;

import net.kyori.adventure.text.Component;
import net.minestom.server.extras.placeholder.PlaceholderResult;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PlaceholderResultParser {
    @NotNull Component parse(PlaceholderResult result);
}
