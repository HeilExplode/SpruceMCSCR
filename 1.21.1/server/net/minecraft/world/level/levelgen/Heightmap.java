/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectListIterator
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.util.BitStorage;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.slf4j.Logger;

public class Heightmap {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final Predicate<BlockState> NOT_AIR = blockState -> !blockState.isAir();
    static final Predicate<BlockState> MATERIAL_MOTION_BLOCKING = BlockBehaviour.BlockStateBase::blocksMotion;
    private final BitStorage data;
    private final Predicate<BlockState> isOpaque;
    private final ChunkAccess chunk;

    public Heightmap(ChunkAccess chunkAccess, Types types) {
        this.isOpaque = types.isOpaque();
        this.chunk = chunkAccess;
        int n = Mth.ceillog2(chunkAccess.getHeight() + 1);
        this.data = new SimpleBitStorage(n, 256);
    }

    public static void primeHeightmaps(ChunkAccess chunkAccess, Set<Types> set) {
        int n = set.size();
        ObjectArrayList objectArrayList = new ObjectArrayList(n);
        ObjectListIterator objectListIterator = objectArrayList.iterator();
        int n2 = chunkAccess.getHighestSectionPosition() + 16;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 16; ++i) {
            block1: for (int j = 0; j < 16; ++j) {
                for (Types types : set) {
                    objectArrayList.add((Object)chunkAccess.getOrCreateHeightmapUnprimed(types));
                }
                for (int k = n2 - 1; k >= chunkAccess.getMinBuildHeight(); --k) {
                    mutableBlockPos.set(i, k, j);
                    BlockState blockState = chunkAccess.getBlockState(mutableBlockPos);
                    if (blockState.is(Blocks.AIR)) continue;
                    while (objectListIterator.hasNext()) {
                        Heightmap heightmap = (Heightmap)objectListIterator.next();
                        if (!heightmap.isOpaque.test(blockState)) continue;
                        heightmap.setHeight(i, j, k + 1);
                        objectListIterator.remove();
                    }
                    if (objectArrayList.isEmpty()) continue block1;
                    objectListIterator.back(n);
                }
            }
        }
    }

    public boolean update(int n, int n2, int n3, BlockState blockState) {
        int n4 = this.getFirstAvailable(n, n3);
        if (n2 <= n4 - 2) {
            return false;
        }
        if (this.isOpaque.test(blockState)) {
            if (n2 >= n4) {
                this.setHeight(n, n3, n2 + 1);
                return true;
            }
        } else if (n4 - 1 == n2) {
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            for (int i = n2 - 1; i >= this.chunk.getMinBuildHeight(); --i) {
                mutableBlockPos.set(n, i, n3);
                if (!this.isOpaque.test(this.chunk.getBlockState(mutableBlockPos))) continue;
                this.setHeight(n, n3, i + 1);
                return true;
            }
            this.setHeight(n, n3, this.chunk.getMinBuildHeight());
            return true;
        }
        return false;
    }

    public int getFirstAvailable(int n, int n2) {
        return this.getFirstAvailable(Heightmap.getIndex(n, n2));
    }

    public int getHighestTaken(int n, int n2) {
        return this.getFirstAvailable(Heightmap.getIndex(n, n2)) - 1;
    }

    private int getFirstAvailable(int n) {
        return this.data.get(n) + this.chunk.getMinBuildHeight();
    }

    private void setHeight(int n, int n2, int n3) {
        this.data.set(Heightmap.getIndex(n, n2), n3 - this.chunk.getMinBuildHeight());
    }

    public void setRawData(ChunkAccess chunkAccess, Types types, long[] lArray) {
        long[] lArray2 = this.data.getRaw();
        if (lArray2.length == lArray.length) {
            System.arraycopy(lArray, 0, lArray2, 0, lArray.length);
            return;
        }
        LOGGER.warn("Ignoring heightmap data for chunk " + String.valueOf(chunkAccess.getPos()) + ", size does not match; expected: " + lArray2.length + ", got: " + lArray.length);
        Heightmap.primeHeightmaps(chunkAccess, EnumSet.of(types));
    }

    public long[] getRawData() {
        return this.data.getRaw();
    }

    private static int getIndex(int n, int n2) {
        return n + n2 * 16;
    }

    public static enum Types implements StringRepresentable
    {
        WORLD_SURFACE_WG("WORLD_SURFACE_WG", Usage.WORLDGEN, NOT_AIR),
        WORLD_SURFACE("WORLD_SURFACE", Usage.CLIENT, NOT_AIR),
        OCEAN_FLOOR_WG("OCEAN_FLOOR_WG", Usage.WORLDGEN, MATERIAL_MOTION_BLOCKING),
        OCEAN_FLOOR("OCEAN_FLOOR", Usage.LIVE_WORLD, MATERIAL_MOTION_BLOCKING),
        MOTION_BLOCKING("MOTION_BLOCKING", Usage.CLIENT, blockState -> blockState.blocksMotion() || !blockState.getFluidState().isEmpty()),
        MOTION_BLOCKING_NO_LEAVES("MOTION_BLOCKING_NO_LEAVES", Usage.LIVE_WORLD, blockState -> (blockState.blocksMotion() || !blockState.getFluidState().isEmpty()) && !(blockState.getBlock() instanceof LeavesBlock));

        public static final Codec<Types> CODEC;
        private final String serializationKey;
        private final Usage usage;
        private final Predicate<BlockState> isOpaque;

        private Types(String string2, Usage usage, Predicate<BlockState> predicate) {
            this.serializationKey = string2;
            this.usage = usage;
            this.isOpaque = predicate;
        }

        public String getSerializationKey() {
            return this.serializationKey;
        }

        public boolean sendToClient() {
            return this.usage == Usage.CLIENT;
        }

        public boolean keepAfterWorldgen() {
            return this.usage != Usage.WORLDGEN;
        }

        public Predicate<BlockState> isOpaque() {
            return this.isOpaque;
        }

        @Override
        public String getSerializedName() {
            return this.serializationKey;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Types::values);
        }
    }

    public static enum Usage {
        WORLDGEN,
        LIVE_WORLD,
        CLIENT;

    }
}

