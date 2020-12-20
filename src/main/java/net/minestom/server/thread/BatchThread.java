package net.minestom.server.thread;

import net.minestom.server.MinecraftServer;
import net.minestom.server.lock.AcquirableElement;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;

public class BatchThread extends Thread {

    private final BatchRunnable runnable;

    private final BatchQueue queue;

    private int cost;

    public BatchThread(@NotNull BatchRunnable runnable, int number) {
        super(runnable, MinecraftServer.THREAD_NAME_TICK + "-" + number);
        this.runnable = runnable;
        this.queue = new BatchQueue();

        this.runnable.setLinkedThread(this);
    }

    public int getCost() {
        return cost;
    }

    @NotNull
    public BatchRunnable getMainRunnable() {
        return runnable;
    }

    @NotNull
    public BatchQueue getQueue() {
        return queue;
    }

    public void addRunnable(@NotNull Runnable runnable, int cost) {
        this.runnable.queue.add(runnable);
        this.cost += cost;
    }

    public void shutdown() {
        this.runnable.stop = true;
    }

    public static class BatchRunnable implements Runnable {

        private volatile boolean stop;
        private BatchThread batchThread;

        private volatile CountDownLatch countDownLatch;

        private final Queue<Runnable> queue = new ArrayDeque<>();

        @Override
        public void run() {
            Check.notNull(batchThread, "The linked BatchThread cannot be null!");
            while (!stop) {

                // The latch is necessary to control the tick rates
                if (countDownLatch == null)
                    continue;

                synchronized (this) {
                    // Execute all pending runnable
                    Runnable runnable;
                    while ((runnable = queue.poll()) != null) {
                        runnable.run();
                    }

                    batchThread.cost = 0;

                    // Execute waiting acquisition
                    {
                        AcquirableElement.Handler.processQueue(batchThread.getQueue());
                    }

                    this.countDownLatch.countDown();
                    this.countDownLatch = null;

                    // Wait for the next notify (game tick)
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public synchronized void startTick(@NotNull CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
            this.notifyAll();
        }

        private void setLinkedThread(BatchThread batchThread) {
            this.batchThread = batchThread;
        }
    }

}
