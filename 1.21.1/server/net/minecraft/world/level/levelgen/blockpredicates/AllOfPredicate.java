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

class AllOfPredicate
extends CombiningPredicate {
    public static final MapCodec<AllOfPredicate> CODEC = AllOfPredicate.codec(AllOfPredicate::new);

    public AllOfPredicate(List<BlockPredicate> list) {
        super(list);
    }

    @Override
    public boolean test(WorldGenLevel worldGenLevel, BlockPos blockPos) {
        for (BlockPredicate blockPredicate : this.predicates) {
            if (blockPredicate.test(worldGenLevel, blockPos)) continue;
            return false;
        }
        return true;
    }

    @Override
    public BlockPredicateType<?> type() {
        return BlockPredicateType.ALL_OF;
    }

    @Override
    public /* synthetic */ boolean test(Object object, Object object2) {
        return this.test((WorldGenLevel)object, (BlockPos)object2);
    }
}

