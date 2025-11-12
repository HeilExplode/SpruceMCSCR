/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import org.slf4j.Logger;

@FunctionalInterface
public interface TaskChainer {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static TaskChainer immediate(final Executor executor) {
        return new TaskChainer(){

            @Override
            public <T> void append(CompletableFuture<T> completableFuture, Consumer<T> consumer) {
                ((CompletableFuture)completableFuture.thenAcceptAsync((Consumer)consumer, executor)).exceptionally(throwable -> {
                    LOGGER.error("Task failed", throwable);
                    return null;
                });
            }
        };
    }

    default public void append(Runnable runnable) {
        this.append(CompletableFuture.completedFuture(null), object -> runnable.run());
    }

    public <T> void append(CompletableFuture<T> var1, Consumer<T> var2);
}

