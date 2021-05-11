package net.minestom.server.extras.placeholder;

import net.kyori.adventure.text.Component;
import net.minestom.server.extras.placeholder.component.PlaceholderComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;

public class PlaceholderResult {
    private final @NotNull PlaceholderResult.ResultType resultType;
    private final @NotNull @Unmodifiable List<Component> values;

    public PlaceholderResult(@NotNull PlaceholderResult.ResultType resultType, @NotNull @Unmodifiable List<Component> values) {
        this.resultType = resultType;
        this.values = values;
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


    public static PlaceholderResult result(@NotNull PlaceholderResult.ResultType resultType, @NotNull List<Component> values) {
        return new PlaceholderResult(resultType, Collections.unmodifiableList(values));
    }

    public static PlaceholderResult result(@NotNull PlaceholderResult.ResultType resultType, @NotNull Component @NotNull... values) {
        return new PlaceholderResult(resultType, List.of(values));
    }

    public static PlaceholderResult result(@NotNull PlaceholderResult.ResultType resultType, @NotNull Component value) {
        return new PlaceholderResult(resultType, Collections.singletonList(value));
    }


    public static PlaceholderResult error(@NotNull List<Component> values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, Collections.unmodifiableList(values));
    }

    public static PlaceholderResult error(@NotNull Component @NotNull... values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, List.of(values));
    }

    public static PlaceholderResult error(@NotNull Component value) {
        return new PlaceholderResult(PlaceholderResult.ResultType.ERROR, Collections.singletonList(value));
    }

    PlaceholderResult finalise(PlaceholderComponent placeholderComponent) {
        return new FinalPlaceholderResult(this, placeholderComponent);
    }



    public static PlaceholderResult parsed(@NotNull List<Component> values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.unmodifiableList(values));
    }

    public static PlaceholderResult parsed(@NotNull Component @NotNull... values) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, List.of(values));
    }

    public static PlaceholderResult parsed(@NotNull Component value) {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.singletonList(value));
    }


    public static PlaceholderResult unknown() {
        return new PlaceholderResult(PlaceholderResult.ResultType.SUCCESS, Collections.emptyList());
    }

    public enum ResultType {
        ERROR,
        SUCCESS,
        UNKNOWN
    }

}
