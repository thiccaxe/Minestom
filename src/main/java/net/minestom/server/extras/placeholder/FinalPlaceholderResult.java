package net.minestom.server.extras.placeholder;

import net.kyori.adventure.text.Component;
import net.minestom.server.extras.placeholder.component.PlaceholderComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

public final class FinalPlaceholderResult extends PlaceholderResult {

    private final @NotNull PlaceholderComponent placeholder;

    FinalPlaceholderResult(@NotNull PlaceholderResult.ResultType resultType, @NotNull @Unmodifiable List<Component> values, @NotNull PlaceholderComponent placeholder) {
        super(resultType, values);
        this.placeholder = placeholder;
    }

    FinalPlaceholderResult(@NotNull PlaceholderResult placeholderResult, @NotNull PlaceholderComponent placeholder) {
        this(placeholderResult.getResultType(), placeholderResult.getValues(), placeholder);
    }

    /**
     * Get the original placeholder component.
     * @return the original placeholder
     */
    public @NotNull PlaceholderComponent getPlaceholder() {
        return placeholder;
    }
}
