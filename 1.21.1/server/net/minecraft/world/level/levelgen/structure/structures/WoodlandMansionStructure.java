/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import java.util.LinkedList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.WoodlandMansionPieces;

public class WoodlandMansionStructure
extends Structure {
    public static final MapCodec<WoodlandMansionStructure> CODEC = WoodlandMansionStructure.simpleCodec(WoodlandMansionStructure::new);

    public WoodlandMansionStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = this.getLowestYIn5by5BoxOffset7Blocks(generationContext, rotation);
        if (blockPos.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext, blockPos, rotation)));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext, BlockPos blockPos, Rotation rotation) {
        LinkedList linkedList = Lists.newLinkedList();
        WoodlandMansionPieces.generateMansion(generationContext.structureTemplateManager(), blockPos, rotation, linkedList, generationContext.random());
        linkedList.forEach(structurePiecesBuilder::addPiece);
    }

    @Override
    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        int n = worldGenLevel.getMinBuildHeight();
        BoundingBox boundingBox2 = piecesContainer.calculateBoundingBox();
        int n2 = boundingBox2.minY();
        for (int i = boundingBox.minX(); i <= boundingBox.maxX(); ++i) {
            block1: for (int j = boundingBox.minZ(); j <= boundingBox.maxZ(); ++j) {
                mutableBlockPos.set(i, n2, j);
                if (worldGenLevel.isEmptyBlock(mutableBlockPos) || !boundingBox2.isInside(mutableBlockPos) || !piecesContainer.isInsidePiece(mutableBlockPos)) continue;
                for (int k = n2 - 1; k > n; --k) {
                    mutableBlockPos.setY(k);
                    if (!worldGenLevel.isEmptyBlock(mutableBlockPos) && !worldGenLevel.getBlockState(mutableBlockPos).liquid()) continue block1;
                    worldGenLevel.setBlock(mutableBlockPos, Blocks.COBBLESTONE.defaultBlockState(), 2);
                }
            }
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.WOODLAND_MANSION;
    }
}

