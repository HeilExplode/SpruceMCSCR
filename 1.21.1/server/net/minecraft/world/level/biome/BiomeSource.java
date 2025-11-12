/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.biome;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Climate;

public abstract class BiomeSource
implements BiomeResolver {
    public static final Codec<BiomeSource> CODEC = BuiltInRegistries.BIOME_SOURCE.byNameCodec().dispatchStable(BiomeSource::codec, Function.identity());
    private final Supplier<Set<Holder<Biome>>> possibleBiomes = Suppliers.memoize(() -> (Set)this.collectPossibleBiomes().distinct().collect(ImmutableSet.toImmutableSet()));

    protected BiomeSource() {
    }

    protected abstract MapCodec<? extends BiomeSource> codec();

    protected abstract Stream<Holder<Biome>> collectPossibleBiomes();

    public Set<Holder<Biome>> possibleBiomes() {
        return this.possibleBiomes.get();
    }

    public Set<Holder<Biome>> getBiomesWithin(int n, int n2, int n3, int n4, Climate.Sampler sampler) {
        int n5 = QuartPos.fromBlock(n - n4);
        int n6 = QuartPos.fromBlock(n2 - n4);
        int n7 = QuartPos.fromBlock(n3 - n4);
        int n8 = QuartPos.fromBlock(n + n4);
        int n9 = QuartPos.fromBlock(n2 + n4);
        int n10 = QuartPos.fromBlock(n3 + n4);
        int n11 = n8 - n5 + 1;
        int n12 = n9 - n6 + 1;
        int n13 = n10 - n7 + 1;
        HashSet hashSet = Sets.newHashSet();
        for (int i = 0; i < n13; ++i) {
            for (int j = 0; j < n11; ++j) {
                for (int k = 0; k < n12; ++k) {
                    int n14 = n5 + j;
                    int n15 = n6 + k;
                    int n16 = n7 + i;
                    hashSet.add(this.getNoiseBiome(n14, n15, n16, sampler));
                }
            }
        }
        return hashSet;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int n, int n2, int n3, int n4, Predicate<Holder<Biome>> predicate, RandomSource randomSource, Climate.Sampler sampler) {
        return this.findBiomeHorizontal(n, n2, n3, n4, 1, predicate, randomSource, false, sampler);
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findClosestBiome3d(BlockPos blockPos, int n, int n2, int n3, Predicate<Holder<Biome>> predicate, Climate.Sampler sampler, LevelReader levelReader) {
        Set set = this.possibleBiomes().stream().filter(predicate).collect(Collectors.toUnmodifiableSet());
        if (set.isEmpty()) {
            return null;
        }
        int n4 = Math.floorDiv(n, n2);
        int[] nArray = Mth.outFromOrigin(blockPos.getY(), levelReader.getMinBuildHeight() + 1, levelReader.getMaxBuildHeight(), n3).toArray();
        for (BlockPos.MutableBlockPos mutableBlockPos : BlockPos.spiralAround(BlockPos.ZERO, n4, Direction.EAST, Direction.SOUTH)) {
            int n5 = blockPos.getX() + mutableBlockPos.getX() * n2;
            int n6 = blockPos.getZ() + mutableBlockPos.getZ() * n2;
            int n7 = QuartPos.fromBlock(n5);
            int n8 = QuartPos.fromBlock(n6);
            for (int n9 : nArray) {
                int n10 = QuartPos.fromBlock(n9);
                Holder<Biome> holder = this.getNoiseBiome(n7, n10, n8, sampler);
                if (!set.contains(holder)) continue;
                return Pair.of((Object)new BlockPos(n5, n9, n6), holder);
            }
        }
        return null;
    }

    @Nullable
    public Pair<BlockPos, Holder<Biome>> findBiomeHorizontal(int n, int n2, int n3, int n4, int n5, Predicate<Holder<Biome>> predicate, RandomSource randomSource, boolean bl, Climate.Sampler sampler) {
        int n6;
        int n7 = QuartPos.fromBlock(n);
        int n8 = QuartPos.fromBlock(n3);
        int n9 = QuartPos.fromBlock(n4);
        int n10 = QuartPos.fromBlock(n2);
        Pair pair = null;
        int n11 = 0;
        for (int i = n6 = bl ? 0 : n9; i <= n9; i += n5) {
            int n12;
            int n13 = n12 = SharedConstants.debugGenerateSquareTerrainWithoutNoise ? 0 : -i;
            while (n12 <= i) {
                boolean bl2 = Math.abs(n12) == i;
                for (int j = -i; j <= i; j += n5) {
                    int n14;
                    Holder<Biome> holder;
                    int n15;
                    if (bl) {
                        int n16 = n15 = Math.abs(j) == i ? 1 : 0;
                        if (n15 == 0 && !bl2) continue;
                    }
                    if (!predicate.test(holder = this.getNoiseBiome(n15 = n7 + j, n10, n14 = n8 + n12, sampler))) continue;
                    if (pair == null || randomSource.nextInt(n11 + 1) == 0) {
                        BlockPos blockPos = new BlockPos(QuartPos.toBlock(n15), n2, QuartPos.toBlock(n14));
                        if (bl) {
                            return Pair.of((Object)blockPos, holder);
                        }
                        pair = Pair.of((Object)blockPos, holder);
                    }
                    ++n11;
                }
                n12 += n5;
            }
        }
        return pair;
    }

    @Override
    public abstract Holder<Biome> getNoiseBiome(int var1, int var2, int var3, Climate.Sampler var4);

    public void addDebugInfo(List<String> list, BlockPos blockPos, Climate.Sampler sampler) {
    }
}

