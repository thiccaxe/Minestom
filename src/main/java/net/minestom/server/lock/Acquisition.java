package net.minestom.server.lock;

import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchQueue;
import net.minestom.server.thread.BatchThread;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Acquisition {

    private static final ScheduledExecutorService ACQUISITION_CONTENTION_SERVICE = Executors.newSingleThreadScheduledExecutor();

    static {
        ACQUISITION_CONTENTION_SERVICE.scheduleAtFixedRate(() -> {

            final Set<BatchThread> threads = MinecraftServer.getUpdateManager().getThreadProvider().getThreads();

            for (BatchThread batchThread : threads) {
                final BatchThread waitingThread = (BatchThread) batchThread.getQueue().getWaitingThread();
                if (waitingThread != null && waitingThread.getState() == Thread.State.WAITING &&
                        batchThread.getState() == Thread.State.WAITING) {
                    processQueue(waitingThread.getQueue());
                }
            }

        }, 3, 3, TimeUnit.MILLISECONDS);
    }

    public <E, T extends AcquirableElement<E>> void acquire(Collection<T> collection,
                                                            Supplier<Collection<E>> collectionSupplier,
                                                            Consumer<Collection<E>> consumer) {
        Collection<E> result = collectionSupplier.get();

        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();

        // Map the elements by their associated thread
        for (T element : collection) {
            final BatchThread elementThread = element.getHandler().getBatchThread();
            List<E> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, batchThread -> new ArrayList<>());
            threadCacheList.add(element.unsafeUnwrap());
        }

        // Acquire all the threads
        {
            // TODO
        }

        // Give result
        consumer.accept(result);
    }

    /**
     * Checks if the {@link AcquirableElement} update tick is in the same thread as {@link Thread#currentThread()}.
     * If yes return immediately, otherwise a lock will be created and added to {@link BatchQueue#getQueue()}
     * to be executed later during {@link #processQueue(BatchQueue)}.
     *
     * @param data the object containing data about the acquisition
     * @return true if the acquisition didn't require any synchronization
     */
    public static boolean acquire(@Nullable BatchThread elementThread, @NotNull AcquisitionData data) {
        if (elementThread == null) {
            // Element didn't get assigned a thread yet (meaning that the element is not part of any thread)
            // Returns false in order to force synchronization (useful if this element is acquired multiple time)
            return false;
        }

        final Thread currentThread = Thread.currentThread();

        final boolean sameThread = System.identityHashCode(elementThread) == System.identityHashCode(currentThread);

        if (sameThread) {
            // Element can be acquired without any wait/block
            return true;
        } else {
            // Element needs to be synchronized, forward a request
            try {

                // Prevent most of contentions, the rest in handled in the acquisition scheduled service
                {
                    BatchThread batchThread = (BatchThread) currentThread;
                    Acquisition.processQueue(batchThread.getQueue());
                }

                final BatchQueue periodQueue = elementThread.getQueue();
                synchronized (periodQueue) {
                    periodQueue.setWaitingThread(elementThread);
                    periodQueue.getQueue().add(data);
                    periodQueue.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return false;
        }
    }

    /**
     * Notifies all the locks and wait for them to return using a {@link Phaser}.
     * <p>
     * Currently called during entities tick (TODO: chunks & instances)
     * and in {@link BatchThread.BatchRunnable#run()} after every thread-tick.
     *
     * @param queue the queue to empty containing the locks to notify
     */
    public static void processQueue(@NotNull BatchQueue queue) {
        Queue<AcquisitionData> acquisitionQueue = queue.getQueue();

        if (acquisitionQueue.isEmpty())
            return;

        Phaser phaser = new Phaser(1);
        synchronized (queue) {
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

    public static final class AcquisitionData {

        private volatile Phaser phaser;

        @Nullable
        public Phaser getPhaser() {
            return phaser;
        }
    }

}
