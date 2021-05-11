package net.minestom.server.extras.placeholder.component;


import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.extras.placeholder.FinalPlaceholderResult;
import net.minestom.server.extras.placeholder.PlaceholderResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

final class PlaceholderComponentImpl implements PlaceholderComponent {

    private final @NotNull Key placeholderKey;
    private final @NotNull @Unmodifiable List<Object> placeholderArguments;
    private final @NotNull PlaceholderResultParser successParser;
    private final @NotNull PlaceholderResultParser errorParser;
    private final @NotNull PlaceholderResultParser unknownParser;

    @ParametersAreNonnullByDefault
    PlaceholderComponentImpl(Key placeholderKey,
                             @Unmodifiable List<Object> placeholderArguments,
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
    public @NotNull Key key() {
        return placeholderKey;
    }

    @Override
    public @NotNull @Unmodifiable List<Object> arguments() {
        return placeholderArguments;
    }

    @Override
    public @NotNull Component success(@NotNull FinalPlaceholderResult result) {
        return successParser.parse(result);
    }

    @Override
    public @NotNull Component error(@NotNull FinalPlaceholderResult result) {
        return errorParser.parse(result);
    }

    @Override
    public @NotNull Component unknown(@NotNull FinalPlaceholderResult result) {
        return unknownParser.parse(result);
    }

    @Override
    public @NotNull PlaceholderComponent.Builder toBuilder() {
        return PlaceholderComponentBuilderImpl.builder(placeholderKey)
                .arguments(placeholderArguments)
                .resultSuccess(successParser)
                .resultError(errorParser)
                .resultUnknown(unknownParser);
    }
}
