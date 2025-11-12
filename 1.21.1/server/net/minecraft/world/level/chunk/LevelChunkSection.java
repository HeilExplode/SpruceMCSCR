/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.chunk;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.PalettedContainerRO;
import net.minecraft.world.level.material.FluidState;

public class LevelChunkSection {
    public static final int SECTION_WIDTH = 16;
    public static final int SECTION_HEIGHT = 16;
    public static final int SECTION_SIZE = 4096;
    public static final int BIOME_CONTAINER_BITS = 2;
    private short nonEmptyBlockCount;
    private short tickingBlockCount;
    private short tickingFluidCount;
    private final PalettedContainer<BlockState> states;
    private PalettedContainerRO<Holder<Biome>> biomes;

    public LevelChunkSection(PalettedContainer<BlockState> palettedContainer, PalettedContainerRO<Holder<Biome>> palettedContainerRO) {
        this.states = palettedContainer;
        this.biomes = palettedContainerRO;
        this.recalcBlockCounts();
    }

    public LevelChunkSection(Registry<Biome> registry) {
        this.states = new PalettedContainer<BlockState>(Block.BLOCK_STATE_REGISTRY, Blocks.AIR.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        this.biomes = new PalettedContainer<Holder.Reference<Biome>>(registry.asHolderIdMap(), registry.getHolderOrThrow(Biomes.PLAINS), PalettedContainer.Strategy.SECTION_BIOMES);
    }

    public BlockState getBlockState(int n, int n2, int n3) {
        return this.states.get(n, n2, n3);
    }

    public FluidState getFluidState(int n, int n2, int n3) {
        return this.states.get(n, n2, n3).getFluidState();
    }

    public void acquire() {
        this.states.acquire();
    }

    public void release() {
        this.states.release();
    }

    public BlockState setBlockState(int n, int n2, int n3, BlockState blockState) {
        return this.setBlockState(n, n2, n3, blockState, true);
    }

    public BlockState setBlockState(int n, int n2, int n3, BlockState blockState, boolean bl) {
        BlockState blockState2 = bl ? this.states.getAndSet(n, n2, n3, blockState) : this.states.getAndSetUnchecked(n, n2, n3, blockState);
        FluidState fluidState = blockState2.getFluidState();
        FluidState fluidState2 = blockState.getFluidState();
        if (!blockState2.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount - 1);
            if (blockState2.isRandomlyTicking()) {
                this.tickingBlockCount = (short)(this.tickingBlockCount - 1);
            }
        }
        if (!fluidState.isEmpty()) {
            this.tickingFluidCount = (short)(this.tickingFluidCount - 1);
        }
        if (!blockState.isAir()) {
            this.nonEmptyBlockCount = (short)(this.nonEmptyBlockCount + 1);
            if (blockState.isRandomlyTicking()) {
                this.tickingBlockCount = (short)(this.tickingBlockCount + 1);
            }
        }
        if (!fluidState2.isEmpty()) {
            this.tickingFluidCount = (short)(this.tickingFluidCount + 1);
        }
        return blockState2;
    }

    public boolean hasOnlyAir() {
        return this.nonEmptyBlockCount == 0;
    }

    public boolean isRandomlyTicking() {
        return this.isRandomlyTickingBlocks() || this.isRandomlyTickingFluids();
    }

    public boolean isRandomlyTickingBlocks() {
        return this.tickingBlockCount > 0;
    }

    public boolean isRandomlyTickingFluids() {
        return this.tickingFluidCount > 0;
    }

    public void recalcBlockCounts() {
        class BlockCounter
        implements PalettedContainer.CountConsumer<BlockState> {
            public int nonEmptyBlockCount;
            public int tickingBlockCount;
            public int tickingFluidCount;

            BlockCounter(LevelChunkSection levelChunkSection) {
            }

            @Override
            public void accept(BlockState blockState, int n) {
                FluidState fluidState = blockState.getFluidState();
                if (!blockState.isAir()) {
                    this.nonEmptyBlockCount += n;
                    if (blockState.isRandomlyTicking()) {
                        this.tickingBlockCount += n;
                    }
                }
                if (!fluidState.isEmpty()) {
                    this.nonEmptyBlockCount += n;
                    if (fluidState.isRandomlyTicking()) {
                        this.tickingFluidCount += n;
                    }
                }
            }

            @Override
            public /* synthetic */ void accept(Object object, int n) {
                this.accept((BlockState)object, n);
            }
        }
        BlockCounter blockCounter = new BlockCounter(this);
        this.states.count(blockCounter);
        this.nonEmptyBlockCount = (short)blockCounter.nonEmptyBlockCount;
        this.tickingBlockCount = (short)blockCounter.tickingBlockCount;
        this.tickingFluidCount = (short)blockCounter.tickingFluidCount;
    }

    public PalettedContainer<BlockState> getStates() {
        return this.states;
    }

    public PalettedContainerRO<Holder<Biome>> getBiomes() {
        return this.biomes;
    }

    public void read(FriendlyByteBuf friendlyByteBuf) {
        this.nonEmptyBlockCount = friendlyByteBuf.readShort();
        this.states.read(friendlyByteBuf);
        PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
        palettedContainer.read(friendlyByteBuf);
        this.biomes = palettedContainer;
    }

    public void readBiomes(FriendlyByteBuf friendlyByteBuf) {
        PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
        palettedContainer.read(friendlyByteBuf);
        this.biomes = palettedContainer;
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeShort(this.nonEmptyBlockCount);
        this.states.write(friendlyByteBuf);
        this.biomes.write(friendlyByteBuf);
    }

    public int getSerializedSize() {
        return 2 + this.states.getSerializedSize() + this.biomes.getSerializedSize();
    }

    public boolean maybeHas(Predicate<BlockState> predicate) {
        return this.states.maybeHas(predicate);
    }

    public Holder<Biome> getNoiseBiome(int n, int n2, int n3) {
        return this.biomes.get(n, n2, n3);
    }

    public void fillBiomesFromNoise(BiomeResolver biomeResolver, Climate.Sampler sampler, int n, int n2, int n3) {
        PalettedContainer<Holder<Biome>> palettedContainer = this.biomes.recreate();
        int n4 = 4;
        for (int i = 0; i < 4; ++i) {
            for (int j = 0; j < 4; ++j) {
                for (int k = 0; k < 4; ++k) {
                    palettedContainer.getAndSetUnchecked(i, j, k, biomeResolver.getNoiseBiome(n + i, n2 + j, n3 + k, sampler));
                }
            }
        }
        this.biomes = palettedContainer;
    }
}

