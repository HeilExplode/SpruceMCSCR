/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.AncientCityStructurePieces;
import net.minecraft.data.worldgen.BastionPieces;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.PillagerOutpostPools;
import net.minecraft.data.worldgen.TrailRuinsStructurePools;
import net.minecraft.data.worldgen.TrialChambersStructurePools;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools {
    public static final ResourceKey<StructureTemplatePool> EMPTY = Pools.createKey("empty");

    public static ResourceKey<StructureTemplatePool> createKey(String string) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.withDefaultNamespace(string));
    }

    public static ResourceKey<StructureTemplatePool> parseKey(String string) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse(string));
    }

    public static void register(BootstrapContext<StructureTemplatePool> bootstrapContext, String string, StructureTemplatePool structureTemplatePool) {
        bootstrapContext.register(Pools.createKey(string), structureTemplatePool);
    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
        HolderGetter<StructureTemplatePool> holderGetter = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> reference = holderGetter.getOrThrow(EMPTY);
        bootstrapContext.register(EMPTY, new StructureTemplatePool(reference, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of(), StructureTemplatePool.Projection.RIGID));
        BastionPieces.bootstrap(bootstrapContext);
        PillagerOutpostPools.bootstrap(bootstrapContext);
        VillagePools.bootstrap(bootstrapContext);
        AncientCityStructurePieces.bootstrap(bootstrapContext);
        TrailRuinsStructurePools.bootstrap(bootstrapContext);
        TrialChambersStructurePools.bootstrap(bootstrapContext);
    }
}

