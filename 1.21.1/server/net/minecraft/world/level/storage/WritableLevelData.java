/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelData;

public interface WritableLevelData
extends LevelData {
    public void setSpawn(BlockPos var1, float var2);
}

