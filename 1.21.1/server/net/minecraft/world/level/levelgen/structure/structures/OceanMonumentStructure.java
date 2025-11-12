/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.serialization.MapCodec;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.RandomSupport;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.OceanMonumentPieces;

public class OceanMonumentStructure
extends Structure {
    public static final MapCodec<OceanMonumentStructure> CODEC = OceanMonumentStructure.simpleCodec(OceanMonumentStructure::new);

    public OceanMonumentStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        int n = generationContext.chunkPos().getBlockX(9);
        int n2 = generationContext.chunkPos().getBlockZ(9);
        Set<Holder<Biome>> set = generationContext.biomeSource().getBiomesWithin(n, generationContext.chunkGenerator().getSeaLevel(), n2, 29, generationContext.randomState().sampler());
        for (Holder<Biome> holder : set) {
            if (holder.is(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING)) continue;
            return Optional.empty();
        }
        return OceanMonumentStructure.onTopOfChunkCenter(generationContext, Heightmap.Types.OCEAN_FLOOR_WG, structurePiecesBuilder -> OceanMonumentStructure.generatePieces(structurePiecesBuilder, generationContext));
    }

    private static StructurePiece createTopPiece(ChunkPos chunkPos, WorldgenRandom worldgenRandom) {
        int n = chunkPos.getMinBlockX() - 29;
        int n2 = chunkPos.getMinBlockZ() - 29;
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
        return new OceanMonumentPieces.MonumentBuilding(worldgenRandom, n, n2, direction);
    }

    private static void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        structurePiecesBuilder.addPiece(OceanMonumentStructure.createTopPiece(generationContext.chunkPos(), generationContext.random()));
    }

    public static PiecesContainer regeneratePiecesAfterLoad(ChunkPos chunkPos, long l, PiecesContainer piecesContainer) {
        if (piecesContainer.isEmpty()) {
            return piecesContainer;
        }
        WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenRandom.setLargeFeatureSeed(l, chunkPos.x, chunkPos.z);
        StructurePiece structurePiece = piecesContainer.pieces().get(0);
        BoundingBox boundingBox = structurePiece.getBoundingBox();
        int n = boundingBox.minX();
        int n2 = boundingBox.minZ();
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(worldgenRandom);
        Direction direction2 = Objects.requireNonNullElse(structurePiece.getOrientation(), direction);
        OceanMonumentPieces.MonumentBuilding monumentBuilding = new OceanMonumentPieces.MonumentBuilding(worldgenRandom, n, n2, direction2);
        StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
        structurePiecesBuilder.addPiece(monumentBuilding);
        return structurePiecesBuilder.build();
    }

    @Override
    public StructureType<?> type() {
        return StructureType.OCEAN_MONUMENT;
    }
}

