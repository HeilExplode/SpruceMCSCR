/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record SeededContainerLoot(ResourceKey<LootTable> lootTable, long seed) {
    public static final Codec<SeededContainerLoot> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ResourceKey.codec(Registries.LOOT_TABLE).fieldOf("loot_table").forGetter(SeededContainerLoot::lootTable), (App)Codec.LONG.optionalFieldOf("seed", (Object)0L).forGetter(SeededContainerLoot::seed)).apply((Applicative)instance, SeededContainerLoot::new));
}

