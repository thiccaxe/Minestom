package net.minestom.server;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minestom.server.entity.EntityManager;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.lock.Acquisition;
import net.minestom.server.monitoring.TickMonitor;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.thread.PerInstanceThreadProvider;
import net.minestom.server.thread.ThreadProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

/**
 * Manager responsible for the server ticks.
 * <p>
 * The {@link ThreadProvider} manages the multi-thread aspect for {@link Instance} ticks,
 * it can be modified with {@link #setThreadProvider(ThreadProvider)}.
 */
public final class UpdateManager {

    public final static Logger LOGGER = LoggerFactory.getLogger(UpdateManager.class);

    private final ScheduledExecutorService updateExecutionService = Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder().setNameFormat(MinecraftServer.THREAD_NAME_UPDATE).build()
    );

    private volatile boolean stopRequested;

    private ThreadProvider threadProvider;

    private final Queue<DoubleConsumer> tickStartCallbacks = Queues.newConcurrentLinkedQueue();
    private final Queue<DoubleConsumer> tickEndCallbacks = Queues.newConcurrentLinkedQueue();
    private final List<Consumer<TickMonitor>> tickMonitors = new CopyOnWriteArrayList<>();

    {
        // DEFAULT THREAD PROVIDER
        //threadProvider = new PerGroupChunkProvider();
        //threadProvider = new PerInstanceThreadProvider();

        final int threadCount = 2;

        //threadProvider = new PerChunkThreadProvider(threadCount);
        threadProvider = new PerInstanceThreadProvider(threadCount);
    }

    /**
     * Should only be created in MinecraftServer.
     */
    protected UpdateManager() {
    }

    /**
     * Starts the server loop in the update thread.
     */
    protected void start() {
        final ConnectionManager connectionManager = MinecraftServer.getConnectionManager();

        updateExecutionService.scheduleAtFixedRate(() -> {
            try {
                if (stopRequested) {
                    updateExecutionService.shutdown();
                    return;
                }

                long currentTime = System.nanoTime();
                final long tickStart = System.currentTimeMillis();

                // Tick start callbacks
                doTickCallback(tickStartCallbacks, tickStart);

                // Waiting players update (newly connected clients waiting to get into the server)
                connectionManager.updateWaitingPlayers();

                // Keep Alive Handling
                connectionManager.handleKeepAlive(tickStart);

                // Server tick (chunks/entities)
                serverTick(tickStart);

                // the time that the tick took in nanoseconds
                final long tickTime = System.nanoTime() - currentTime;
                final double tickTimeMs = tickTime / 1e6D;

                // Tick end callbacks
                doTickCallback(tickEndCallbacks, tickTimeMs);

                // Monitoring
                if (!tickMonitors.isEmpty()) {
                    final double acquisitionTimeMs = Acquisition.getCurrentWaitMonitoring() / 1e6D;
                    final TickMonitor tickMonitor = new TickMonitor(tickTimeMs, acquisitionTimeMs);

                    this.tickMonitors.forEach(consumer -> consumer.accept(tickMonitor));

                    Acquisition.resetWaitMonitoring();
                }

            } catch (Exception e) {
                MinecraftServer.getExceptionManager().handleException(e);
            }
        }, 0, MinecraftServer.TICK_MS, TimeUnit.MILLISECONDS);
    }

    /**
     * Executes a server tick and returns only once all the futures are completed.
     *
     * @param tickStart the time of the tick in milliseconds
     */
    private void serverTick(long tickStart) {
        // Server tick (instance/chunk/entity)
        // Synchronize with the update manager instance, like the signal for chunk load/unload
        synchronized (this) {
            this.threadProvider.update(tickStart);
        }

        CountDownLatch countDownLatch = threadProvider.notifyThreads();

        // Wait tick end
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }
    }

    /**
     * Used to execute tick-related callbacks.
     *
     * @param callbacks the callbacks to execute
     * @param value     the value to give to the consumers
     */
    private void doTickCallback(@NotNull Queue<DoubleConsumer> callbacks, double value) {
        if (!callbacks.isEmpty()) {
            DoubleConsumer callback;
            while ((callback = callbacks.poll()) != null) {
                callback.accept(value);
            }
        }
    }

    /**
     * Gets the current {@link ThreadProvider}.
     *
     * @return the current thread provider
     */
    @NotNull
    public ThreadProvider getThreadProvider() {
        return threadProvider;
    }

    /**
     * Changes the server {@link ThreadProvider}.
     *
     * @param threadProvider the new thread provider
     */
    public synchronized void setThreadProvider(@NotNull ThreadProvider threadProvider) {
        if (this.threadProvider != null) {
            // Shutdown the previous thread provider if any
            this.threadProvider.shutdown();
        }
        this.threadProvider = threadProvider;
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been created.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public synchronized void signalInstanceCreate(@NotNull Instance instance) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onInstanceCreate(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that an instance has been deleted.
     * <p>
     * WARNING: should be automatically done by the {@link InstanceManager}.
     *
     * @param instance the instance
     */
    public synchronized void signalInstanceDelete(@NotNull Instance instance) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onInstanceDelete(instance);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been loaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkLoad(@NotNull Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkLoad(instance, chunkX, chunkZ);
    }

    /**
     * Signals the {@link ThreadProvider} that a chunk has been unloaded.
     * <p>
     * WARNING: should be automatically done by the {@link Instance} implementation.
     *
     * @param instance the instance of the chunk
     * @param chunkX   the chunk X
     * @param chunkZ   the chunk Z
     */
    public synchronized void signalChunkUnload(@NotNull Instance instance, int chunkX, int chunkZ) {
        if (this.threadProvider == null)
            return;
        this.threadProvider.onChunkUnload(instance, chunkX, chunkZ);
    }

    /**
     * Adds a callback executed at the start of the next server tick.
     * <p>
     * The long in the consumer represents the starting time (in ms) of the tick.
     *
     * @param callback the tick start callback
     */
    public void addTickStartCallback(@NotNull DoubleConsumer callback) {
        this.tickStartCallbacks.add(callback);
    }

    /**
     * Removes a tick start callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickStartCallback(@NotNull DoubleConsumer callback) {
        this.tickStartCallbacks.remove(callback);
    }

    /**
     * Adds a callback executed at the end of the next server tick.
     * <p>
     * The long in the consumer represents the duration (in ms) of the tick.
     *
     * @param callback the tick end callback
     */
    public void addTickEndCallback(@NotNull DoubleConsumer callback) {
        this.tickEndCallbacks.add(callback);
    }

    /**
     * Removes a tick end callback.
     *
     * @param callback the callback to remove
     */
    public void removeTickEndCallback(@NotNull DoubleConsumer callback) {
        this.tickEndCallbacks.remove(callback);
    }

    public void addTickMonitor(@NotNull Consumer<TickMonitor> consumer) {
        this.tickMonitors.add(consumer);
    }

    /**
     * Stops the server loop.
     */
    public void stop() {
        stopRequested = true;
    }
}
