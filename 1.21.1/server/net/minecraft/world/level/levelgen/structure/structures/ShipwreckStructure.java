/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.ShipwreckPieces;

public class ShipwreckStructure
extends Structure {
    public static final MapCodec<ShipwreckStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(ShipwreckStructure.settingsCodec(instance), (App)Codec.BOOL.fieldOf("is_beached").forGetter(shipwreckStructure -> shipwreckStructure.isBeached)).apply((Applicative)instance, ShipwreckStructure::new));
    public final boolean isBeached;

    public ShipwreckStructure(Structure.StructureSettings structureSettings, boolean bl) {
        super(structureSettings);
        this.isBeached = bl;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Heightmap.Types types = this.isBeached ? Heightmap.Types.WORLD_SURFACE_WG : Heightmap.Types.OCEAN_FLOOR_WG;
        return ShipwreckStructure.onTopOfChunkCenter(generationContext, types, structurePiecesBuilder -> this.generatePieces((StructurePiecesBuilder)structurePiecesBuilder, generationContext));
    }

    private void generatePieces(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        Rotation rotation = Rotation.getRandom(generationContext.random());
        BlockPos blockPos = new BlockPos(generationContext.chunkPos().getMinBlockX(), 90, generationContext.chunkPos().getMinBlockZ());
        ShipwreckPieces.ShipwreckPiece shipwreckPiece = ShipwreckPieces.addRandomPiece(generationContext.structureTemplateManager(), blockPos, rotation, structurePiecesBuilder, generationContext.random(), this.isBeached);
        if (shipwreckPiece.isTooBigToFitInWorldGenRegion()) {
            int n;
            BoundingBox boundingBox = shipwreckPiece.getBoundingBox();
            if (this.isBeached) {
                int n2 = Structure.getLowestY(generationContext, boundingBox.minX(), boundingBox.getXSpan(), boundingBox.minZ(), boundingBox.getZSpan());
                n = shipwreckPiece.calculateBeachedPosition(n2, generationContext.random());
            } else {
                n = Structure.getMeanFirstOccupiedHeight(generationContext, boundingBox.minX(), boundingBox.getXSpan(), boundingBox.minZ(), boundingBox.getZSpan());
            }
            shipwreckPiece.adjustPositionHeight(n);
        }
    }

    @Override
    public StructureType<?> type() {
        return StructureType.SHIPWRECK;
    }
}

