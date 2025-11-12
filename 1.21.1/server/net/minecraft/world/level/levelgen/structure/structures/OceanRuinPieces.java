/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.structures.OceanRuinStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.CappedProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.AppendLoot;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;

public class OceanRuinPieces {
    static final StructureProcessor WARM_SUSPICIOUS_BLOCK_PROCESSOR = OceanRuinPieces.archyRuleProcessor(Blocks.SAND, Blocks.SUSPICIOUS_SAND, BuiltInLootTables.OCEAN_RUIN_WARM_ARCHAEOLOGY);
    static final StructureProcessor COLD_SUSPICIOUS_BLOCK_PROCESSOR = OceanRuinPieces.archyRuleProcessor(Blocks.GRAVEL, Blocks.SUSPICIOUS_GRAVEL, BuiltInLootTables.OCEAN_RUIN_COLD_ARCHAEOLOGY);
    private static final ResourceLocation[] WARM_RUINS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/warm_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_4"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_5"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_6"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_7"), ResourceLocation.withDefaultNamespace("underwater_ruin/warm_8")};
    private static final ResourceLocation[] RUINS_BRICK = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/brick_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_4"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_5"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_6"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_7"), ResourceLocation.withDefaultNamespace("underwater_ruin/brick_8")};
    private static final ResourceLocation[] RUINS_CRACKED = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_4"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_5"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_6"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_7"), ResourceLocation.withDefaultNamespace("underwater_ruin/cracked_8")};
    private static final ResourceLocation[] RUINS_MOSSY = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_4"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_5"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_6"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_7"), ResourceLocation.withDefaultNamespace("underwater_ruin/mossy_8")};
    private static final ResourceLocation[] BIG_RUINS_BRICK = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/big_brick_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_brick_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_brick_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_brick_8")};
    private static final ResourceLocation[] BIG_RUINS_MOSSY = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/big_mossy_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_mossy_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_mossy_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_mossy_8")};
    private static final ResourceLocation[] BIG_RUINS_CRACKED = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/big_cracked_1"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_cracked_2"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_cracked_3"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_cracked_8")};
    private static final ResourceLocation[] BIG_WARM_RUINS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("underwater_ruin/big_warm_4"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_warm_5"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_warm_6"), ResourceLocation.withDefaultNamespace("underwater_ruin/big_warm_7")};

    private static StructureProcessor archyRuleProcessor(Block block, Block block2, ResourceKey<LootTable> resourceKey) {
        return new CappedProcessor(new RuleProcessor(List.of(new ProcessorRule(new BlockMatchTest(block), AlwaysTrueTest.INSTANCE, PosAlwaysTrueTest.INSTANCE, block2.defaultBlockState(), new AppendLoot(resourceKey)))), ConstantInt.of(5));
    }

    private static ResourceLocation getSmallWarmRuin(RandomSource randomSource) {
        return Util.getRandom(WARM_RUINS, randomSource);
    }

    private static ResourceLocation getBigWarmRuin(RandomSource randomSource) {
        return Util.getRandom(BIG_WARM_RUINS, randomSource);
    }

    public static void addPieces(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, OceanRuinStructure oceanRuinStructure) {
        boolean bl = randomSource.nextFloat() <= oceanRuinStructure.largeProbability;
        float f = bl ? 0.9f : 0.8f;
        OceanRuinPieces.addPiece(structureTemplateManager, blockPos, rotation, structurePieceAccessor, randomSource, oceanRuinStructure, bl, f);
        if (bl && randomSource.nextFloat() <= oceanRuinStructure.clusterProbability) {
            OceanRuinPieces.addClusterRuins(structureTemplateManager, randomSource, rotation, blockPos, oceanRuinStructure, structurePieceAccessor);
        }
    }

    private static void addClusterRuins(StructureTemplateManager structureTemplateManager, RandomSource randomSource, Rotation rotation, BlockPos blockPos, OceanRuinStructure oceanRuinStructure, StructurePieceAccessor structurePieceAccessor) {
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), 90, blockPos.getZ());
        BlockPos blockPos3 = StructureTemplate.transform(new BlockPos(15, 0, 15), Mirror.NONE, rotation, BlockPos.ZERO).offset(blockPos2);
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos2, blockPos3);
        BlockPos blockPos4 = new BlockPos(Math.min(blockPos2.getX(), blockPos3.getX()), blockPos2.getY(), Math.min(blockPos2.getZ(), blockPos3.getZ()));
        List<BlockPos> list = OceanRuinPieces.allPositions(randomSource, blockPos4);
        int n = Mth.nextInt(randomSource, 4, 8);
        for (int i = 0; i < n; ++i) {
            Rotation rotation2;
            BlockPos blockPos5;
            int n2;
            BlockPos blockPos6;
            BoundingBox boundingBox2;
            if (list.isEmpty() || (boundingBox2 = BoundingBox.fromCorners(blockPos6 = list.remove(n2 = randomSource.nextInt(list.size())), blockPos5 = StructureTemplate.transform(new BlockPos(5, 0, 6), Mirror.NONE, rotation2 = Rotation.getRandom(randomSource), BlockPos.ZERO).offset(blockPos6))).intersects(boundingBox)) continue;
            OceanRuinPieces.addPiece(structureTemplateManager, blockPos6, rotation2, structurePieceAccessor, randomSource, oceanRuinStructure, false, 0.8f);
        }
    }

    private static List<BlockPos> allPositions(RandomSource randomSource, BlockPos blockPos) {
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(blockPos.offset(-16 + Mth.nextInt(randomSource, 1, 8), 0, 16 + Mth.nextInt(randomSource, 1, 7)));
        arrayList.add(blockPos.offset(-16 + Mth.nextInt(randomSource, 1, 8), 0, Mth.nextInt(randomSource, 1, 7)));
        arrayList.add(blockPos.offset(-16 + Mth.nextInt(randomSource, 1, 8), 0, -16 + Mth.nextInt(randomSource, 4, 8)));
        arrayList.add(blockPos.offset(Mth.nextInt(randomSource, 1, 7), 0, 16 + Mth.nextInt(randomSource, 1, 7)));
        arrayList.add(blockPos.offset(Mth.nextInt(randomSource, 1, 7), 0, -16 + Mth.nextInt(randomSource, 4, 6)));
        arrayList.add(blockPos.offset(16 + Mth.nextInt(randomSource, 1, 7), 0, 16 + Mth.nextInt(randomSource, 3, 8)));
        arrayList.add(blockPos.offset(16 + Mth.nextInt(randomSource, 1, 7), 0, Mth.nextInt(randomSource, 1, 7)));
        arrayList.add(blockPos.offset(16 + Mth.nextInt(randomSource, 1, 7), 0, -16 + Mth.nextInt(randomSource, 4, 8)));
        return arrayList;
    }

    private static void addPiece(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, StructurePieceAccessor structurePieceAccessor, RandomSource randomSource, OceanRuinStructure oceanRuinStructure, boolean bl, float f) {
        switch (oceanRuinStructure.biomeTemp) {
            default: {
                ResourceLocation resourceLocation = bl ? OceanRuinPieces.getBigWarmRuin(randomSource) : OceanRuinPieces.getSmallWarmRuin(randomSource);
                structurePieceAccessor.addPiece(new OceanRuinPiece(structureTemplateManager, resourceLocation, blockPos, rotation, f, oceanRuinStructure.biomeTemp, bl));
                break;
            }
            case COLD: {
                ResourceLocation[] resourceLocationArray = bl ? BIG_RUINS_BRICK : RUINS_BRICK;
                ResourceLocation[] resourceLocationArray2 = bl ? BIG_RUINS_CRACKED : RUINS_CRACKED;
                ResourceLocation[] resourceLocationArray3 = bl ? BIG_RUINS_MOSSY : RUINS_MOSSY;
                int n = randomSource.nextInt(resourceLocationArray.length);
                structurePieceAccessor.addPiece(new OceanRuinPiece(structureTemplateManager, resourceLocationArray[n], blockPos, rotation, f, oceanRuinStructure.biomeTemp, bl));
                structurePieceAccessor.addPiece(new OceanRuinPiece(structureTemplateManager, resourceLocationArray2[n], blockPos, rotation, 0.7f, oceanRuinStructure.biomeTemp, bl));
                structurePieceAccessor.addPiece(new OceanRuinPiece(structureTemplateManager, resourceLocationArray3[n], blockPos, rotation, 0.5f, oceanRuinStructure.biomeTemp, bl));
            }
        }
    }

    public static class OceanRuinPiece
    extends TemplateStructurePiece {
        private final OceanRuinStructure.Type biomeType;
        private final float integrity;
        private final boolean isLarge;

        public OceanRuinPiece(StructureTemplateManager structureTemplateManager, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, float f, OceanRuinStructure.Type type, boolean bl) {
            super(StructurePieceType.OCEAN_RUIN, 0, structureTemplateManager, resourceLocation, resourceLocation.toString(), OceanRuinPiece.makeSettings(rotation, f, type), blockPos);
            this.integrity = f;
            this.biomeType = type;
            this.isLarge = bl;
        }

        private OceanRuinPiece(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag, Rotation rotation, float f, OceanRuinStructure.Type type, boolean bl) {
            super(StructurePieceType.OCEAN_RUIN, compoundTag, structureTemplateManager, resourceLocation -> OceanRuinPiece.makeSettings(rotation, f, type));
            this.integrity = f;
            this.biomeType = type;
            this.isLarge = bl;
        }

        private static StructurePlaceSettings makeSettings(Rotation rotation, float f, OceanRuinStructure.Type type) {
            StructureProcessor structureProcessor = type == OceanRuinStructure.Type.COLD ? COLD_SUSPICIOUS_BLOCK_PROCESSOR : WARM_SUSPICIOUS_BLOCK_PROCESSOR;
            return new StructurePlaceSettings().setRotation(rotation).setMirror(Mirror.NONE).addProcessor(new BlockRotProcessor(f)).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR).addProcessor(structureProcessor);
        }

        public static OceanRuinPiece create(StructureTemplateManager structureTemplateManager, CompoundTag compoundTag) {
            Rotation rotation = Rotation.valueOf(compoundTag.getString("Rot"));
            float f = compoundTag.getFloat("Integrity");
            OceanRuinStructure.Type type = OceanRuinStructure.Type.valueOf(compoundTag.getString("BiomeType"));
            boolean bl = compoundTag.getBoolean("IsLarge");
            return new OceanRuinPiece(structureTemplateManager, compoundTag, rotation, f, type, bl);
        }

        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
            super.addAdditionalSaveData(structurePieceSerializationContext, compoundTag);
            compoundTag.putString("Rot", this.placeSettings.getRotation().name());
            compoundTag.putFloat("Integrity", this.integrity);
            compoundTag.putString("BiomeType", this.biomeType.toString());
            compoundTag.putBoolean("IsLarge", this.isLarge);
        }

        @Override
        protected void handleDataMarker(String string, BlockPos blockPos, ServerLevelAccessor serverLevelAccessor, RandomSource randomSource, BoundingBox boundingBox) {
            Drowned drowned;
            if ("chest".equals(string)) {
                serverLevelAccessor.setBlock(blockPos, (BlockState)Blocks.CHEST.defaultBlockState().setValue(ChestBlock.WATERLOGGED, serverLevelAccessor.getFluidState(blockPos).is(FluidTags.WATER)), 2);
                BlockEntity blockEntity = serverLevelAccessor.getBlockEntity(blockPos);
                if (blockEntity instanceof ChestBlockEntity) {
                    ((ChestBlockEntity)blockEntity).setLootTable(this.isLarge ? BuiltInLootTables.UNDERWATER_RUIN_BIG : BuiltInLootTables.UNDERWATER_RUIN_SMALL, randomSource.nextLong());
                }
            } else if ("drowned".equals(string) && (drowned = EntityType.DROWNED.create(serverLevelAccessor.getLevel())) != null) {
                drowned.setPersistenceRequired();
                drowned.moveTo(blockPos, 0.0f, 0.0f);
                drowned.finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(blockPos), MobSpawnType.STRUCTURE, null);
                serverLevelAccessor.addFreshEntityWithPassengers(drowned);
                if (blockPos.getY() > serverLevelAccessor.getSeaLevel()) {
                    serverLevelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 2);
                } else {
                    serverLevelAccessor.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 2);
                }
            }
        }

        @Override
        public void postProcess(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, BlockPos blockPos) {
            int n = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, this.templatePosition.getX(), this.templatePosition.getZ());
            this.templatePosition = new BlockPos(this.templatePosition.getX(), n, this.templatePosition.getZ());
            BlockPos blockPos2 = StructureTemplate.transform(new BlockPos(this.template.getSize().getX() - 1, 0, this.template.getSize().getZ() - 1), Mirror.NONE, this.placeSettings.getRotation(), BlockPos.ZERO).offset(this.templatePosition);
            this.templatePosition = new BlockPos(this.templatePosition.getX(), this.getHeight(this.templatePosition, worldGenLevel, blockPos2), this.templatePosition.getZ());
            super.postProcess(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, chunkPos, blockPos);
        }

        private int getHeight(BlockPos blockPos, BlockGetter blockGetter, BlockPos blockPos2) {
            int n = blockPos.getY();
            int n2 = 512;
            int n3 = n - 1;
            int n4 = 0;
            for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
                int n5 = blockPos3.getX();
                int n6 = blockPos3.getZ();
                int n7 = blockPos.getY() - 1;
                BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n5, n7, n6);
                BlockState blockState = blockGetter.getBlockState(mutableBlockPos);
                FluidState fluidState = blockGetter.getFluidState(mutableBlockPos);
                while ((blockState.isAir() || fluidState.is(FluidTags.WATER) || blockState.is(BlockTags.ICE)) && n7 > blockGetter.getMinBuildHeight() + 1) {
                    mutableBlockPos.set(n5, --n7, n6);
                    blockState = blockGetter.getBlockState(mutableBlockPos);
                    fluidState = blockGetter.getFluidState(mutableBlockPos);
                }
                n2 = Math.min(n2, n7);
                if (n7 >= n3 - 2) continue;
                ++n4;
            }
            int n8 = Math.abs(blockPos.getX() - blockPos2.getX());
            if (n3 - n2 > 2 && n4 > n8 - 2) {
                n = n2 + 1;
            }
            return n;
        }
    }
}

