/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

public final class BelowZeroRetrogen {
    private static final BitSet EMPTY = new BitSet(0);
    private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> LongStream.of(bitSet.toLongArray()));
    private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = BuiltInRegistries.CHUNK_STATUS.byNameCodec().comapFlatMap(chunkStatus -> chunkStatus == ChunkStatus.EMPTY ? DataResult.error(() -> "target_status cannot be empty") : DataResult.success((Object)chunkStatus), Function.identity());
    public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NON_EMPTY_CHUNK_STATUS.fieldOf("target_status").forGetter(BelowZeroRetrogen::targetStatus), (App)BITSET_CODEC.lenientOptionalFieldOf("missing_bedrock").forGetter(belowZeroRetrogen -> belowZeroRetrogen.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(belowZeroRetrogen.missingBedrock))).apply((Applicative)instance, BelowZeroRetrogen::new));
    private static final Set<ResourceKey<Biome>> RETAINED_RETROGEN_BIOMES = Set.of(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES, Biomes.DEEP_DARK);
    public static final LevelHeightAccessor UPGRADE_HEIGHT_ACCESSOR = new LevelHeightAccessor(){

        @Override
        public int getHeight() {
            return 64;
        }

        @Override
        public int getMinBuildHeight() {
            return -64;
        }
    };
    private final ChunkStatus targetStatus;
    private final BitSet missingBedrock;

    private BelowZeroRetrogen(ChunkStatus chunkStatus, Optional<BitSet> optional) {
        this.targetStatus = chunkStatus;
        this.missingBedrock = optional.orElse(EMPTY);
    }

    @Nullable
    public static BelowZeroRetrogen read(CompoundTag compoundTag) {
        ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getString("target_status"));
        if (chunkStatus == ChunkStatus.EMPTY) {
            return null;
        }
        return new BelowZeroRetrogen(chunkStatus, Optional.of(BitSet.valueOf(compoundTag.getLongArray("missing_bedrock"))));
    }

    public static void replaceOldBedrock(ProtoChunk protoChunk) {
        int n = 4;
        BlockPos.betweenClosed(0, 0, 0, 15, 4, 15).forEach(blockPos -> {
            if (protoChunk.getBlockState((BlockPos)blockPos).is(Blocks.BEDROCK)) {
                protoChunk.setBlockState((BlockPos)blockPos, Blocks.DEEPSLATE.defaultBlockState(), false);
            }
        });
    }

    public void applyBedrockMask(ProtoChunk protoChunk) {
        LevelHeightAccessor levelHeightAccessor = protoChunk.getHeightAccessorForGeneration();
        int n = levelHeightAccessor.getMinBuildHeight();
        int n2 = levelHeightAccessor.getMaxBuildHeight() - 1;
        for (int i = 0; i < 16; ++i) {
            for (int j = 0; j < 16; ++j) {
                if (!this.hasBedrockHole(i, j)) continue;
                BlockPos.betweenClosed(i, n, j, i, n2, j).forEach(blockPos -> protoChunk.setBlockState((BlockPos)blockPos, Blocks.AIR.defaultBlockState(), false));
            }
        }
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public boolean hasBedrockHoles() {
        return !this.missingBedrock.isEmpty();
    }

    public boolean hasBedrockHole(int n, int n2) {
        return this.missingBedrock.get((n2 & 0xF) * 16 + (n & 0xF));
    }

    public static BiomeResolver getBiomeResolver(BiomeResolver biomeResolver, ChunkAccess chunkAccess) {
        if (!chunkAccess.isUpgrading()) {
            return biomeResolver;
        }
        Predicate<ResourceKey> predicate = RETAINED_RETROGEN_BIOMES::contains;
        return (n, n2, n3, sampler) -> {
            Holder<Biome> holder = biomeResolver.getNoiseBiome(n, n2, n3, sampler);
            if (holder.is(predicate)) {
                return holder;
            }
            return chunkAccess.getNoiseBiome(n, 0, n3);
        };
    }
}

