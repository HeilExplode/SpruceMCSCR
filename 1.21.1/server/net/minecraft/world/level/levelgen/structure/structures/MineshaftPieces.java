/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.MinecartChest;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftStructure;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import org.slf4j.Logger;

public class MineshaftPieces {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_SHAFT_WIDTH = 3;
    private static final int DEFAULT_SHAFT_HEIGHT = 3;
    private static final int DEFAULT_SHAFT_LENGTH = 5;
    private static final int MAX_PILLAR_HEIGHT = 20;
    private static final int MAX_CHAIN_HEIGHT = 50;
    private static final int MAX_DEPTH = 8;
    public static final int MAGIC_START_Y = 50;

    private static MineShaftPiece createRandomShaftPiece(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, @Nullable Direction direction, int n4, MineshaftStructure.Type type) {
        int n5 = randomSource.nextInt(100);
        if (n5 >= 80) {
            BoundingBox boundingBox = MineShaftCrossing.findCrossing(structurePieceAccessor, randomSource, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftCrossing(n4, boundingBox, direction, type);
            }
        } else if (n5 >= 70) {
            BoundingBox boundingBox = MineShaftStairs.findStairs(structurePieceAccessor, randomSource, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftStairs(n4, boundingBox, direction, type);
            }
        } else {
            BoundingBox boundingBox = MineShaftCorridor.findCorridorSize(structurePieceAccessor, randomSource, n, n2, n3, direction);
            if (boundingBox != null) {
                return new MineShaftCorridor(n4, randomSource, boundingBox, direction, type);
            }
        }
        return null;
    }

    static MineShaftPiece generateAndAddPiece(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction, int n4) {
        if (n4 > 8) {
            return null;
        }
        if (Math.abs(n - structurePiece.getBoundingBox().minX()) > 80 || Math.abs(n3 - structurePiece.getBoundingBox().minZ()) > 80) {
            return null;
        }
        MineshaftStructure.Type type = ((MineShaftPiece)structurePiece).type;
        MineShaftPiece mineShaftPiece = MineshaftPieces.createRandomShaftPiece(structurePieceAccessor, randomSource, n, n2, n3, direction, n4 + 1, type);
        if (mineShaftPiece != null) {
            structurePieceAccessor.addPiece(mineShaftPiece);
            mineShaftPiece.addChildren(structurePiece, structurePieceAccessor, randomSource);
        }
        return mineShaftPiece;
    }

    public static class MineShaftCrossing
    extends MineShaftPiece {
        private final Direction direction;
        private final boolean isTwoFloored;

        public MineShaftCrossing(CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, compoundTag);
            this.isTwoFloored = compoundTag.getBoolean("tf");
            this.direction = Direction.from2DDataValue(compoundTag.getInt("D"));
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("tf", this.isTwoFloored);
            compoundTag.putInt("D", this.direction.get2DDataValue());
        }

        public MineShaftCrossing(int n, BoundingBox boundingBox, @Nullable Direction direction, MineshaftStructure.Type type) {
            super(StructurePieceType.MINE_SHAFT_CROSSING, n, type, boundingBox);
            this.direction = direction;
            this.isTwoFloored = boundingBox.getYSpan() > 3;
        }

        @Nullable
        public static BoundingBox findCrossing(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction) {
            int n4 = randomSource.nextInt(4) == 0 ? 6 : 2;
            BoundingBox boundingBox = switch (direction) {
                default -> new BoundingBox(-1, 0, -4, 3, n4, 0);
                case Direction.SOUTH -> new BoundingBox(-1, 0, 0, 3, n4, 4);
                case Direction.WEST -> new BoundingBox(-4, 0, -1, 0, n4, 3);
                case Direction.EAST -> new BoundingBox(0, 0, -1, 4, n4, 3);
            };
            boundingBox.move(n, n2, n3);
            if (structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            int n = this.getGenDepth();
            switch (this.direction) {
                default: {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                    break;
                }
                case SOUTH: {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                    break;
                }
                case WEST: {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.WEST, n);
                    break;
                }
                case EAST: {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, Direction.EAST, n);
                }
            }
            if (this.isTwoFloored) {
                if (randomSource.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() - 1, Direction.NORTH, n);
                }
                if (randomSource.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.WEST, n);
                }
                if (randomSource.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.minZ() + 1, Direction.EAST, n);
                }
                if (randomSource.nextBoolean()) {
                    MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3 + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                }
            }
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.isInInvalidLocation(worldGenLevel, boundingBox)) {
                return;
            }
            BlockState blockState = this.type.getPlanksState();
            if (this.isTwoFloored) {
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.minY() + 3 - 1, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.maxY() - 2, this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.maxY() - 2, this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY() + 3, this.boundingBox.minZ() + 1, this.boundingBox.maxX() - 1, this.boundingBox.minY() + 3, this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), this.boundingBox.maxX() - 1, this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ() - 1, CAVE_AIR, CAVE_AIR, false);
            }
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.minX() + 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.minZ() + 1, this.boundingBox.maxY());
            this.placeSupportPillar(worldGenLevel, boundingBox, this.boundingBox.maxX() - 1, this.boundingBox.minY(), this.boundingBox.maxZ() - 1, this.boundingBox.maxY());
            int n = this.boundingBox.minY() - 1;
            for (int i = this.boundingBox.minX(); i <= this.boundingBox.maxX(); ++i) {
                for (int j = this.boundingBox.minZ(); j <= this.boundingBox.maxZ(); ++j) {
                    this.setPlanksBlock(worldGenLevel, boundingBox, blockState, i, n, j);
                }
            }
        }

        private void placeSupportPillar(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            if (!this.getBlock(worldGenLevel, n, n4 + 1, n3, boundingBox).isAir()) {
                this.generateBox(worldGenLevel, boundingBox, n, n2, n3, n, n4, n3, this.type.getPlanksState(), CAVE_AIR, false);
            }
        }
    }

    public static class MineShaftStairs
    extends MineShaftPiece {
        public MineShaftStairs(int n, BoundingBox boundingBox, Direction direction, MineshaftStructure.Type type) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, n, type, boundingBox);
            this.setOrientation(direction);
        }

        public MineShaftStairs(CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_STAIRS, compoundTag);
        }

        @Nullable
        public static BoundingBox findStairs(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction) {
            BoundingBox boundingBox = switch (direction) {
                default -> new BoundingBox(0, -5, -8, 2, 2, 0);
                case Direction.SOUTH -> new BoundingBox(0, -5, 0, 2, 2, 8);
                case Direction.WEST -> new BoundingBox(-8, -5, 0, 0, 2, 2);
                case Direction.EAST -> new BoundingBox(0, -5, 0, 8, 2, 2);
            };
            boundingBox.move(n, n2, n3);
            if (structurePieceAccessor.findCollisionPiece(boundingBox) != null) {
                return null;
            }
            return boundingBox;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            int n = this.getGenDepth();
            Direction direction = this.getOrientation();
            if (direction != null) {
                switch (direction) {
                    default: {
                        MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                        break;
                    }
                    case SOUTH: {
                        MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                        break;
                    }
                    case WEST: {
                        MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.WEST, n);
                        break;
                    }
                    case EAST: {
                        MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), this.boundingBox.minZ(), Direction.EAST, n);
                    }
                }
            }
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.isInInvalidLocation(worldGenLevel, boundingBox)) {
                return;
            }
            this.generateBox(worldGenLevel, boundingBox, 0, 5, 0, 2, 7, 1, CAVE_AIR, CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 7, 2, 2, 8, CAVE_AIR, CAVE_AIR, false);
            for (int i = 0; i < 5; ++i) {
                this.generateBox(worldGenLevel, boundingBox, 0, 5 - i - (i < 4 ? 1 : 0), 2 + i, 2, 7 - i, 2 + i, CAVE_AIR, CAVE_AIR, false);
            }
        }
    }

    public static class MineShaftCorridor
    extends MineShaftPiece {
        private final boolean hasRails;
        private final boolean spiderCorridor;
        private boolean hasPlacedSpider;
        private final int numSections;

        public MineShaftCorridor(CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, compoundTag);
            this.hasRails = compoundTag.getBoolean("hr");
            this.spiderCorridor = compoundTag.getBoolean("sc");
            this.hasPlacedSpider = compoundTag.getBoolean("hps");
            this.numSections = compoundTag.getInt("Num");
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putBoolean("hr", this.hasRails);
            compoundTag.putBoolean("sc", this.spiderCorridor);
            compoundTag.putBoolean("hps", this.hasPlacedSpider);
            compoundTag.putInt("Num", this.numSections);
        }

        public MineShaftCorridor(int n, RandomSource randomSource, BoundingBox boundingBox, Direction direction, MineshaftStructure.Type type) {
            super(StructurePieceType.MINE_SHAFT_CORRIDOR, n, type, boundingBox);
            this.setOrientation(direction);
            this.hasRails = randomSource.nextInt(3) == 0;
            this.spiderCorridor = !this.hasRails && randomSource.nextInt(23) == 0;
            this.numSections = this.getOrientation().getAxis() == Direction.Axis.Z ? boundingBox.getZSpan() / 5 : boundingBox.getXSpan() / 5;
        }

        /*
         * Enabled aggressive block sorting
         */
        @Nullable
        public static BoundingBox findCorridorSize(StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, int n, int n2, int n3, Direction direction) {
            int n4 = randomSource.nextInt(3) + 2;
            while (n4 > 0) {
                int n5 = n4 * 5;
                BoundingBox boundingBox = switch (direction) {
                    default -> new BoundingBox(0, 0, -(n5 - 1), 2, 2, 0);
                    case Direction.SOUTH -> new BoundingBox(0, 0, 0, 2, 2, n5 - 1);
                    case Direction.WEST -> new BoundingBox(-(n5 - 1), 0, 0, 0, 2, 2);
                    case Direction.EAST -> new BoundingBox(0, 0, 0, n5 - 1, 2, 2);
                };
                boundingBox.move(n, n2, n3);
                if (structurePieceAccessor.findCollisionPiece(boundingBox) == null) {
                    return boundingBox;
                }
                --n4;
            }
            return null;
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            block24: {
                int n = this.getGenDepth();
                int n2 = randomSource.nextInt(4);
                Direction direction = this.getOrientation();
                if (direction != null) {
                    switch (direction) {
                        default: {
                            if (n2 <= 1) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ() - 1, direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ(), Direction.WEST, n);
                                break;
                            }
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ(), Direction.EAST, n);
                            break;
                        }
                        case SOUTH: {
                            if (n2 <= 1) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.maxZ() + 1, direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.maxZ() - 3, Direction.WEST, n);
                                break;
                            }
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.maxZ() - 3, Direction.EAST, n);
                            break;
                        }
                        case WEST: {
                            if (n2 <= 1) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ(), direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                                break;
                            }
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX(), this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                            break;
                        }
                        case EAST: {
                            if (n2 <= 1) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ(), direction, n);
                                break;
                            }
                            if (n2 == 2) {
                                MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.minZ() - 1, Direction.NORTH, n);
                                break;
                            }
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() - 3, this.boundingBox.minY() - 1 + randomSource.nextInt(3), this.boundingBox.maxZ() + 1, Direction.SOUTH, n);
                        }
                    }
                }
                if (n >= 8) break block24;
                if (direction == Direction.NORTH || direction == Direction.SOUTH) {
                    int n3 = this.boundingBox.minZ() + 3;
                    while (n3 + 3 <= this.boundingBox.maxZ()) {
                        int n4 = randomSource.nextInt(5);
                        if (n4 == 0) {
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY(), n3, Direction.WEST, n + 1);
                        } else if (n4 == 1) {
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY(), n3, Direction.EAST, n + 1);
                        }
                        n3 += 5;
                    }
                } else {
                    int n5 = this.boundingBox.minX() + 3;
                    while (n5 + 3 <= this.boundingBox.maxX()) {
                        int n6 = randomSource.nextInt(5);
                        if (n6 == 0) {
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, n5, this.boundingBox.minY(), this.boundingBox.minZ() - 1, Direction.NORTH, n + 1);
                        } else if (n6 == 1) {
                            MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, n5, this.boundingBox.minY(), this.boundingBox.maxZ() + 1, Direction.SOUTH, n + 1);
                        }
                        n5 += 5;
                    }
                }
            }
        }

        @Override
        protected boolean createChest(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, int n, int n2, int n3, ResourceKey<LootTable> resourceKey) {
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            if (boundingBox.isInside(mutableBlockPos) && worldGenLevel.getBlockState(mutableBlockPos).isAir() && !worldGenLevel.getBlockState(((BlockPos)mutableBlockPos).below()).isAir()) {
                BlockState blockState = (BlockState)Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, randomSource.nextBoolean() ? RailShape.NORTH_SOUTH : RailShape.EAST_WEST);
                this.placeBlock(worldGenLevel, blockState, n, n2, n3, boundingBox);
                MinecartChest minecartChest = new MinecartChest(worldGenLevel.getLevel(), (double)mutableBlockPos.getX() + 0.5, (double)mutableBlockPos.getY() + 0.5, (double)mutableBlockPos.getZ() + 0.5);
                minecartChest.setLootTable(resourceKey, randomSource.nextLong());
                worldGenLevel.addFreshEntity(minecartChest);
                return true;
            }
            return false;
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n;
            int n2;
            int n3;
            if (this.isInInvalidLocation(worldGenLevel, boundingBox)) {
                return;
            }
            boolean bl = false;
            int n4 = 2;
            boolean bl2 = false;
            int n5 = 2;
            int n6 = this.numSections * 5 - 1;
            BlockState blockState = this.type.getPlanksState();
            this.generateBox(worldGenLevel, boundingBox, 0, 0, 0, 2, 1, n6, CAVE_AIR, CAVE_AIR, false);
            this.generateMaybeBox(worldGenLevel, boundingBox, randomSource, 0.8f, 0, 2, 0, 2, 2, n6, CAVE_AIR, CAVE_AIR, false, false);
            if (this.spiderCorridor) {
                this.generateMaybeBox(worldGenLevel, boundingBox, randomSource, 0.6f, 0, 0, 0, 2, 1, n6, Blocks.COBWEB.defaultBlockState(), CAVE_AIR, false, true);
            }
            for (n3 = 0; n3 < this.numSections; ++n3) {
                n2 = 2 + n3 * 5;
                this.placeSupport(worldGenLevel, boundingBox, 0, 0, n2, 2, 2, randomSource);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1f, 0, 2, n2 - 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1f, 2, 2, n2 - 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1f, 0, 2, n2 + 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.1f, 2, 2, n2 + 1);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05f, 0, 2, n2 - 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05f, 2, 2, n2 - 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05f, 0, 2, n2 + 2);
                this.maybePlaceCobWeb(worldGenLevel, boundingBox, randomSource, 0.05f, 2, 2, n2 + 2);
                if (randomSource.nextInt(100) == 0) {
                    this.createChest(worldGenLevel, boundingBox, randomSource, 2, 0, n2 - 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (randomSource.nextInt(100) == 0) {
                    this.createChest(worldGenLevel, boundingBox, randomSource, 0, 0, n2 + 1, BuiltInLootTables.ABANDONED_MINESHAFT);
                }
                if (!this.spiderCorridor || this.hasPlacedSpider) continue;
                n = 1;
                int n7 = n2 - 1 + randomSource.nextInt(3);
                BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(1, 0, n7);
                if (!boundingBox.isInside(mutableBlockPos) || !this.isInterior(worldGenLevel, 1, 0, n7, boundingBox)) continue;
                this.hasPlacedSpider = true;
                worldGenLevel.setBlock(mutableBlockPos, Blocks.SPAWNER.defaultBlockState(), 2);
                BlockEntity blockEntity = worldGenLevel.getBlockEntity(mutableBlockPos);
                if (!(blockEntity instanceof SpawnerBlockEntity)) continue;
                SpawnerBlockEntity spawnerBlockEntity = (SpawnerBlockEntity)blockEntity;
                spawnerBlockEntity.setEntityId(EntityType.CAVE_SPIDER, randomSource);
            }
            for (n3 = 0; n3 <= 2; ++n3) {
                for (n2 = 0; n2 <= n6; ++n2) {
                    this.setPlanksBlock(worldGenLevel, boundingBox, blockState, n3, -1, n2);
                }
            }
            n3 = 2;
            this.placeDoubleLowerOrUpperSupport(worldGenLevel, boundingBox, 0, -1, 2);
            if (this.numSections > 1) {
                n2 = n6 - 2;
                this.placeDoubleLowerOrUpperSupport(worldGenLevel, boundingBox, 0, -1, n2);
            }
            if (this.hasRails) {
                BlockState blockState2 = (BlockState)Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH);
                for (n = 0; n <= n6; ++n) {
                    BlockState blockState3 = this.getBlock(worldGenLevel, 1, -1, n, boundingBox);
                    if (blockState3.isAir() || !blockState3.isSolidRender(worldGenLevel, this.getWorldPos(1, -1, n))) continue;
                    float f = this.isInterior(worldGenLevel, 1, 0, n, boundingBox) ? 0.7f : 0.9f;
                    this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, f, 1, 0, n, blockState2);
                }
            }
        }

        private void placeDoubleLowerOrUpperSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3) {
            BlockState blockState = this.type.getWoodState();
            BlockState blockState2 = this.type.getPlanksState();
            if (this.getBlock(worldGenLevel, n, n2, n3, boundingBox).is(blockState2.getBlock())) {
                this.fillPillarDownOrChainUp(worldGenLevel, blockState, n, n2, n3, boundingBox);
            }
            if (this.getBlock(worldGenLevel, n + 2, n2, n3, boundingBox).is(blockState2.getBlock())) {
                this.fillPillarDownOrChainUp(worldGenLevel, blockState, n + 2, n2, n3, boundingBox);
            }
        }

        @Override
        protected void fillColumnDown(WorldGenLevel worldGenLevel, BlockState blockState, int n, int n2, int n3, BoundingBox boundingBox) {
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            if (!boundingBox.isInside(mutableBlockPos)) {
                return;
            }
            int n4 = mutableBlockPos.getY();
            while (this.isReplaceableByStructures(worldGenLevel.getBlockState(mutableBlockPos)) && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1) {
                mutableBlockPos.move(Direction.DOWN);
            }
            if (!this.canPlaceColumnOnTopOf(worldGenLevel, mutableBlockPos, worldGenLevel.getBlockState(mutableBlockPos))) {
                return;
            }
            while (mutableBlockPos.getY() < n4) {
                mutableBlockPos.move(Direction.UP);
                worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
            }
        }

        protected void fillPillarDownOrChainUp(WorldGenLevel worldGenLevel, BlockState blockState, int n, int n2, int n3, BoundingBox boundingBox) {
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            if (!boundingBox.isInside(mutableBlockPos)) {
                return;
            }
            int n4 = mutableBlockPos.getY();
            int n5 = 1;
            boolean bl = true;
            boolean bl2 = true;
            while (bl || bl2) {
                boolean bl3;
                BlockState blockState2;
                if (bl) {
                    mutableBlockPos.setY(n4 - n5);
                    blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
                    boolean bl4 = bl3 = this.isReplaceableByStructures(blockState2) && !blockState2.is(Blocks.LAVA);
                    if (!bl3 && this.canPlaceColumnOnTopOf(worldGenLevel, mutableBlockPos, blockState2)) {
                        MineShaftCorridor.fillColumnBetween(worldGenLevel, blockState, mutableBlockPos, n4 - n5 + 1, n4);
                        return;
                    }
                    boolean bl5 = bl = n5 <= 20 && bl3 && mutableBlockPos.getY() > worldGenLevel.getMinBuildHeight() + 1;
                }
                if (bl2) {
                    mutableBlockPos.setY(n4 + n5);
                    blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
                    bl3 = this.isReplaceableByStructures(blockState2);
                    if (!bl3 && this.canHangChainBelow(worldGenLevel, mutableBlockPos, blockState2)) {
                        worldGenLevel.setBlock(mutableBlockPos.setY(n4 + 1), this.type.getFenceState(), 2);
                        MineShaftCorridor.fillColumnBetween(worldGenLevel, Blocks.CHAIN.defaultBlockState(), mutableBlockPos, n4 + 2, n4 + n5);
                        return;
                    }
                    bl2 = n5 <= 50 && bl3 && mutableBlockPos.getY() < worldGenLevel.getMaxBuildHeight() - 1;
                }
                ++n5;
            }
        }

        private static void fillColumnBetween(WorldGenLevel worldGenLevel, BlockState blockState, BlockPos.MutableBlockPos mutableBlockPos, int n, int n2) {
            for (int i = n; i < n2; ++i) {
                worldGenLevel.setBlock(mutableBlockPos.setY(i), blockState, 2);
            }
        }

        private boolean canPlaceColumnOnTopOf(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
            return blockState.isFaceSturdy(levelReader, blockPos, Direction.UP);
        }

        private boolean canHangChainBelow(LevelReader levelReader, BlockPos blockPos, BlockState blockState) {
            return Block.canSupportCenter(levelReader, blockPos, Direction.DOWN) && !(blockState.getBlock() instanceof FallingBlock);
        }

        private void placeSupport(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4, int n5, RandomSource randomSource) {
            if (!this.isSupportingBox(worldGenLevel, boundingBox, n, n5, n4, n3)) {
                return;
            }
            BlockState blockState = this.type.getPlanksState();
            BlockState blockState2 = this.type.getFenceState();
            this.generateBox(worldGenLevel, boundingBox, n, n2, n3, n, n4 - 1, n3, (BlockState)blockState2.setValue(FenceBlock.WEST, true), CAVE_AIR, false);
            this.generateBox(worldGenLevel, boundingBox, n5, n2, n3, n5, n4 - 1, n3, (BlockState)blockState2.setValue(FenceBlock.EAST, true), CAVE_AIR, false);
            if (randomSource.nextInt(4) == 0) {
                this.generateBox(worldGenLevel, boundingBox, n, n4, n3, n, n4, n3, blockState, CAVE_AIR, false);
                this.generateBox(worldGenLevel, boundingBox, n5, n4, n3, n5, n4, n3, blockState, CAVE_AIR, false);
            } else {
                this.generateBox(worldGenLevel, boundingBox, n, n4, n3, n5, n4, n3, blockState, CAVE_AIR, false);
                this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.05f, n + 1, n4, n3 - 1, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.SOUTH));
                this.maybeGenerateBlock(worldGenLevel, boundingBox, randomSource, 0.05f, n + 1, n4, n3 + 1, (BlockState)Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, Direction.NORTH));
            }
        }

        private void maybePlaceCobWeb(WorldGenLevel worldGenLevel, BoundingBox boundingBox, RandomSource randomSource, float f, int n, int n2, int n3) {
            if (this.isInterior(worldGenLevel, n, n2, n3, boundingBox) && randomSource.nextFloat() < f && this.hasSturdyNeighbours(worldGenLevel, boundingBox, n, n2, n3, 2)) {
                this.placeBlock(worldGenLevel, Blocks.COBWEB.defaultBlockState(), n, n2, n3, boundingBox);
            }
        }

        private boolean hasSturdyNeighbours(WorldGenLevel worldGenLevel, BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            int n5 = 0;
            for (Direction direction : Direction.values()) {
                mutableBlockPos.move(direction);
                if (boundingBox.isInside(mutableBlockPos) && worldGenLevel.getBlockState(mutableBlockPos).isFaceSturdy(worldGenLevel, mutableBlockPos, direction.getOpposite()) && ++n5 >= n4) {
                    return true;
                }
                mutableBlockPos.move(direction.getOpposite());
            }
            return false;
        }
    }

    static abstract class MineShaftPiece
    extends StructurePiece {
        protected MineshaftStructure.Type type;

        public MineShaftPiece(StructurePieceType structurePieceType, int n, MineshaftStructure.Type type, BoundingBox boundingBox) {
            super(structurePieceType, n, boundingBox);
            this.type = type;
        }

        public MineShaftPiece(StructurePieceType structurePieceType, CompoundTag compoundTag) {
            super(structurePieceType, compoundTag);
            this.type = MineshaftStructure.Type.byId(compoundTag.getInt("MST"));
        }

        @Override
        protected boolean canBeReplaced(LevelReader levelReader, int n, int n2, int n3, BoundingBox boundingBox) {
            BlockState blockState = this.getBlock(levelReader, n, n2, n3, boundingBox);
            return !blockState.is(this.type.getPlanksState().getBlock()) && !blockState.is(this.type.getWoodState().getBlock()) && !blockState.is(this.type.getFenceState().getBlock()) && !blockState.is(Blocks.CHAIN);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            compoundTag.putInt("MST", this.type.ordinal());
        }

        protected boolean isSupportingBox(BlockGetter blockGetter, BoundingBox boundingBox, int n, int n2, int n3, int n4) {
            for (int i = n; i <= n2; ++i) {
                if (!this.getBlock(blockGetter, i, n3 + 1, n4, boundingBox).isAir()) continue;
                return false;
            }
            return true;
        }

        protected boolean isInInvalidLocation(LevelAccessor levelAccessor, BoundingBox boundingBox) {
            int n;
            int n2;
            int n3;
            int n4;
            int n5 = Math.max(this.boundingBox.minX() - 1, boundingBox.minX());
            int n6 = Math.max(this.boundingBox.minY() - 1, boundingBox.minY());
            int n7 = Math.max(this.boundingBox.minZ() - 1, boundingBox.minZ());
            int n8 = Math.min(this.boundingBox.maxX() + 1, boundingBox.maxX());
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos((n5 + n8) / 2, (n6 + (n4 = Math.min(this.boundingBox.maxY() + 1, boundingBox.maxY()))) / 2, (n7 + (n3 = Math.min(this.boundingBox.maxZ() + 1, boundingBox.maxZ()))) / 2);
            if (levelAccessor.getBiome(mutableBlockPos).is(BiomeTags.MINESHAFT_BLOCKING)) {
                return true;
            }
            for (n2 = n5; n2 <= n8; ++n2) {
                for (n = n7; n <= n3; ++n) {
                    if (levelAccessor.getBlockState(mutableBlockPos.set(n2, n6, n)).liquid()) {
                        return true;
                    }
                    if (!levelAccessor.getBlockState(mutableBlockPos.set(n2, n4, n)).liquid()) continue;
                    return true;
                }
            }
            for (n2 = n5; n2 <= n8; ++n2) {
                for (n = n6; n <= n4; ++n) {
                    if (levelAccessor.getBlockState(mutableBlockPos.set(n2, n, n7)).liquid()) {
                        return true;
                    }
                    if (!levelAccessor.getBlockState(mutableBlockPos.set(n2, n, n3)).liquid()) continue;
                    return true;
                }
            }
            for (n2 = n7; n2 <= n3; ++n2) {
                for (n = n6; n <= n4; ++n) {
                    if (levelAccessor.getBlockState(mutableBlockPos.set(n5, n, n2)).liquid()) {
                        return true;
                    }
                    if (!levelAccessor.getBlockState(mutableBlockPos.set(n8, n, n2)).liquid()) continue;
                    return true;
                }
            }
            return false;
        }

        protected void setPlanksBlock(WorldGenLevel worldGenLevel, BoundingBox boundingBox, BlockState blockState, int n, int n2, int n3) {
            if (!this.isInterior(worldGenLevel, n, n2, n3, boundingBox)) {
                return;
            }
            BlockPos.MutableBlockPos mutableBlockPos = this.getWorldPos(n, n2, n3);
            BlockState blockState2 = worldGenLevel.getBlockState(mutableBlockPos);
            if (!blockState2.isFaceSturdy(worldGenLevel, mutableBlockPos, Direction.UP)) {
                worldGenLevel.setBlock(mutableBlockPos, blockState, 2);
            }
        }
    }

    public static class MineShaftRoom
    extends MineShaftPiece {
        private final List<BoundingBox> childEntranceBoxes = Lists.newLinkedList();

        public MineShaftRoom(int n, RandomSource randomSource, int n2, int n3, MineshaftStructure.Type type) {
            super(StructurePieceType.MINE_SHAFT_ROOM, n, type, new BoundingBox(n2, 50, n3, n2 + 7 + randomSource.nextInt(6), 54 + randomSource.nextInt(6), n3 + 7 + randomSource.nextInt(6)));
            this.type = type;
        }

        public MineShaftRoom(CompoundTag compoundTag) {
            super(StructurePieceType.MINE_SHAFT_ROOM, compoundTag);
            BoundingBox.CODEC.listOf().parse((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getList("Entrances", 11)).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(this.childEntranceBoxes::addAll);
        }

        @Override
        public void addChildren(StructurePiece structurePiece, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource) {
            BoundingBox boundingBox;
            MineShaftPiece mineShaftPiece;
            int n;
            int n2 = this.getGenDepth();
            int n3 = this.boundingBox.getYSpan() - 3 - 1;
            if (n3 <= 0) {
                n3 = 1;
            }
            for (n = 0; n < this.boundingBox.getXSpan() && (n += randomSource.nextInt(this.boundingBox.getXSpan())) + 3 <= this.boundingBox.getXSpan(); n += 4) {
                mineShaftPiece = MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n, this.boundingBox.minY() + randomSource.nextInt(n3) + 1, this.boundingBox.minZ() - 1, Direction.NORTH, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.minZ() + 1));
            }
            for (n = 0; n < this.boundingBox.getXSpan() && (n += randomSource.nextInt(this.boundingBox.getXSpan())) + 3 <= this.boundingBox.getXSpan(); n += 4) {
                mineShaftPiece = MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() + n, this.boundingBox.minY() + randomSource.nextInt(n3) + 1, this.boundingBox.maxZ() + 1, Direction.SOUTH, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(boundingBox.minX(), boundingBox.minY(), this.boundingBox.maxZ() - 1, boundingBox.maxX(), boundingBox.maxY(), this.boundingBox.maxZ()));
            }
            for (n = 0; n < this.boundingBox.getZSpan() && (n += randomSource.nextInt(this.boundingBox.getZSpan())) + 3 <= this.boundingBox.getZSpan(); n += 4) {
                mineShaftPiece = MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.minX() - 1, this.boundingBox.minY() + randomSource.nextInt(n3) + 1, this.boundingBox.minZ() + n, Direction.WEST, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), this.boundingBox.minX() + 1, boundingBox.maxY(), boundingBox.maxZ()));
            }
            for (n = 0; n < this.boundingBox.getZSpan() && (n += randomSource.nextInt(this.boundingBox.getZSpan())) + 3 <= this.boundingBox.getZSpan(); n += 4) {
                mineShaftPiece = MineshaftPieces.generateAndAddPiece(structurePiece, structurePieceAccessor, randomSource, this.boundingBox.maxX() + 1, this.boundingBox.minY() + randomSource.nextInt(n3) + 1, this.boundingBox.minZ() + n, Direction.EAST, n2);
                if (mineShaftPiece == null) continue;
                boundingBox = mineShaftPiece.getBoundingBox();
                this.childEntranceBoxes.add(new BoundingBox(this.boundingBox.maxX() - 1, boundingBox.minY(), boundingBox.minZ(), this.boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()));
            }
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            if (this.isInInvalidLocation(worldGenLevel, boundingBox)) {
                return;
            }
            this.generateBox(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY() + 1, this.boundingBox.minZ(), this.boundingBox.maxX(), Math.min(this.boundingBox.minY() + 3, this.boundingBox.maxY()), this.boundingBox.maxZ(), CAVE_AIR, CAVE_AIR, false);
            for (BoundingBox boundingBox2 : this.childEntranceBoxes) {
                this.generateBox(worldGenLevel, boundingBox, boundingBox2.minX(), boundingBox2.maxY() - 2, boundingBox2.minZ(), boundingBox2.maxX(), boundingBox2.maxY(), boundingBox2.maxZ(), CAVE_AIR, CAVE_AIR, false);
            }
            this.generateUpperHalfSphere(worldGenLevel, boundingBox, this.boundingBox.minX(), this.boundingBox.minY() + 4, this.boundingBox.minZ(), this.boundingBox.maxX(), this.boundingBox.maxY(), this.boundingBox.maxZ(), CAVE_AIR, false);
        }

        @Override
        public void move(int n, int n2, int n3) {
            super.move(n, n2, n3);
            for (BoundingBox boundingBox : this.childEntranceBoxes) {
                boundingBox.move(n, n2, n3);
            }
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            BoundingBox.CODEC.listOf().encodeStart((DynamicOps)NbtOps.INSTANCE, this.childEntranceBoxes).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("Entrances", (Tag)tag));
        }
    }
}

