package net.minestom.server.lock;

import net.minestom.server.thread.BatchQueue;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchSetupHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Represents an element which can be acquired.
 * Used for synchronization purpose.
 * <p>
 * Implementations of this class are recommended to be immutable (or at least thread-safe).
 * The default one is {@link net.minestom.server.lock.type.AcquirableImpl}.
 *
 * @param <T> the acquirable object type
 */
public interface AcquirableElement<T> {

    default void acquire(@NotNull Consumer<T> consumer) {
        final Thread currentThread = Thread.currentThread();
        Acquisition.AcquisitionData data = new Acquisition.AcquisitionData();

        final boolean sameThread = Acquisition.acquire(currentThread, getHandler().getBatchThread(), data);
        final T unwrap = unsafeUnwrap();
        if (sameThread) {
            consumer.accept(unwrap);
        } else {
            synchronized (unwrap) {
                consumer.accept(unwrap);

                // Notify the end of the tasks if required
                Phaser phaser = data.getPhaser();
                if (phaser != null) {
                    phaser.arriveAndDeregister();
                }
            }
        }
    }

    @NotNull
    T unsafeUnwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        private volatile BatchThread batchThread = null;

        @Nullable
        public BatchThread getBatchThread() {
            return batchThread;
        }

        /**
         * Specifies in which thread this element will be updated.
         * Currently defined before every tick for all game elements in {@link BatchSetupHandler#pushTask(Set, long)}.
         *
         * @param batchThread the thread where this element will be updated
         */
        public void refreshThread(@NotNull BatchThread batchThread) {
            this.batchThread = batchThread;
        }

        /**
         * Executed during this element tick to empty the current thread acquisition queue.
         */
        public void acquisitionTick() {
            Acquisition.processQueue(batchThread.getQueue());
        }

        /**
         * Gets the acquisition queue linked to this element's thread.
         *
         * @return the acquisition queue
         */
        public BatchQueue getPeriodQueue() {
            return batchThread != null ? batchThread.getQueue() : null;
        }
    }

}
