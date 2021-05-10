package net.minestom.server.extras.placeholder;

import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RegisteredPlaceholder {
    private final @NotNull PlaceholderParser parser;
    private final @Nullable Extension extension;


    RegisteredPlaceholder(@NotNull PlaceholderParser parser, @Nullable Extension extension) {
        this.parser = parser;
        this.extension = extension;
    }

    public boolean hasExtension() {
        return extension != null;
    }

    public @NotNull PlaceholderParser getParser() {
        return parser;
    }

    public @Nullable Extension getExtension() {
        return extension;
    }

}