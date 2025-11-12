/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SortedArraySet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class DesertPyramidStructure
extends SinglePieceStructure {
    public static final MapCodec<DesertPyramidStructure> CODEC = DesertPyramidStructure.simpleCodec(DesertPyramidStructure::new);

    public DesertPyramidStructure(Structure.StructureSettings structureSettings) {
        super(DesertPyramidPiece::new, 21, 21, structureSettings);
    }

    @Override
    public void afterPlace(WorldGenLevel worldGenLevel, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource randomSource, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {
        SortedArraySet sortedArraySet = SortedArraySet.create(Vec3i::compareTo);
        for (StructurePiece object2 : piecesContainer.pieces()) {
            if (!(object2 instanceof DesertPyramidPiece)) continue;
            DesertPyramidPiece n = (DesertPyramidPiece)object2;
            sortedArraySet.addAll(n.getPotentialSuspiciousSandWorldPositions());
            DesertPyramidStructure.placeSuspiciousSand(boundingBox, worldGenLevel, n.getRandomCollapsedRoofPos());
        }
        ObjectArrayList objectArrayList = new ObjectArrayList(sortedArraySet.stream().toList());
        RandomSource randomSource2 = RandomSource.create(worldGenLevel.getSeed()).forkPositional().at(piecesContainer.calculateBoundingBox().getCenter());
        Util.shuffle(objectArrayList, randomSource2);
        int n = Math.min(sortedArraySet.size(), randomSource2.nextInt(5, 8));
        for (BlockPos blockPos : objectArrayList) {
            if (n > 0) {
                --n;
                DesertPyramidStructure.placeSuspiciousSand(boundingBox, worldGenLevel, blockPos);
                continue;
            }
            if (!boundingBox.isInside(blockPos)) continue;
            worldGenLevel.setBlock(blockPos, Blocks.SAND.defaultBlockState(), 2);
        }
    }

    private static void placeSuspiciousSand(BoundingBox boundingBox, WorldGenLevel worldGenLevel, BlockPos blockPos) {
        if (boundingBox.isInside(blockPos)) {
            worldGenLevel.setBlock(blockPos, Blocks.SUSPICIOUS_SAND.defaultBlockState(), 2);
            worldGenLevel.getBlockEntity(blockPos, BlockEntityType.BRUSHABLE_BLOCK).ifPresent(brushableBlockEntity -> brushableBlockEntity.setLootTable(BuiltInLootTables.DESERT_PYRAMID_ARCHAEOLOGY, blockPos.asLong()));
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.DESERT_PYRAMID;
    }
}

