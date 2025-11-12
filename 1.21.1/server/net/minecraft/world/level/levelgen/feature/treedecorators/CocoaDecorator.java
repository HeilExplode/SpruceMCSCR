/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class CocoaDecorator
extends TreeDecorator {
    public static final MapCodec<CocoaDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(CocoaDecorator::new, cocoaDecorator -> Float.valueOf(cocoaDecorator.probability));
    private final float probability;

    public CocoaDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.COCOA;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        if (randomSource.nextFloat() >= this.probability) {
            return;
        }
        ObjectArrayList<BlockPos> objectArrayList = context.logs();
        int n = ((BlockPos)objectArrayList.get(0)).getY();
        objectArrayList.stream().filter(blockPos -> blockPos.getY() - n <= 2).forEach(blockPos -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                Direction direction2;
                BlockPos blockPos2;
                if (!(randomSource.nextFloat() <= 0.25f) || !context.isAir(blockPos2 = blockPos.offset((direction2 = direction.getOpposite()).getStepX(), 0, direction2.getStepZ()))) continue;
                context.setBlock(blockPos2, (BlockState)((BlockState)Blocks.COCOA.defaultBlockState().setValue(CocoaBlock.AGE, randomSource.nextInt(3))).setValue(CocoaBlock.FACING, direction));
            }
        });
    }
}

