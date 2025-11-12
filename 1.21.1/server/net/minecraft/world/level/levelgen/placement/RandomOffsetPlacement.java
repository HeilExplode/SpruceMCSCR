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
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

public class RandomOffsetPlacement
extends PlacementModifier {
    public static final MapCodec<RandomOffsetPlacement> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)IntProvider.codec(-16, 16).fieldOf("xz_spread").forGetter(randomOffsetPlacement -> randomOffsetPlacement.xzSpread), (App)IntProvider.codec(-16, 16).fieldOf("y_spread").forGetter(randomOffsetPlacement -> randomOffsetPlacement.ySpread)).apply((Applicative)instance, RandomOffsetPlacement::new));
    private final IntProvider xzSpread;
    private final IntProvider ySpread;

    public static RandomOffsetPlacement of(IntProvider intProvider, IntProvider intProvider2) {
        return new RandomOffsetPlacement(intProvider, intProvider2);
    }

    public static RandomOffsetPlacement vertical(IntProvider intProvider) {
        return new RandomOffsetPlacement(ConstantInt.of(0), intProvider);
    }

    public static RandomOffsetPlacement horizontal(IntProvider intProvider) {
        return new RandomOffsetPlacement(intProvider, ConstantInt.of(0));
    }

    private RandomOffsetPlacement(IntProvider intProvider, IntProvider intProvider2) {
        this.xzSpread = intProvider;
        this.ySpread = intProvider2;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext placementContext, RandomSource randomSource, BlockPos blockPos) {
        int n = blockPos.getX() + this.xzSpread.sample(randomSource);
        int n2 = blockPos.getY() + this.ySpread.sample(randomSource);
        int n3 = blockPos.getZ() + this.xzSpread.sample(randomSource);
        return Stream.of(new BlockPos(n, n2, n3));
    }

    @Override
    public PlacementModifierType<?> type() {
        return PlacementModifierType.RANDOM_OFFSET;
    }
}

