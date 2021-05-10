package net.minestom.server.extras.placeholder;

import net.minestom.server.extras.placeholder.component.PlaceholderComponent;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PlaceholderParser {
    @NotNull PlaceholderResult set(@NotNull PlaceholderComponent placeholder);
}

