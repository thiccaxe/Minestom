package net.minestom.server.extras.placeholder;

import net.kyori.adventure.text.Component;
import net.minestom.server.extras.placeholder.component.PlaceholderComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public final class PlaceholderResult {
    private final @NotNull PlaceholderResult.ResultType resultType;
    private final @NotNull @Unmodifiable List<Component> values;
    private final @NotNull PlaceholderComponent placeholder;

    private PlaceholderResult(@NotNull PlaceholderResult.ResultType resultType, @NotNull @Unmodifiable List<Component> values, @NotNull PlaceholderComponent placeholder) {
        this.resultType = resultType;
        this.values = values;
        this.placeholder = placeholder;
    }


    /**
     * get the result type of the parse.
     * @return the result type.
     */
    public @NotNull ResultType getResultType() {
        return resultType;
    }

    /**
     * get the values returned by the parse.
     * @return the values.
     */
    public @NotNull @Unmodifiable List<Component> getValues() {
        return values;
    }

    /**
     * Get the original placeholder component.
     * @return the original placeholder
     */
    public @NotNull PlaceholderComponent getPlaceholder() {
        return placeholder;
    }

    public static PlaceholderResult result(@NotNull PlaceholderComponent placeholder, @NotNull PlaceholderResult.ResultType resultType, @NotNull List<Component> values) {
        return new PlaceholderResult(resultType, Collections.unmodifiableList(values), placeholder);
    }

    public static PlaceholderResult result(@NotNull PlaceholderComponent placeholder, @NotNull PlaceholderResult.ResultType resultType, @NotNull Component @NotNull... values) {
        return new PlaceholderResult(resultType, List.of(values), placeholder);
    }

    public static PlaceholderResult result(@NotNull PlaceholderComponent placeholder, @NotNull PlaceholderResult.ResultType resultType, @NotNull Component value) {
        return new PlaceholderResult(resultType, Collections.singletonList(value), placeholder);
    }


    public static PlaceholderResult error(@NotNull PlaceholderComponent placeholder, @NotNull List<Component> values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, Collections.unmodifiableList(values), placeholder);
    }

    public static PlaceholderResult error(@NotNull PlaceholderComponent placeholder, @NotNull Component @NotNull... values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, List.of(values), placeholder);
    }

    public static PlaceholderResult error(@NotNull PlaceholderComponent placeholder, @NotNull Component value) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, Collections.singletonList(value), placeholder);
    }



    public static PlaceholderResult parsed(@NotNull PlaceholderComponent placeholder, @NotNull List<Component> values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.unmodifiableList(values), placeholder);
    }

    public static PlaceholderResult parsed(@NotNull PlaceholderComponent placeholder, @NotNull Component @NotNull... values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, List.of(values), placeholder);
    }

    public static PlaceholderResult parsed(@NotNull PlaceholderComponent placeholder, @NotNull Component value) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.singletonList(value), placeholder);
    }


    public static PlaceholderResult unknown(@NotNull PlaceholderComponent placeholder) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.emptyList(), placeholder);
    }

    public enum ResultType {
        ERROR,
        SUCCESS,
        UNKNOWN
    }

}
