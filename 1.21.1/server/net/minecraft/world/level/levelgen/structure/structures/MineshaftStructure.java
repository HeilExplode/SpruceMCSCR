/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.structures;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;

public class MineshaftStructure
extends Structure {
    public static final MapCodec<MineshaftStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(MineshaftStructure.settingsCodec(instance), (App)Type.CODEC.fieldOf("mineshaft_type").forGetter(mineshaftStructure -> mineshaftStructure.type)).apply((Applicative)instance, MineshaftStructure::new));
    private final Type type;

    public MineshaftStructure(Structure.StructureSettings structureSettings, Type type) {
        super(structureSettings);
        this.type = type;
    }

    @Override
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext generationContext) {
        generationContext.random().nextDouble();
        ChunkPos chunkPos = generationContext.chunkPos();
        BlockPos blockPos = new BlockPos(chunkPos.getMiddleBlockX(), 50, chunkPos.getMinBlockZ());
        StructurePiecesBuilder structurePiecesBuilder = new StructurePiecesBuilder();
        int n = this.generatePiecesAndAdjust(structurePiecesBuilder, generationContext);
        return Optional.of(new Structure.GenerationStub(blockPos.offset(0, n, 0), (Either<Consumer<StructurePiecesBuilder>, StructurePiecesBuilder>)Either.right((Object)structurePiecesBuilder)));
    }

    private int generatePiecesAndAdjust(StructurePiecesBuilder structurePiecesBuilder, Structure.GenerationContext generationContext) {
        ChunkPos chunkPos = generationContext.chunkPos();
        WorldgenRandom worldgenRandom = generationContext.random();
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        MineshaftPieces.MineShaftRoom mineShaftRoom = new MineshaftPieces.MineShaftRoom(0, worldgenRandom, chunkPos.getBlockX(2), chunkPos.getBlockZ(2), this.type);
        structurePiecesBuilder.addPiece(mineShaftRoom);
        mineShaftRoom.addChildren(mineShaftRoom, structurePiecesBuilder, worldgenRandom);
        int n = chunkGenerator.getSeaLevel();
        if (this.type == Type.MESA) {
            BlockPos blockPos = structurePiecesBuilder.getBoundingBox().getCenter();
            int n2 = chunkGenerator.getBaseHeight(blockPos.getX(), blockPos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, generationContext.heightAccessor(), generationContext.randomState());
            int n3 = n2 <= n ? n : Mth.randomBetweenInclusive(worldgenRandom, n, n2);
            int n4 = n3 - blockPos.getY();
            structurePiecesBuilder.offsetPiecesVertically(n4);
            return n4;
        }
        return structurePiecesBuilder.moveBelowSeaLevel(n, chunkGenerator.getMinY(), worldgenRandom, 10);
    }

    @Override
    public StructureType<?> type() {
        return StructureType.MINESHAFT;
    }

    public static enum Type implements StringRepresentable
    {
        NORMAL("normal", Blocks.OAK_LOG, Blocks.OAK_PLANKS, Blocks.OAK_FENCE),
        MESA("mesa", Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_PLANKS, Blocks.DARK_OAK_FENCE);

        public static final Codec<Type> CODEC;
        private static final IntFunction<Type> BY_ID;
        private final String name;
        private final BlockState woodState;
        private final BlockState planksState;
        private final BlockState fenceState;

        private Type(String string2, Block block, Block block2, Block block3) {
            this.name = string2;
            this.woodState = block.defaultBlockState();
            this.planksState = block2.defaultBlockState();
            this.fenceState = block3.defaultBlockState();
        }

        public String getName() {
            return this.name;
        }

        public static Type byId(int n) {
            return BY_ID.apply(n);
        }

        public BlockState getWoodState() {
            return this.woodState;
        }

        public BlockState getPlanksState() {
            return this.planksState;
        }

        public BlockState getFenceState() {
            return this.fenceState;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        static {
            CODEC = StringRepresentable.fromEnum(Type::values);
            BY_ID = ByIdMap.continuous(Enum::ordinal, Type.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        }
    }
}

