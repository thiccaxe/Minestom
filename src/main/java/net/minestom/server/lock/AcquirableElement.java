package net.minestom.server.lock;

import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchSetupHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Queue;
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
        AcquisitionData acquisitionData = new AcquisitionData();

        boolean sameThread = getHandler().tryAcquisition(acquisitionData);
        final T unwrap = unsafeUnwrap();
        if (sameThread) {
            consumer.accept(unwrap);
        } else {
            synchronized (unwrap) {
                consumer.accept(unwrap);

                // Notify the end of the tasks if required
                Phaser phaser = acquisitionData.phaser;
                if (phaser != null) {
                    acquisitionData.phaser.arriveAndDeregister();
                }
            }
        }
    }

    @NotNull
    T unsafeUnwrap();

    @NotNull
    Handler getHandler();

    class Handler {

        /**
         * Notifies all the locks and wait for them to return using a {@link Phaser}.
         * <p>
         * Currently called during entities tick (TODO: chunks & instances)
         * and in {@link BatchThread.BatchRunnable#run()} after every thread-tick.
         *
         * @param acquisitionQueue the queue to empty containing the locks to notify
         */
        public static void processQueue(@NotNull Queue<AcquisitionData> acquisitionQueue) {
            Phaser phaser = new Phaser(1);
            synchronized (acquisitionQueue) {
                if (acquisitionQueue.isEmpty()) {
                    return;
                }

                AcquisitionData lock;
                while ((lock = acquisitionQueue.poll()) != null) {
                    lock.phaser = phaser;
                    phaser.register();
                }

                acquisitionQueue.notifyAll();
            }

            phaser.arriveAndAwaitAdvance();
        }

        private volatile BatchThread batchThread = null;

        /**
         * Checks if the {@link AcquirableElement} update tick is in the same thread as {@link Thread#currentThread()}.
         * If yes return immediately, otherwise a lock will be created and added to {@link BatchThread#getWaitingAcquisitionQueue()}
         * to be executed later during {@link #processQueue(Queue)}.
         *
         * @param lock the lock used if a thread-mismatch is found
         * @return true if the acquisition didn't require any synchronization
         */
        public boolean tryAcquisition(@NotNull AcquisitionData lock) {
            final Queue<AcquisitionData> periodQueue = getPeriodQueue();

            final Thread currentThread = Thread.currentThread();

            if (batchThread == null) {
                // Element didn't get assigned a thread yet (meaning that the element is not part of any thread)
                // Returns false in order to force synchronization (useful if this element is acquired multiple time)
                return false;
            }

            final boolean sameThread = System.identityHashCode(batchThread) == System.identityHashCode(currentThread);

            if (sameThread) {
                // Element can be acquired without any wait/block
                return true;
            } else {
                // Element needs to be synchronized, forward a request
                try {
                    // FIXME: multiple threads trying to acquire object from each other, they end up waiting forever
                    synchronized (periodQueue) {
                        periodQueue.add(lock);
                        periodQueue.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return false;
            }
        }

        /**
         * Specifies in which thread this element will be updated.
         * Currently defined before every tick for all game elements in {@link BatchSetupHandler#pushTask(List, long)}.
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
            processQueue(batchThread.getWaitingAcquisitionQueue());
        }

        /**
         * Gets the acquisition queue linked to this element's thread.
         *
         * @return the acquisition queue
         */
        public Queue<AcquisitionData> getPeriodQueue() {
            return batchThread != null ? batchThread.getWaitingAcquisitionQueue() : null;
        }
    }

    final class AcquisitionData {

        private volatile Phaser phaser;

    }

}
