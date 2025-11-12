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
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class FixedPlacement
extends PlacementModifier {
    public static final MapCodec<FixedPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockPos.CODEC.listOf().fieldOf("positions").forGetter(fixedPlacement -> fixedPlacement.positions)).apply((Applicative)instance, FixedPlacement::new));
    private final List<BlockPos> positions;

    public static FixedPlacement of(BlockPos ... blockPosArray) {
        return new FixedPlacement(List.of(blockPosArray));
    }

    private FixedPlacement(List<BlockPos> list) {
        this.positions = list;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos2) {
        int n = SectionPos.blockToSectionCoord(blockPos2.getX());
        int n2 = SectionPos.blockToSectionCoord(blockPos2.getZ());
        boolean bl = false;
        for (BlockPos blockPos3 : this.positions) {
            if (!FixedPlacement.isSameChunk(n, n2, blockPos3)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return Stream.empty();
        }
        return this.positions.stream().filter(blockPos -> FixedPlacement.isSameChunk(n, n2, blockPos));
    }

    private static boolean isSameChunk(int n, int n2, BlockPos blockPos) {
        return n == SectionPos.blockToSectionCoord(blockPos.getX()) && n2 == SectionPos.blockToSectionCoord(blockPos.getZ());
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.FIXED_PLACEMENT;
    }
}

