/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.pools.alias;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.Direct;
import net.minecraft.world.level.levelgen.structure.pools.alias.Random;
import net.minecraft.world.level.levelgen.structure.pools.alias.RandomGroup;

public interface PoolAliasBinding {
    public static final Codec<PoolAliasBinding> CODEC = BuiltInRegistries.POOL_ALIAS_BINDING_TYPE.byNameCodec().dispatch(PoolAliasBinding::codec, Function.identity());

    public void forEachResolved(RandomSource var1, BiConsumer<ResourceKey<StructureTemplatePool>, ResourceKey<StructureTemplatePool>> var2);

    public Stream<ResourceKey<StructureTemplatePool>> allTargets();

    public static Direct direct(String string, String string2) {
        return PoolAliasBinding.direct(Pools.createKey(string), Pools.createKey(string2));
    }

    public static Direct direct(ResourceKey<StructureTemplatePool> resourceKey, ResourceKey<StructureTemplatePool> resourceKey2) {
        return new Direct(resourceKey, resourceKey2);
    }

    public static Random random(String string, SimpleWeightedRandomList<String> simpleWeightedRandomList) {
        SimpleWeightedRandomList.Builder builder = SimpleWeightedRandomList.builder();
        simpleWeightedRandomList.unwrap().forEach(wrapper -> builder.add(Pools.createKey((String)wrapper.data()), wrapper.getWeight().asInt()));
        return PoolAliasBinding.random(Pools.createKey(string), builder.build());
    }

    public static Random random(ResourceKey<StructureTemplatePool> resourceKey, SimpleWeightedRandomList<ResourceKey<StructureTemplatePool>> simpleWeightedRandomList) {
        return new Random(resourceKey, simpleWeightedRandomList);
    }

    public static RandomGroup randomGroup(SimpleWeightedRandomList<List<PoolAliasBinding>> simpleWeightedRandomList) {
        return new RandomGroup(simpleWeightedRandomList);
    }

    public MapCodec<? extends PoolAliasBinding> codec();
}

