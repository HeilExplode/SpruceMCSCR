/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public abstract class StructurePiece {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
    protected BoundingBox boundingBox;
    @Nullable
    private Direction orientation;
    private Mirror mirror;
    private Rotation rotation;
    protected int genDepth;
    private final StructurePieceType type;
    private static final Set<Block> SHAPE_CHECK_BLOCKS = ImmutableSet.builder().add((Object)Blocks.NETHER_BRICK_FENCE).add((Object)Blocks.TORCH).add((Object)Blocks.WALL_TORCH).add((Object)Blocks.OAK_FENCE).add((Object)Blocks.SPRUCE_FENCE).add((Object)Blocks.DARK_OAK_FENCE).add((Object)Blocks.ACACIA_FENCE).add((Object)Blocks.BIRCH_FENCE).add((Object)Blocks.JUNGLE_FENCE).add((Object)Blocks.LADDER).add((Object)Blocks.IRON_BARS).build();

    protected StructurePiece(StructurePieceType structurePieceType, int n, BoundingBox boundingBox) {
        this.type = structurePieceType;
        this.genDepth = n;
        this.boundingBox = boundingBox;
    }

    public StructurePiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
        this(structurePieceType, compoundTag.getInt("GD"), (BoundingBox)BoundingBox.CODEC.parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.get("BB")).getOrThrow(string -> new IllegalArgumentException("Invalid boundingbox: " + string)));
        int n = compoundTag.getInt("O");
        this.setOrientation(n == -1 ? null : Direction.from2DDataValue(n));
    }

    protected static BoundingBox makeBoundingBox(int n, int n2, int n3, Direction direction, int n4, int n5, int n6) {
        if (direction.getAxis() == Direction.Axis.Z) {
            return new BoundingBox(n, n2, n3, n + n4 - 1, n2 + n5 - 1, n3 + n6 - 1);
        }
        return new BoundingBox(n, n2, n3, n + n6 - 1, n2 + n5 - 1, n3 + n4 - 1);
    }

    protected static Direction getRandomHorizontalDirection(RandomSource randomSource) {
        return Direction.Plane.HORIZONTAL.getRandomDirection(randomSource);
    }

    public final CompoundTag createTag(StructurePieceSerializationContext structurePieceSerializationContext) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("id", BuiltInRegistries.STRUCTURE_PIECE.getKey(this.getType()).toString());
        BoundingBox.CODEC.encodeStart((DynamicOps)NbtOps.INSTANCE, (Object)this.boundingBox).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("BB", (Tag)tag));
        Direction direction = this.getOrientation();
        compoundTag.putInt("O", direction == null ? -1 : direction.get2DDataValue());
        compoundTag.putInt("GD", this.genDepth);
        this.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
        return compoundTag;
    }

    protected abstract void addAdditionalSaveData(StructurePieceSerializationContext var1, CompoundTag var2);

    public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
    }

    public abstract void postProcess(WorldGenLevel var1, StructureManager var2, ChunkGenerator var3, RandomSource var4, BoundingBox var5, ChunkPos var6, BlockPos var7);

    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    public int getGenDepth() {
        return this.genDepth;
    }

    public void setGenDepth(int n) {
        this.genDepth = n;
    }

    public boolean isCloseToChunk(ChunkPos chunkPos, int n) {
        int n2 = chunkPos.getMinBlockX();
        int n3 = chunkPos.getMinBlockZ();
        return this.boundingBox.intersects(n2 - n, n3 - n, n2 + 15 + n, n3 + 15 + n);
    }

    public BlockPos getLocatorPosition() {
        return new BlockPos(this.boundingBox.getCenter());
    }

    protected BlockPos.MutableBlockPos getWorldPos(int n, int n2, int n3) {
        return new BlockPos.MutableBlockPos(this.getWorldX(n, n3), this.getWorldY(n2), this.getWorldZ(n, n3));
    }

    protected int getWorldX(int n, int n2) {
        Direction direction = this.getOrientation();
        if (direction == null) {
            return n;
        }
        switch (direction) {
            case NORTH: 
            case SOUTH: {
                return this.boundingBox.minX() + n;
            }
            case WEST: {
                return this.boundingBox.maxX() - n2;
            }
            case EAST: {
                return this.boundingBox.minX() + n2;
            }
        }
        return n;
    }

    protected int getWorldY(int n) {
        if (this.getOrientation() == null) {
            return n;
        }
        return n + this.boundingBox.minY();
    }

    protected int getWorldZ(int n, int n2) {
        Direction direction = this.getOrientation();
        if (direction == null) {
            return n2;
        }
        switch (direction) {
            case NORTH: {
                return this.boundingBox.maxZ() - n2;
            }
            case SOUTH: {
                return this.boundingBox.minZ() + n2;
            }
            case WEST: 
            case EAST: {
                return this.boundingBox.minZ() + n;
            }
        }
        return n2;
    }

    protected void placeBlock(WorldGenLevel worldGenLevel, BlockState blockState, int n, int n2, int n3, BoundingBox boundingBox) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
        if (!boundingBox.isInside(mutableBlockPos)) {
            return;
        }
        if (!this.canBeReplaced(worldGenLevel, n, n2, n3, boundingBox)) {
            return;
        }
        if (this.mirror != Mirror.NONE) {
            blockState = blockState.mirror(this.mirror);
        }
        if (this.rotation != Rotation.NONE) {
            blockState = blockState.rotate(this.rotation);
        }
        worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
        FluidState fluidState = worldGenLevel.getFluidState(mutableBlockPos);
        if (!fluidState.isEmpty()) {
            worldGenLevel.scheduleTick((BlockPos)mutableBlockPos, fluidState.getType(), 0);
        }
        if (SHAPE_CHECK_BLOCKS.contains(blockState.getBlock())) {
            worldGenLevel.getChunk(mutableBlockPos).markPosForPostprocessing(mutableBlockPos);
        }
    }

    protected boolean canBeReplaced(LevelReader levelReader, int n, int n2, int n3, BoundingBox boundingBox) {
        return true;
    }

    protected BlockState getBlock(BlockGetter blockGetter, int n, int n2, int n3, BoundingBox boundingBox) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
        if (!boundingBox.isInside(mutableBlockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return blockGetter.getBlockState(mutableBlockPos);
    }

    protected boolean isInterior(LevelReader levelReader, int n, int n2, int n3, BoundingBox boundingBox) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2 + 1, n3);
        if (!boundingBox.isInside(mutableBlockPos)) {
            return false;
        }
        return mutableBlockPos.getY() < levelReader.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, mutableBlockPos.getX(), mutableBlockPos.getZ());
    }

    protected void generateAirBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6) {
        for (int i = n2; i <= n5; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    this.placeBlock(worldGenLevel, Blocks.AIR.defaultBlockState(), j, i, k, boundingBox);
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6, BlockState blockState, BlockState blockState2, boolean bl) {
        for (int i = n2; i <= n5; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    if (bl && this.getBlock(worldGenLevel, j, i, k, boundingBox).isAir()) continue;
                    if (i == n2 || i == n5 || j == n || j == n4 || k == n3 || k == n6) {
                        this.placeBlock(worldGenLevel, blockState, j, i, k, boundingBox);
                        continue;
                    }
                    this.placeBlock(worldGenLevel, blockState2, j, i, k, boundingBox);
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BoundingBox boundingBox2, BlockState blockState, BlockState blockState2, boolean bl) {
        this.generateBox(worldGenLevel, boundingBox, boundingBox2.minX(), boundingBox2.minY(), boundingBox2.minZ(), boundingBox2.maxX(), boundingBox2.maxY(), boundingBox2.maxZ(), blockState, blockState2, bl);
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6, boolean bl, RandomSource randomSource, BlockSelector blockSelector) {
        for (int i = n2; i <= n5; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    if (bl && this.getBlock(worldGenLevel, j, i, k, boundingBox).isAir()) continue;
                    blockSelector.next(randomSource, j, i, k, i == n2 || i == n5 || j == n || j == n4 || k == n3 || k == n6);
                    this.placeBlock(worldGenLevel, blockSelector.getNext(), j, i, k, boundingBox);
                }
            }
        }
    }

    protected void generateBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BoundingBox boundingBox2, boolean bl, RandomSource randomSource, BlockSelector blockSelector) {
        this.generateBox(worldGenLevel, boundingBox, boundingBox2.minX(), boundingBox2.minY(), boundingBox2.minZ(), boundingBox2.maxX(), boundingBox2.maxY(), boundingBox2.maxZ(), bl, randomSource, blockSelector);
    }

    protected void generateMaybeBox(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, float f, int n, int n2, int n3, int n4, int n5, int n6, BlockState blockState, BlockState blockState2, boolean bl, boolean bl2) {
        for (int i = n2; i <= n5; ++i) {
            for (int j = n; j <= n4; ++j) {
                for (int k = n3; k <= n6; ++k) {
                    if (randomSource.nextFloat() > f || bl && this.getBlock(worldGenLevel, j, i, k, boundingBox).isAir() || bl2 && !this.isInterior(worldGenLevel, j, i, k, boundingBox)) continue;
                    if (i == n2 || i == n5 || j == n || j == n4 || k == n3 || k == n6) {
                        this.placeBlock(worldGenLevel, blockState, j, i, k, boundingBox);
                        continue;
                    }
                    this.placeBlock(worldGenLevel, blockState2, j, i, k, boundingBox);
                }
            }
        }
    }

    protected void maybeGenerateBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, float f, int n, int n2, int n3, BlockState blockState) {
        if (randomSource.nextFloat() < f) {
            this.placeBlock(worldGenLevel, blockState, n, n2, n3, boundingBox);
        }
    }

    protected void generateUpperHalfSphere(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, int n6, BlockState blockState, boolean bl) {
        float f = n4 - n + 1;
        float f2 = n5 - n2 + 1;
        float f3 = n6 - n3 + 1;
        float f4 = (float)n + f / 2.0f;
        float f5 = (float)n3 + f3 / 2.0f;
        for (int i = n2; i <= n5; ++i) {
            float f6 = (float)(i - n2) / f2;
            for (int j = n; j <= n4; ++j) {
                float f7 = ((float)j - f4) / (f * 0.5f);
                for (int k = n3; k <= n6; ++k) {
                    float f8;
                    float f9 = ((float)k - f5) / (f3 * 0.5f);
                    if (bl && this.getBlock(worldGenLevel, j, i, k, boundingBox).isAir() || !((f8 = f7 * f7 + f6 * f6 + f9 * f9) <= 1.05f)) continue;
                    this.placeBlock(worldGenLevel, blockState, j, i, k, boundingBox);
                }
            }
        }
    }

    protected void fillColumnDown(WorldGenLevel worldGenLevel, BlockState blockState, int n, int n2, int n3, BoundingBox boundingBox) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
        if (!boundingBox.isInside(mutableBlockPos)) {
            return;
        }
        while (this.isReplaceableByStructures(worldGenLevel.getBlockState(mutableBlockPos)) && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1) {
            worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
            mutableBlockPos.move(Direction.DOWN);
        }
    }

    protected boolean isReplaceableByStructures(BlockState blockState) {
        return blockState.isAir() || blockState.liquid() || blockState.is(Blocks.GLOW_LICHEN) || blockState.is(Blocks.SEAGRASS) || blockState.is(Blocks.TALL_SEAGRASS);
    }

    protected boolean createChest(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int n, int n2, int n3, ResourceKey<LootTable> resourceKey) {
        return this.createChest(worldGenLevel, boundingBox, randomSource, this.getWorldPos(n, n2, n3), resourceKey, null);
    }

    public static BlockState reorient(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState) {
        Object object3;
        Object object2 = null;
        for (Object object3 : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos2 = blockPos.relative((Direction)object3);
            BlockState blockState2 = blockGetter.getBlockState(blockPos2);
            if (blockState2.is(Blocks.CHEST)) {
                return blockState;
            }
            if (!blockState2.isSolidRender(blockGetter, blockPos2)) continue;
            if (object2 == null) {
                object2 = object3;
                continue;
            }
            object2 = null;
            break;
        }
        if (object2 != null) {
            return (BlockState)blockState.setValue(HorizontalDirectionalBlock.FACING, ((Direction)object2).getOpposite());
        }
        Object object4 = blockState.getValue(HorizontalDirectionalBlock.FACING);
        object3 = blockPos.relative((Direction)object4);
        if (blockGetter.getBlockState((BlockPos)object3).isSolidRender(blockGetter, (BlockPos)object3)) {
            object4 = ((Direction)object4).getOpposite();
            object3 = blockPos.relative((Direction)object4);
        }
        if (blockGetter.getBlockState((BlockPos)object3).isSolidRender(blockGetter, (BlockPos)object3)) {
            object4 = ((Direction)object4).getClockWise();
            object3 = blockPos.relative((Direction)object4);
        }
        if (blockGetter.getBlockState((BlockPos)object3).isSolidRender(blockGetter, (BlockPos)object3)) {
            object4 = ((Direction)object4).getOpposite();
            object3 = blockPos.relative((Direction)object4);
        }
        return (BlockState)blockState.setValue(HorizontalDirectionalBlock.FACING, object4);
    }

    protected boolean createChest(ServerLevelAccessor serverLevelAccessor, BoundingBox boundingBox, RandomSource randomSource, BlockPos blockPos, ResourceKey<LootTable> resourceKey, @Nullable BlockState blockState) {
        if (!boundingBox.isInside(blockPos) || serverLevelAccessor.getBlockState(blockPos).is(Blocks.CHEST)) {
            return false;
        }
        if (blockState == null) {
            blockState = StructurePiece.reorient(serverLevelAccessor, blockPos, Blocks.CHEST.defaultBlockState());
        }
        serverLevelAccessor.setBlock(blockPos, blockState, 2);
        BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
        if (blockEntity instanceof ChestBlockEntity) {
            ((ChestBlockEntity)blockEntity).setLootTable(resourceKey, randomSource.nextLong());
        }
        return true;
    }

    protected boolean createDispenser(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int n, int n2, int n3, Direction direction, ResourceKey<LootTable> resourceKey) {
        BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
        if (boundingBox.isInside(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).is(Blocks.DISPENSER)) {
            this.placeBlock(worldGenLevel, (BlockState)Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction), n, n2, n3, boundingBox);
            BlockEntity blockEntity = worldGenLevel.getBlockEntity(mutableBlockPos);
            if (blockEntity instanceof DispenserBlockEntity) {
                ((DispenserBlockEntity)blockEntity).setLootTable(resourceKey, randomSource.nextLong());
            }
            return true;
        }
        return false;
    }

    public void move(int n, int n2, int n3) {
        this.boundingBox.move(n, n2, n3);
    }

    public static BoundingBox createBoundingBox(Stream<StructurePiece> stream) {
        return BoundingBox.encapsulatingBoxes(stream.map(StructurePiece::getBoundingBox)::iterator).orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox without pieces"));
    }

    @Nullable
    public static StructurePiece findCollisionPiece(List<StructurePiece> list, BoundingBox boundingBox) {
        for (StructurePiece structurePiece : list) {
            if (!structurePiece.getBoundingBox().intersects(boundingBox)) continue;
            return structurePiece;
        }
        return null;
    }

    @Nullable
    public Direction getOrientation() {
        return this.orientation;
    }

    public void setOrientation(@Nullable Direction direction) {
        this.orientation = direction;
        if (direction == null) {
            this.rotation = Rotation.NONE;
            this.mirror = Mirror.NONE;
        } else {
            switch (direction) {
                case SOUTH: {
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.NONE;
                    break;
                }
                case WEST: {
                    this.mirror = Mirror.LEFT_RIGHT;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                }
                case EAST: {
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.CLOCKWISE_90;
                    break;
                }
                default: {
                    this.mirror = Mirror.NONE;
                    this.rotation = Rotation.NONE;
                }
            }
        }
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public StructurePieceType getType() {
        return this.type;
    }

    public static abstract class BlockSelector {
        protected BlockState next = Blocks.AIR.defaultBlockState();

        public abstract void next(RandomSource var1, int var2, int var3, int var4, boolean var5);

        public BlockState getNext() {
            return this.next;
        }
    }
}

