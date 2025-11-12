/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MultifaceGrowthConfiguration
implements FeatureConfiguration {
    public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success).orElse((Object)((MultifaceBlock)Blocks.GLOW_LICHEN)).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.placeBlock), (App)Codec.intRange((int)1, (int)64).fieldOf("search_range").orElse((Object)10).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.searchRange), (App)Codec.BOOL.fieldOf("can_place_on_floor").orElse((Object)false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnFloor), (App)Codec.BOOL.fieldOf("can_place_on_ceiling").orElse((Object)false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnCeiling), (App)Codec.BOOL.fieldOf("can_place_on_wall").orElse((Object)false).forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canPlaceOnWall), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("chance_of_spreading").orElse((Object)Float.valueOf(0.5f)).forGetter(multifaceGrowthConfiguration -> Float.valueOf(multifaceGrowthConfiguration.chanceOfSpreading)), (App)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on").forGetter(multifaceGrowthConfiguration -> multifaceGrowthConfiguration.canBePlacedOn)).apply((Applicative)instance, MultifaceGrowthConfiguration::new));
    public final MultifaceBlock placeBlock;
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    private final ObjectArrayList<Direction> validDirections;

    private static DataResult<MultifaceBlock> apply(Block block) {
        DataResult dataResult;
        if (block instanceof MultifaceBlock) {
            MultifaceBlock multifaceBlock = (MultifaceBlock)block;
            dataResult = DataResult.success((Object)multifaceBlock);
        } else {
            dataResult = DataResult.error(() -> "Growth block should be a multiface block");
        }
        return dataResult;
    }

    public MultifaceGrowthConfiguration(MultifaceBlock multifaceBlock, int n, boolean bl, boolean bl2, boolean bl3, float f, HolderSet<Block> holderSet) {
        this.placeBlock = multifaceBlock;
        this.searchRange = n;
        this.canPlaceOnFloor = bl;
        this.canPlaceOnCeiling = bl2;
        this.canPlaceOnWall = bl3;
        this.chanceOfSpreading = f;
        this.canBePlacedOn = holderSet;
        this.validDirections = new ObjectArrayList(6);
        if (bl2) {
            this.validDirections.add((Object)Direction.UP);
        }
        if (bl) {
            this.validDirections.add((Object)Direction.DOWN);
        }
        if (bl3) {
            Direction.Plane.HORIZONTAL.forEach(arg_0 -> this.validDirections.add(arg_0));
        }
    }

    public List<Direction> getShuffledDirectionsExcept(RandomSource randomSource, Direction direction) {
        return Util.toShuffledList(this.validDirections.stream().filter(direction2 -> direction2 != direction), randomSource);
    }

    public List<Direction> getShuffledDirections(RandomSource randomSource) {
        return Util.shuffledCopy(this.validDirections, randomSource);
    }
}

