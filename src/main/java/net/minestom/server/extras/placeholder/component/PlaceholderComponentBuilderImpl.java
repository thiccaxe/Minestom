package net.minestom.server.extras.placeholder.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

final class PlaceholderComponentBuilderImpl implements PlaceholderComponent.Builder{

    private static final PlaceholderResultParser SUCCESS_PARSER = result -> Component.join(Component.text(" "), result.getValues());
    private static final PlaceholderResultParser ERROR_PARSER = result -> Component.text("Error: ").append(Component.join(Component.text(" "), result.getValues()));
    private static final PlaceholderResultParser UNKNOWN_PARSER = result -> Component.text(result.getPlaceholder().key().value() + ": ")
            .append(Component.join(Component.text(" "), result.getValues()));



    private @NotNull Key placeholderKey;
    private @NotNull final List<Component> placeholderArguments;
    private @NotNull PlaceholderResultParser successParser;
    private @NotNull PlaceholderResultParser errorParser;
    private @NotNull PlaceholderResultParser unknownParser;


    @ParametersAreNonnullByDefault
    private PlaceholderComponentBuilderImpl(Key placeholderKey,
                                     List<Component> placeholderArguments,
                                     PlaceholderResultParser successParser,
                                     PlaceholderResultParser errorParser,
                                     PlaceholderResultParser unknownParser
    ) {
        this.placeholderKey = placeholderKey;
        this.placeholderArguments = placeholderArguments;
        this.successParser = successParser;
        this.errorParser = errorParser;
        this.unknownParser = unknownParser;
    }


    @Override
    public @NotNull PlaceholderComponent build() {
        return new PlaceholderComponentImpl(
                placeholderKey,
                placeholderArguments,
                successParser,
                errorParser,
                unknownParser
        );
    }

    @Override
    public @NotNull PlaceholderComponent.Builder key(@NotNull Key placeholderKey) {
        this.placeholderKey = placeholderKey;
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder argument(@NotNull Component placeholderArgument) {
        this.placeholderArguments.add(placeholderArgument);
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder arguments(@NotNull List<Component> placeholderArguments) {
        this.placeholderArguments.addAll(placeholderArguments);
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder arguments(@NotNull Component @NotNull ... placeholderArguments) {
        this.placeholderArguments.addAll(List.of(placeholderArguments));
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder clearArguments() {
        placeholderArguments.clear();
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder resultSuccess(PlaceholderResultParser successParser) {
        this.successParser = successParser;
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder resultError(PlaceholderResultParser errorParser) {
        this.errorParser = errorParser;
        return this;
    }

    @Override
    public @NotNull PlaceholderComponent.Builder resultUnknown(PlaceholderResultParser unknownParser) {
        this.unknownParser = unknownParser;
        return this;
    }

    public static PlaceholderComponent.Builder builder(Key key) {
        return new PlaceholderComponentBuilderImpl(key, new ArrayList<>(), SUCCESS_PARSER, ERROR_PARSER, UNKNOWN_PARSER);
    }
}
