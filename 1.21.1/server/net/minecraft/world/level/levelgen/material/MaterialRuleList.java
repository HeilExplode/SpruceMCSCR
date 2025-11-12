/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.material;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseChunk;

public record MaterialRuleList(List<NoiseChunk.BlockStateFiller> materialRuleList) implements NoiseChunk.BlockStateFiller
{
    @Override
    @Nullable
    public BlockState calculate(DensityFunction.FunctionContext functionContext) {
        for (NoiseChunk.BlockStateFiller blockStateFiller : this.materialRuleList) {
            BlockState blockState = blockStateFiller.calculate(functionContext);
            if (blockState == null) continue;
            return blockState;
        }
        return null;
    }
}

