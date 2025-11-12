/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.StrongholdPieces;

public class StrongholdStructure
extends Structure {
    public static final MapCodec<StrongholdStructure> CODEC = StrongholdStructure.simpleCodec(StrongholdStructure::new);

    public StrongholdStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        return Optional.of(new Structure.GenerationStub(generationContext.chunkPos().getWorldPosition(), structurePiecesBuilder -> StrongholdStructure.generatePieces(structurePiecesBuilder, generationContext)));
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        StrongholdPieces.StartPiece startPiece;
        int n = 0;
        do {
            structurePiecesBuilder.clear();
            generationContext.random().setLargeFeatureSeed(generationContext.seed() + (long)n++, generationContext.chunkPos().x, generationContext.chunkPos().z);
            StrongholdPieces.resetPieces();
            startPiece = new StrongholdPieces.StartPiece(generationContext.random(), generationContext.chunkPos().getBlockX(2), generationContext.chunkPos().getBlockZ(2));
            structurePiecesBuilder.addPiece(startPiece);
            startPiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
            List<StructurePiece> list = startPiece.pendingChildren;
            while (!list.isEmpty()) {
                int n2 = generationContext.random().nextInt(list.size());
                StructurePiece structurePiece = list.remove(n2);
                structurePiece.addChildren(startPiece, structurePiecesBuilder, generationContext.random());
            }
            structurePiecesBuilder.moveBelowSeaLevel(generationContext.chunkGenerator().getSeaLevel(), generationContext.chunkGenerator().getMinY(), generationContext.random(), 10);
        } while (structurePiecesBuilder.isEmpty() || startPiece.portalRoomPiece == null);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.STRONGHOLD;
    }
}

