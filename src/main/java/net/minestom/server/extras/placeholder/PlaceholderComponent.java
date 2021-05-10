package net.minestom.server.extras.placeholder;

import it.unimi.dsi.fastutil.doubles.Double2LongLinkedOpenHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.*;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEventSource;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class PlaceholderComponent implements BuildableComponent<PlaceholderComponent, PlaceholderComponent.Builder> {

    private final @NotNull Key key;
    private final @Unmodifiable @NotNull Collection<Component> arguments;

    public static @NotNull Component placeholder(Key key) {
        return new PlaceholderComponent(key, Collections.emptyList());
    }
    

    private PlaceholderComponent(@NotNull Key key, @NotNull Collection<Component> arguments) {
        this.key = key;
        this.arguments = Collections.unmodifiableCollection(arguments);
    }


    @Override
    public @NotNull PlaceholderComponent.Builder toBuilder() {
        return new Builder(key).argument(this.arguments);
    }

    @Override
    public @NotNull
    @Unmodifiable List<Component> children() {
        return null;
    }

    @Override
    public @NotNull Component children(@NotNull List<? extends ComponentLike> children) {
        return null;
    }

    @Override
    public @NotNull Component append(@NotNull Component component) {
        return null;
    }

    @Override
    public @NotNull Style style() {
        return null;
    }

    @Override
    public @NotNull Component style(@NotNull Style style) {
        return null;
    }

    @Override
    public @NotNull Component replaceText(@NotNull Consumer<TextReplacementConfig.Builder> configurer) {
        return null;
    }

    @Override
    public @NotNull Component replaceText(@NotNull TextReplacementConfig config) {
        return null;
    }


    public static class Builder implements ComponentBuilder<PlaceholderComponent, PlaceholderComponent.Builder> {

        private @NotNull Key key;
        private @NotNull
        final List<Component> arguments;

        private Builder(@NotNull Key key) {
            this(key, new ArrayList<>());
        }

        private Builder(@NotNull Key key, @NotNull List<Component> arguments) {
            this.key = key;
            this.arguments = arguments;
        }



        public @NotNull PlaceholderComponent.Builder argument(@NotNull Component argument) {
            arguments.add(argument);
            return this;
        }

        public @NotNull PlaceholderComponent.Builder argument(@NotNull Component @NotNull... arguments) {
            this.arguments.addAll(Arrays.asList(arguments));
            return this;
        }

        public @NotNull PlaceholderComponent.Builder argument(@NotNull Collection<Component> arguments) {
            this.arguments.addAll(arguments);
            return this;
        }

        public @NotNull PlaceholderComponent.Builder key(@NotNull Key key) {
            this.key = key;
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent build() {
            return new PlaceholderComponent(key, arguments);
        }


        /*
        Below Methods are UNUSED by this class.
         */

        @Override
        public @NotNull PlaceholderComponent.Builder append(@NotNull Component component) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder append(@NotNull Component @NotNull ... components) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder append(@NotNull ComponentLike @NotNull ... components) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder append(@NotNull Iterable<? extends ComponentLike> components) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder applyDeep(@NotNull Consumer<? super ComponentBuilder<?, ?>> action) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder mapChildren(@NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder mapChildrenDeep(@NotNull Function<BuildableComponent<?, ?>, ? extends BuildableComponent<?, ?>> function) {
            return this;
        }

        @Override
        public @NotNull List<Component> children() {
            return Collections.emptyList();
        }

        @Override
        public @NotNull PlaceholderComponent.Builder style(@NotNull Style style) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder style(@NotNull Consumer<Style.Builder> consumer) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder font(@Nullable Key font) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder color(@Nullable TextColor color) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder colorIfAbsent(@Nullable TextColor color) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder decoration(@NotNull TextDecoration decoration, TextDecoration.@NotNull State state) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder clickEvent(@Nullable ClickEvent event) {
            return this;
        }

        @Override
        public@NotNull PlaceholderComponent.Builder hoverEvent(@Nullable HoverEventSource<?> source) {
            return this;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder insertion(@Nullable String insertion) {
            return null;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder mergeStyle(@NotNull Component that, @NotNull Set<Style.Merge> merges) {
            return null;
        }

        @Override
        public @NotNull PlaceholderComponent.Builder resetStyle() {
            return null;
        }

    }

}
