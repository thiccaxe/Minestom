package net.minestom.server.extras.placeholder.component;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.extras.placeholder.PlaceholderResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public interface PlaceholderComponent extends BuildableComponent<PlaceholderComponent, PlaceholderComponent.Builder> {


    /**
     * Get the key of the placeholder.
     *
     * @return the key
     */
    @NotNull Key key();

    /**
     * Get the arguments of the placeholder
     *
     * @return the arguments
     */
    @NotNull List<Component> arguments();

    /**
     * Get the component this should be replaced with on a successful parse.
     *
     * @param result the result of the successful parse
     *
     * @return the component to replace this with
     */
    @NotNull Component success(@NotNull PlaceholderResult result);

    /**
     * Get the component this should be replaced with on a parse with error.
     *
     * @param result the result of the parse with error
     *
     * @return the component to replace this with
     */
    @NotNull Component error(@NotNull PlaceholderResult result);

    /**
     * Get the component this should be replaced with when the Key is not registered.
     *
     * @param result the result of the unknown parse
     *
     * @return the component to replace this with
     */
    @NotNull Component unknown(@NotNull PlaceholderResult result);

    default @NotNull Component set(@NotNull PlaceholderResult result) {
        switch (result.getResultType()) {
            default:
            case SUCCESS:
                return success(result);
            case ERROR:
                return error(result);
            case UNKNOWN:
                return unknown(result);
        }
    }

    /**
     * Get a builder with the specified key
     * @return a new builder
     */
    static @NotNull PlaceholderComponent.Builder builder(Key key) {
        return PlaceholderComponentBuilderImpl.builder(key);
    }


    // Following Methods are for compatibility with Adventure
    @Override
    @NotNull
    @Unmodifiable default List<Component> children() {
        return Collections.emptyList();
    }

    @Override
    default @NotNull TextComponent children(@NotNull List<? extends ComponentLike> children) {
        return Component.empty();
    }

    @Override
    default @NotNull Component append(@NotNull Component component) {
        return this;
    }

    @Override
    default @NotNull
    Style style() {
        return Style.empty();
    }

    @Override
    default @NotNull TextComponent style(@NotNull Style style) {
        return Component.empty();
    }

    @Override
    default @NotNull Component replaceText(@NotNull Consumer<TextReplacementConfig.Builder> configurer) {
        return this;
    }

    @Override
    default @NotNull Component replaceText(@NotNull TextReplacementConfig config) {
        return this;
    }


    interface Builder extends ComponentBuilder<PlaceholderComponent, PlaceholderComponent.Builder> {

        @NotNull PlaceholderComponent.Builder key(@NotNull Key placeholderKey);

        @NotNull PlaceholderComponent.Builder argument(@NotNull Component placeholderArgument);

        @NotNull PlaceholderComponent.Builder arguments(@NotNull List<Component> placeholderArguments);

        @NotNull PlaceholderComponent.Builder arguments(@NotNull Component @NotNull... placeholderArguments);

        @NotNull PlaceholderComponent.Builder clearArguments();

        @NotNull PlaceholderComponent.Builder resultSuccess(PlaceholderResultParser successParser);

        @NotNull PlaceholderComponent.Builder resultError(PlaceholderResultParser errorParser);

        @NotNull PlaceholderComponent.Builder resultUnknown(PlaceholderResultParser unknownParser);

        default @NotNull PlaceholderComponent.Builder result(@NotNull PlaceholderResult.ResultType resultType, @NotNull PlaceholderResultParser parser) {
            switch (resultType) {
                default:
                case SUCCESS:
                    return resultSuccess(parser);
                case ERROR:
                    return resultError(parser);
                case UNKNOWN:
                    return resultUnknown(parser);
            }
        }


        // Following Methods are for compatibility with Adventure
        @Override
        default @NotNull PlaceholderComponent.Builder append(@NotNull Component component) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder append(@NotNull Component @NotNull ... components) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder append(@NotNull ComponentLike @NotNull ... components) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder append(@NotNull Iterable<? extends ComponentLike> components) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder applyDeep(@NotNull Consumer<? super ComponentBuilder<?, ?>> action) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder mapChildren(@NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder mapChildrenDeep(@NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
            return this;
        }

        @Override
        default @NotNull List<Component> children() {
            return Collections.emptyList();
        }

        @Override
        default @NotNull PlaceholderComponent.Builder style(@NotNull Style style) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder style(@NotNull Consumer<Style.Builder> consumer) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder font(@Nullable Key font) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder color(@Nullable TextColor color) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder colorIfAbsent(@Nullable TextColor color) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder decoration(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder clickEvent(@Nullable ClickEvent event) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder hoverEvent(@Nullable HoverEventSource<?> source) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder insertion(@Nullable String insertion) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder mergeStyle(@NotNull Component that, @NotNull Set<Style.Merge> merges) {
            return this;
        }

        @Override
        default @NotNull PlaceholderComponent.Builder resetStyle() {
            return this;
        }
    }




}
