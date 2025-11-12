/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage;

import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.storage.WorldData;

public record LevelDataAndDimensions(WorldData worldData, WorldDimensions.Complete dimensions) {
}

