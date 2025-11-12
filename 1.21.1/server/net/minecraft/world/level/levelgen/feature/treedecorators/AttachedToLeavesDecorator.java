/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashSet;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AttachedToLeavesDecorator
extends TreeDecorator {
    public static final MapCodec<AttachedToLeavesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(attachedToLeavesDecorator -> Float.valueOf(attachedToLeavesDecorator.probability)), (App)Codec.intRange((int)0, (int)16).fieldOf("exclusion_radius_xz").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusXZ), (App)Codec.intRange((int)0, (int)16).fieldOf("exclusion_radius_y").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.exclusionRadiusY), (App)BlockStateProvider.CODEC.fieldOf("block_provider").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.blockProvider), (App)Codec.intRange((int)1, (int)16).fieldOf("required_empty_blocks").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.requiredEmptyBlocks), (App)ExtraCodecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(attachedToLeavesDecorator -> attachedToLeavesDecorator.directions)).apply((Applicative)instance, AttachedToLeavesDecorator::new));
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesDecorator(float f, int n, int n2, BlockStateProvider blockStateProvider, int n3, List<Direction> list) {
        this.probability = f;
        this.exclusionRadiusXZ = n;
        this.exclusionRadiusY = n2;
        this.blockProvider = blockStateProvider;
        this.requiredEmptyBlocks = n3;
        this.directions = list;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        HashSet<BlockPos> hashSet = new HashSet<BlockPos>();
        RandomSource randomSource = context.random();
        for (BlockPos blockPos : Util.shuffledCopy(context.leaves(), randomSource)) {
            Direction direction;
            BlockPos blockPos2 = blockPos.relative(direction = Util.getRandom(this.directions, randomSource));
            if (hashSet.contains(blockPos2) || !(randomSource.nextFloat() < this.probability) || !this.hasRequiredEmptyBlocks(context, blockPos, direction)) continue;
            BlockPos blockPos3 = blockPos2.offset(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
            BlockPos blockPos4 = blockPos2.offset(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);
            for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
                hashSet.add(blockPos5.immutable());
            }
            context.setBlock(blockPos2, this.blockProvider.getState(randomSource, blockPos2));
        }
    }

    private boolean hasRequiredEmptyBlocks(TreeDecorator.Context context, BlockPos blockPos, Direction direction) {
        for (int i = 1; i <= this.requiredEmptyBlocks; ++i) {
            BlockPos blockPos2 = blockPos.relative(direction, i);
            if (context.isAir(blockPos2)) continue;
            return false;
        }
        return true;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}

