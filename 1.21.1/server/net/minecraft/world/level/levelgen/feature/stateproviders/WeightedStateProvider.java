/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class WeightedStateProvider
extends BlockStateProvider {
    public static final MapCodec<WeightedStateProvider> CODEC = SimpleWeightedRandomList.wrappedCodec(BlockState.CODEC).comapFlatMap(WeightedStateProvider::create, weightedStateProvider -> weightedStateProvider.weightedList).fieldOf("entries");
    private final SimpleWeightedRandomList<BlockState> weightedList;

    private static DataResult<WeightedStateProvider> create(SimpleWeightedRandomList<BlockState> simpleWeightedRandomList) {
        if (simpleWeightedRandomList.isEmpty()) {
            return DataResult.error(() -> "WeightedStateProvider with no states");
        }
        return DataResult.success((Object)new WeightedStateProvider(simpleWeightedRandomList));
    }

    public WeightedStateProvider(SimpleWeightedRandomList<BlockState> simpleWeightedRandomList) {
        this.weightedList = simpleWeightedRandomList;
    }

    public WeightedStateProvider(SimpleWeightedRandomList.Builder<BlockState> builder) {
        this(builder.build());
    }

    @Override
    protected BlockStateProviderType<?> type() {
        return BlockStateProviderType.WEIGHTED_STATE_PROVIDER;
    }

    @Override
    public BlockState getState(RandomSource randomSource, BlockPos blockPos) {
        return this.weightedList.getRandomValue(randomSource).orElseThrow(IllegalStateException::new);
    }
}

