/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.StateTestingPredicate;

class ReplaceablePredicate
extends StateTestingPredicate {
    public static final MapCodec<ReplaceablePredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> ReplaceablePredicate.stateTestingCodec(instance).apply((Applicative)instance, ReplaceablePredicate::new));

    public ReplaceablePredicate(Vec3i vec3i) {
        super(vec3i);
    }

    @Override
    protected boolean test(BlockState blockState) {
        return blockState.canBeReplaced();
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.REPLACEABLE;
    }
}

