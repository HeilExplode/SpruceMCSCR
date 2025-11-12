/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasBinding;

@FunctionalInterface
public interface PoolAliasLookup {
    public static final PoolAliasLookup EMPTY = resourceKey -> resourceKey;

    public ResourceKey<StructureTemplatePool> lookup(ResourceKey<StructureTemplatePool> var1);

    public static PoolAliasLookup create(List<PoolAliasBinding> list, BlockPos blockPos, long l) {
        if (list.isEmpty()) {
            return EMPTY;
        }
        RandomSource randomSource = RandomSource.create(l).forkPositional().at(blockPos);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        list.forEach(poolAliasBinding -> poolAliasBinding.forEachResolved(randomSource, (arg_0, arg_1) -> ((ImmutableMap.Builder)builder).put(arg_0, arg_1)));
        ImmutableMap immutableMap = builder.build();
        return arg_0 -> PoolAliasLookup.lambda$create$3((Map)immutableMap, arg_0);
    }

    private static /* synthetic */ ResourceKey lambda$create$3(Map map, ResourceKey resourceKey) {
        return Objects.requireNonNull(map.getOrDefault(resourceKey, resourceKey), () -> "alias " + String.valueOf(resourceKey) + " was mapped to null value");
    }
}

