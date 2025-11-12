/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.placement;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class BlockPredicateFilter
extends PlacementFilter {
    public static final MapCodec<BlockPredicateFilter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockPredicate.CODEC.fieldOf("predicate").forGetter(blockPredicateFilter -> blockPredicateFilter.predicate)).apply((Applicative)instance, BlockPredicateFilter::new));
    private final BlockPredicate predicate;

    private BlockPredicateFilter(BlockPredicate blockPredicate) {
        this.predicate = blockPredicate;
    }

    public static BlockPredicateFilter forPredicate(BlockPredicate blockPredicate) {
        return new BlockPredicateFilter(blockPredicate);
    }

    @Override
    protected boolean shouldPlace(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        return this.predicate.test(placementContext.getLevel(), blockPos);
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.BLOCK_PREDICATE_FILTER;
    }
}

