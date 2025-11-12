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
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class BastionHousingUnitsPools {
    public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
        HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
        Holder.Reference<StructureProcessorList> reference = holderGetter.getOrThrow(ProcessorLists.HOUSING);
        HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> reference2 = holderGetter2.getOrThrow(Pools.EMPTY);
        Pools.register(bootstrapContext, "bastion/units/center_pieces", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/center_pieces/center_2", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/pathways", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/pathways/pathway_wall_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/walls/wall_bases", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/walls/wall_base", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/walls/connected_wall", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/stages/stage_0", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_2", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_0_3", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/stages/stage_1", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_2", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_1_3", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/stages/rot/stage_1", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/stages/rot/stage_1_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/stages/stage_2", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_2_1", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/stages/stage_3", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_2", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/stages/stage_3_3", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/fillers/stage_0", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/fillers/stage_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/edges", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/edges/edge_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/wall_units", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/wall_units/unit_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/edge_wall_units", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/wall_units/edge_0_large", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/ramparts", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_2", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/large_ramparts", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/ramparts/ramparts_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        Pools.register(bootstrapContext, "bastion/units/rampart_plates", new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("bastion/units/rampart_plates/plate_0", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
    }
}

