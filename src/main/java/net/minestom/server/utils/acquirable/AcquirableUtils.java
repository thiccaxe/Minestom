package net.minestom.server.utils.acquirable;

import net.minestom.server.lock.Acquirable;
import net.minestom.server.lock.LockedElement;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public final class AcquirableUtils {

    @Nullable
    public static <T> Acquirable<T> getOptionalAcquirable(LockedElement element) {
        return element != null ? element.getAcquiredElement() : null;
    }

    public static <T> void forEachUnwrap(Collection<Acquirable<T>> collection, Consumer<T> action) {
        Objects.requireNonNull(action);
        for (Acquirable<T> t : collection) {
            action.accept(t.unsafeUnwrap());
        }
    }

}
