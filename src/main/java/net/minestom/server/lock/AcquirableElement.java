package net.minestom.server.lock;

import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchQueue;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.thread.batch.BatchSetupHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
         * @param queue the queue to empty containing the locks to notify
         */
        public static void processQueue(@NotNull BatchQueue queue) {
            Phaser phaser = new Phaser(1);
            synchronized (queue) {
                Queue<AcquirableElement.AcquisitionData> acquisitionQueue = queue.getQueue();
                if (acquisitionQueue.isEmpty()) {
                    return;
                }

                AcquisitionData lock;
                while ((lock = acquisitionQueue.poll()) != null) {
                    lock.phaser = phaser;
                    phaser.register();
                }

                queue.setWaitingThread(null);
                queue.notifyAll();
            }

            phaser.arriveAndAwaitAdvance();
        }

        private volatile BatchThread batchThread = null;

        private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        static {
            scheduler.scheduleAtFixedRate(() -> {

                final Set<BatchThread> threads = MinecraftServer.getUpdateManager().getThreadProvider().getThreads();

                for (BatchThread batchThread : threads) {
                    final BatchThread waitingThread = (BatchThread) batchThread.getQueue().getWaitingThread();
                    if (waitingThread != null && waitingThread.getState().equals(Thread.State.WAITING)) {
                        processQueue(waitingThread.getQueue());
                    }
                }

            }, 3, 3, TimeUnit.MILLISECONDS);
        }

        /**
         * Checks if the {@link AcquirableElement} update tick is in the same thread as {@link Thread#currentThread()}.
         * If yes return immediately, otherwise a lock will be created and added to {@link BatchQueue#getQueue()}
         * to be executed later during {@link #processQueue(BatchQueue)}.
         *
         * @param lock the lock used if a thread-mismatch is found
         * @return true if the acquisition didn't require any synchronization
         */
        public boolean tryAcquisition(@NotNull AcquisitionData lock) {
            final BatchQueue periodQueue = getPeriodQueue();

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

                    // Prevent most of contentions, the rest in handled in the acquisition scheduled service
                    {
                        BatchThread batchThread = (BatchThread) currentThread;
                        processQueue(batchThread.getQueue());
                    }

                    synchronized (periodQueue) {
                        periodQueue.setWaitingThread(batchThread);
                        periodQueue.getQueue().add(lock);
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
            processQueue(batchThread.getQueue());
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

    final class AcquisitionData {

        private volatile Phaser phaser;

    }

}
