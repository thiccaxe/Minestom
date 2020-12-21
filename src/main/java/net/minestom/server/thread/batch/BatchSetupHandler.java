package net.minestom.server.thread.batch;

import net.minestom.server.Tickable;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Chunk;
import net.minestom.server.instance.Instance;
import net.minestom.server.lock.Acquirable;
import net.minestom.server.thread.BatchThread;
import net.minestom.server.utils.callback.validator.EntityValidator;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Set;

public class BatchSetupHandler implements BatchHandler {

    private static final int INSTANCE_COST = 5;
    private static final int CHUNK_COST = 5;
    private static final int ENTITY_COST = 5;

    private final ArrayList<Acquirable<?>> elements = new ArrayList<>();
    private int estimatedCost;

    @Override
    public void updateInstance(@NotNull Instance instance, long time) {
        addAcquirable(instance.getAcquiredElement(), INSTANCE_COST);
    }

    @Override
    public void updateChunk(@NotNull Instance instance, @NotNull Chunk chunk, long time) {
        addAcquirable(chunk.getAcquiredElement(), CHUNK_COST);
    }

    @Override
    public void conditionalEntityUpdate(@NotNull Instance instance, @NotNull Chunk chunk, long time,
                                        @Nullable EntityValidator condition) {
        final Set<Entity> entities = instance.getChunkEntities(chunk);

        for (Entity entity : entities) {
            if (shouldTick(entity, condition)) {
                addAcquirable(entity.getAcquiredElement(), ENTITY_COST);
            }
        }
    }

    public void pushTask(@NotNull Set<BatchThread> threads, long time) {
        BatchThread fitThread = null;
        int minCost = Integer.MAX_VALUE;

        // Find the thread with the lowest number of tasks
        for (BatchThread thread : threads) {
            final boolean switchThread = fitThread == null || thread.getCost() < minCost;
            if (switchThread) {
                fitThread = thread;
                minCost = thread.getCost();
            }
        }

        Check.notNull(fitThread, "The task thread returned null, something went terribly wrong.");

        // The thread has been decided, all elements need to be have its identifier
        {
            for (Acquirable<?> element : elements) {
                element.getHandler().refreshThread(fitThread);
            }
        }

        // Create the runnable and send it to the thread for execution in the next tick
        final Runnable runnable = createRunnable(time);
        fitThread.addRunnable(runnable, estimatedCost);
    }

    @NotNull
    private Runnable createRunnable(long time) {
        return () -> {
            for (Acquirable<?> element : elements) {
                final Object unwrapElement = element.unsafeUnwrap();

                if (unwrapElement instanceof Tickable) {
                    ((Tickable) unwrapElement).tick(time);
                } else if (unwrapElement instanceof Chunk) {
                    // FIXME: instance null
                    ((Chunk) unwrapElement).tick(time, null);
                }
            }
        };
    }

    private void addAcquirable(Acquirable<?> acquirable, int estimatedCost) {
        this.elements.add(acquirable);
        this.estimatedCost += estimatedCost;
    }

}
