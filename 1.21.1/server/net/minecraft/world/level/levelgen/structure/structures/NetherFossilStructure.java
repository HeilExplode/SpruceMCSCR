/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.NetherFossilPieces;

public class NetherFossilStructure
extends Structure {
    public static final MapCodec<NetherFossilStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(NetherFossilStructure.settingsCodec(instance), (App)HeightProvider.CODEC.fieldOf("height").forGetter(netherFossilStructure -> netherFossilStructure.height)).apply((Applicative)instance, NetherFossilStructure::new));
    public final HeightProvider height;

    public NetherFossilStructure(Structure.StructureSettings structureSettings, HeightProvider heightProvider) {
        super(structureSettings);
        this.height = heightProvider;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        Object object;
        WorldgenRandom worldgenRandom = generationContext.random();
        int n = generationContext.chunkPos().getMinBlockX() + worldgenRandom.nextInt(16);
        int n2 = generationContext.chunkPos().getMinBlockZ() + worldgenRandom.nextInt(16);
        int n3 = generationContext.chunkGenerator().getSeaLevel();
        WorldGenerationContext worldGenerationContext = new WorldGenerationContext(generationContext.chunkGenerator(), generationContext.heightAccessor());
        int n4 = this.height.sample(worldgenRandom, worldGenerationContext);
        NoiseColumn noiseColumn = generationContext.chunkGenerator().getBaseColumn(n, n2, generationContext.heightAccessor(), generationContext.randomState());
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(n, n4, n2);
        while (n4 > n3) {
            object = noiseColumn.getBlock(n4);
            BlockState blockState = noiseColumn.getBlock(--n4);
            if (!((BlockBehaviour.BlockStateBase)object).isAir() || !blockState.is(Blocks.SOUL_SAND) && !blockState.isFaceSturdy(EmptyBlockGetter.INSTANCE, mutableBlockPos.setY(n4), Direction.UP)) continue;
            break;
        }
        if (n4 <= n3) {
            return Optional.empty();
        }
        object = new BlockPos(n, n4, n2);
        return Optional.of(new Structure.GenerationStub((BlockPos)object, arg_0 -> NetherFossilStructure.lambda$findGenerationPoint$2(generationContext, worldgenRandom, (BlockPos)object, arg_0)));
    }

    @Override
    public StructureType<?> type() {
        return StructureType.NETHER_FOSSIL;
    }

    private static /* synthetic */ void lambda$findGenerationPoint$2(Structure.GenerationContext generationContext, WorldgenRandom worldgenRandom, BlockPos blockPos, StructurePiecesBuilder structurePiecesBuilder) {
        NetherFossilPieces.addPieces(generationContext.structureTemplateManager(), structurePiecesBuilder, worldgenRandom, blockPos);
    }
}

