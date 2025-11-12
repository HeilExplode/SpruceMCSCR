/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ServerLevelAccessor;

public interface WorldGenLevel
extends ServerLevelAccessor {
    public long getSeed();

    default public boolean ensureCanWrite(BlockPos blockPos) {
        return true;
    }

    default public void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
    }
}

