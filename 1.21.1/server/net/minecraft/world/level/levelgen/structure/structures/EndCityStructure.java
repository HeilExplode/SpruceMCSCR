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
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.EndCityPieces;

public class EndCityStructure
extends Structure {
    public static final MapCodec<EndCityStructure> CODEC = EndCityStructure.simpleCodec(EndCityStructure::new);

    public EndCityStructure(Structure.StructureSettings structureSettings) {
        super(structureSettings);
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = this.getLowestYIn5by5BoxOffset7Blocks(generationContext, rotation);
        if (blockPos.getY() < 60) {
            return Optional.empty();
        }
        return Optional.of(new Structure.GenerationStub(blockPos, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, blockPos, rotation, generationContext)));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, BlockPos blockPos, Rotation rotation, Structure.GenerationContext generationContext) {
        ArrayList arrayList = Lists.newArrayList();
        EndCityPieces.startHouseTower(generationContext.structureTemplateManager(), blockPos, rotation, arrayList, generationContext.random());
        arrayList.forEach(structurePiecesBuilder::addPiece);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.END_CITY;
    }
}

