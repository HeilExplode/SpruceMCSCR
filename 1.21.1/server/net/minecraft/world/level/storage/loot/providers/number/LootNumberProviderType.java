/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.storage.loot.providers.number;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

public record LootNumberProviderType(MapCodec<? extends NumberProvider> codec) {
}

