package net.minestom.server.extras;

import io.netty.util.ResourceLeakDetector;
import net.minestom.server.MinecraftServer;

public class Optimizations {

    /**
     * Some of these optimizations are UNSAFE!!!
     * This should only be used if the best performance possible is needed!
     *
     * Using this will disable netty errors and disable netty protections for
     * memory leaks, accessing a released buffer, and out of bounds writes
     *
     * ONLY USE IF YOU UNDERSTAND THE CONSEQUENCES
     *
     * This should be the first method you call in your main method
     */
    public static void UNSAFE_enableAllOptimizations() {
        System.setProperty("io.netty.tryReflectionSetAccessible", "true");
        System.setProperty("io.netty.buffer.checkAccessible", "false");
        System.setProperty("io.netty.buffer.checkBounds", "false");
        MinecraftServer.setShouldProcessNettyErrors(false);
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
    }
}
