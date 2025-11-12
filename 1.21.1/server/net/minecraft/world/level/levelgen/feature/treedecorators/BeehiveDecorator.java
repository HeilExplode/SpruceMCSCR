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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class BeehiveDecorator
extends TreeDecorator {
    public static final MapCodec<BeehiveDecorator> CODEC = Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").xmap(BeehiveDecorator::new, beehiveDecorator -> Float.valueOf(beehiveDecorator.probability));
    private static final Direction WORLDGEN_FACING = Direction.SOUTH;
    private static final Direction[] SPAWN_DIRECTIONS = (Direction[])Direction.Plane.HORIZONTAL.stream().filter(direction -> direction != WORLDGEN_FACING.getOpposite()).toArray(Direction[]::new);
    private final float probability;

    public BeehiveDecorator(float f) {
        this.probability = f;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.BEEHIVE;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        RandomSource randomSource = context.random();
        if (randomSource.nextFloat() >= this.probability) {
            return;
        }
        ObjectArrayList<BlockPos> objectArrayList = context.leaves();
        ObjectArrayList<BlockPos> objectArrayList2 = context.logs();
        int n = !objectArrayList.isEmpty() ? Math.max(((BlockPos)objectArrayList.get(0)).getY() - 1, ((BlockPos)objectArrayList2.get(0)).getY() + 1) : Math.min(((BlockPos)objectArrayList2.get(0)).getY() + 1 + randomSource.nextInt(3), ((BlockPos)objectArrayList2.get(objectArrayList2.size() - 1)).getY());
        List list = objectArrayList2.stream().filter(blockPos -> blockPos.getY() == n).flatMap(blockPos -> Stream.of(SPAWN_DIRECTIONS).map(blockPos::relative)).collect(Collectors.toList());
        if (list.isEmpty()) {
            return;
        }
        Collections.shuffle(list);
        Optional<BlockPos> optional = list.stream().filter(blockPos -> context.isAir((BlockPos)blockPos) && context.isAir(blockPos.relative(WORLDGEN_FACING))).findFirst();
        if (optional.isEmpty()) {
            return;
        }
        context.setBlock(optional.get(), (BlockState)Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, WORLDGEN_FACING));
        context.level().getBlockEntity(optional.get(), BlockEntityType.BEEHIVE).ifPresent(beehiveBlockEntity -> {
            int n = 2 + randomSource.nextInt(2);
            for (int i = 0; i < n; ++i) {
                beehiveBlockEntity.storeBee(BeehiveBlockEntity.Occupant.create(randomSource.nextInt(599)));
            }
        });
    }
}

