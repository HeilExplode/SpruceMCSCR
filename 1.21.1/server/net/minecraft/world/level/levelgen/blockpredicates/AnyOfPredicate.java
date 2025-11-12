/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.blockpredicates;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicateType;
import net.minecraft.world.level.levelgen.blockpredicates.CombiningPredicate;

class AnyOfPredicate
extends CombiningPredicate {
    public static final MapCodec<AnyOfPredicate> CODEC = AnyOfPredicate.codec(AnyOfPredicate::new);

    public AnyOfPredicate(List<BlockPredicate> list) {
        super(list);
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        for (BlockPredicate blockPredicate : this.predicates) {
            if (!blockPredicate.test(worldGenLevel, blockPos)) continue;
            return true;
        }
        return false;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.ANY_OF;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}

