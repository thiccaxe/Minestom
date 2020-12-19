package net.minestom.server.lock.type;

import net.minestom.server.lock.AcquirableElement;
import org.jetbrains.annotations.NotNull;

public class AcquirableImpl<T> implements AcquirableElement<T> {

    private final T value;
    private final Handler handler;

    public AcquirableImpl(@NotNull T value) {
        this.value = value;
        this.handler = new Handler();
    }

    @NotNull
    @Override
    public T unsafeUnwrap() {
        return value;
    }

    @NotNull
    @Override
    public Handler getHandler() {
        return handler;
    }
}
