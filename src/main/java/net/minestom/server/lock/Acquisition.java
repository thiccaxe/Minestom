package net.minestom.server.lock;

import net.minestom.server.MinecraftServer;
import net.minestom.server.thread.BatchQueue;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.utils.thread.ThreadUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class Acquisition {

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

    public static <E, T extends AcquirableElement<E>> void acquire(Collection<T> collection,
                                                                   Supplier<Collection<E>> collectionSupplier,
                                                                   Consumer<Collection<E>> consumer) {
        final Thread currentThread = Thread.currentThread();
        Collection<E> result = collectionSupplier.get();

        Map<BatchThread, List<E>> threadCacheMap = retrieveThreadMap(collection, currentThread, result::add);

        // Acquire all the threads
        {
            List<Phaser> phasers = new ArrayList<>();

            for (Map.Entry<BatchThread, List<E>> entry : threadCacheMap.entrySet()) {
                final BatchThread batchThread = entry.getKey();
                final List<E> elements = entry.getValue();

                AcquisitionData data = new AcquisitionData();

                acquire(currentThread, batchThread, data);

                // Retrieve all elements
                result.addAll(elements);

                final Phaser phaser = data.getPhaser();
                if (phaser != null) {
                    phasers.add(phaser);
                }
            }

            // Give result and deregister phasers
            consumer.accept(result);
            for (Phaser phaser : phasers) {
                phaser.arriveAndDeregister();
            }

        }
    }

    public static <E, T extends AcquirableElement<E>> void acquireForEach(@NotNull Collection<T> collection,
                                                                          @NotNull Consumer<E> consumer) {

        final Thread currentThread = Thread.currentThread();
        Map<BatchThread, List<E>> threadCacheMap = retrieveThreadMap(collection, currentThread, consumer);

        // Acquire all the threads one by one
        {
            for (Map.Entry<BatchThread, List<E>> entry : threadCacheMap.entrySet()) {
                final BatchThread batchThread = entry.getKey();
                final List<E> elements = entry.getValue();

                AcquisitionData data = new AcquisitionData();

                acquire(currentThread, batchThread, data);

                // Execute the consumer for all waiting elements
                for (E element : elements) {
                    synchronized (element) {
                        consumer.accept(element);
                    }
                }

                final Phaser phaser = data.getPhaser();
                if (phaser != null) {
                    phaser.arriveAndDeregister();
                }
            }
        }
    }

    /**
     * Notifies all the locks and wait for them to return using a {@link Phaser}.
     * <p>
     * Currently called during entities tick (TODO: chunks & instances)
     * and in {@link BatchThread.BatchRunnable#run()} after every thread-tick.
     *
     * @param queue the queue to empty containing the locks to notify
     * @see #acquire(Thread, BatchThread, AcquisitionData)
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

    /**
     * Checks if the {@link AcquirableElement} update tick is in the same thread as {@link Thread#currentThread()}.
     * If yes return immediately, otherwise a lock will be created and added to {@link BatchQueue#getQueue()}
     * to be executed later during {@link #processQueue(BatchQueue)}.
     *
     * @param data the object containing data about the acquisition
     * @return true if the acquisition didn't require any synchronization
     * @see #processQueue(BatchQueue)
     */
    protected static boolean acquire(@NotNull Thread currentThread, @Nullable BatchThread elementThread, @NotNull AcquisitionData data) {
        if (elementThread == null) {
            // Element didn't get assigned a thread yet (meaning that the element is not part of any thread)
            // Returns false in order to force synchronization (useful if this element is acquired multiple time)
            return false;
        }

        if (!elementThread.getMainRunnable().isInTick()) {
            // Element tick has ended and can therefore be directly accessed (with synchronization)
            return false;
        }

        final boolean sameThread = ThreadUtils.areSame(currentThread, elementThread);

        if (sameThread) {
            // Element can be acquired without any wait/block
            return true;
        } else {
            // Element needs to be synchronized, forward a request

            // Prevent most of contentions, the rest in handled in the acquisition scheduled service
            if (currentThread instanceof BatchThread) {
                BatchThread batchThread = (BatchThread) currentThread;
                Acquisition.processQueue(batchThread.getQueue());
            }

            try {
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

    private static <E, T extends AcquirableElement<E>> Map<BatchThread, List<E>> retrieveThreadMap(@NotNull Collection<T> collection,
                                                                                                   @NotNull Thread currentThread,
                                                                                                   @NotNull Consumer<E> consumer) {
        Map<BatchThread, List<E>> threadCacheMap = new HashMap<>();

        for (T element : collection) {
            final E value = element.unsafeUnwrap();
            final BatchThread elementThread = element.getHandler().getBatchThread();
            if (ThreadUtils.areSame(currentThread, elementThread)) {
                // The element is managed in the current thread, consumer can be immediately called
                consumer.accept(value);
            } else {
                // The element is manager in a different thread, cache it
                List<E> threadCacheList = threadCacheMap.computeIfAbsent(elementThread, batchThread -> new ArrayList<>());
                threadCacheList.add(value);
            }
        }

        return threadCacheMap;
    }

    public static final class AcquisitionData {

        private volatile Phaser phaser;

        @Nullable
        public Phaser getPhaser() {
            return phaser;
        }
    }

}
