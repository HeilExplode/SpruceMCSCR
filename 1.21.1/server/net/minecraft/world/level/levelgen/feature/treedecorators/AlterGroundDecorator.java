/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class AlterGroundDecorator
extends TreeDecorator {
    public static final MapCodec<AlterGroundDecorator> CODEC = BlockStateProvider.CODEC.fieldOf("provider").xmap(AlterGroundDecorator::new, alterGroundDecorator -> alterGroundDecorator.provider);
    private final BlockStateProvider provider;

    public AlterGroundDecorator(BlockStateProvider blockStateProvider) {
        this.provider = blockStateProvider;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return TreeDecoratorType.ALTER_GROUND;
    }

    @Override
    public void place(TreeDecorator.Context context) {
        ArrayList arrayList = Lists.newArrayList();
        ObjectArrayList<BlockPos> objectArrayList = context.roots();
        ObjectArrayList<BlockPos> objectArrayList2 = context.logs();
        if (objectArrayList.isEmpty()) {
            arrayList.addAll(objectArrayList2);
        } else if (!objectArrayList2.isEmpty() && ((BlockPos)objectArrayList.get(0)).getY() == ((BlockPos)objectArrayList2.get(0)).getY()) {
            arrayList.addAll(objectArrayList2);
            arrayList.addAll(objectArrayList);
        } else {
            arrayList.addAll(objectArrayList);
        }
        if (arrayList.isEmpty()) {
            return;
        }
        int n = ((BlockPos)arrayList.get(0)).getY();
        arrayList.stream().filter(blockPos -> blockPos.getY() == n).forEach(blockPos -> {
            this.placeCircle(context, blockPos.west().north());
            this.placeCircle(context, blockPos.east(2).north());
            this.placeCircle(context, blockPos.west().south(2));
            this.placeCircle(context, blockPos.east(2).south(2));
            for (int i = 0; i < 5; ++i) {
                int n = context.random().nextInt(64);
                int n2 = n % 8;
                int n3 = n / 8;
                if (n2 != 0 && n2 != 7 && n3 != 0 && n3 != 7) continue;
                this.placeCircle(context, blockPos.offset(-3 + n2, 0, -3 + n3));
            }
        });
    }

    private void placeCircle(TreeDecorator.Context context, BlockPos blockPos) {
        for (int i = -2; i <= 2; ++i) {
            for (int j = -2; j <= 2; ++j) {
                if (Math.abs(i) == 2 && Math.abs(j) == 2) continue;
                this.placeBlockAt(context, blockPos.offset(i, 0, j));
            }
        }
    }

    private void placeBlockAt(TreeDecorator.Context context, BlockPos blockPos) {
        for (int i = 2; i >= -3; --i) {
            BlockPos blockPos2 = blockPos.above(i);
            if (Feature.isGrassOrDirt(context.level(), blockPos2)) {
                context.setBlock(blockPos2, this.provider.getState(context.random(), blockPos));
                break;
            }
            if (!context.isAir(blockPos2) && i < 0) break;
        }
    }
}

