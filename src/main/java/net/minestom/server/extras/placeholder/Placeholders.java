package net.minestom.server.extras.placeholder;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.extensions.Extension;
import net.minestom.server.extras.placeholder.component.PlaceholderComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class Placeholders {

    private static final HashMap<Key, RegisteredPlaceholder> registeredPlaceholders = new HashMap<>();


    public static void registerPlaceholder(@NotNull Key key, @NotNull PlaceholderParser parser, @Nullable Extension extension) {
        if (registeredPlaceholders.containsKey(key)) {
            throw new IllegalArgumentException("Placeholder is already registered");
        }
        registeredPlaceholders.put(key, new RegisteredPlaceholder(parser, extension));
    }

    public static void registerPlaceholder(@NotNull Key key, @NotNull PlaceholderParser parser) {
        registerPlaceholder(key, parser, null);
    }

    public static boolean placeholderRegistered(Key key) {
        return registeredPlaceholders.containsKey(key);
    }

    public static Component setPlaceholder(PlaceholderComponent placeholderComponent) {
        FinalPlaceholderResult placeholderResult = PlaceholderResult.unknown().finalise(placeholderComponent);
        if (placeholderRegistered(placeholderComponent.key())) {
            try {
                placeholderResult = registeredPlaceholders.get(placeholderComponent.key()).getParser().set(placeholderComponent).finalise(placeholderComponent);
            } catch (Exception e) {
                placeholderResult = PlaceholderResult.error().finalise(placeholderComponent);
            }
        }
        return placeholderComponent.set(placeholderResult);
    }








}

