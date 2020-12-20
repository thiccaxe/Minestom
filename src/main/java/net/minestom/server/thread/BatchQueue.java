package net.minestom.server.thread;

import com.google.common.collect.Queues;
import net.minestom.server.lock.AcquirableElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;

public class BatchQueue {

    private final Queue<AcquirableElement.AcquisitionData> acquisitionDataQueue = Queues.newConcurrentLinkedQueue();

    private volatile Thread waitingThread;

    @NotNull
    public Queue<AcquirableElement.AcquisitionData> getQueue() {
        return acquisitionDataQueue;
    }

    @Nullable
    public Thread getWaitingThread() {
        return waitingThread;
    }

    public void setWaitingThread(@Nullable Thread waitingThread) {
        this.waitingThread = waitingThread;
    }
}
