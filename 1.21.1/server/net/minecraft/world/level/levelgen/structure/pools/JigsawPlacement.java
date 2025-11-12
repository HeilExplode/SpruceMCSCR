/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.SequencedPriorityIterator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class JigsawPlacement {
    static final Logger LOGGER = LogUtils.getLogger();

    public static Optional<Structure.GenerationStub> addPieces(Structure.GenerationContext generationContext, Holder<StructureTemplatePool> holder, Optional<ResourceLocation> optional, int n, BlockPos blockPos, boolean bl, Optional<Heightmap.Types> optional2, int n2, PoolAliasLookup poolAliasLookup, DimensionPadding dimensionPadding, LiquidSettings liquidSettings) {
        BlockPos blockPos2;
        Optional<BlockPos> optional3;
        Comparable<ResourceLocation> comparable;
        RegistryAccess registryAccess = generationContext.registryAccess();
        ChunkGenerator chunkGenerator = generationContext.chunkGenerator();
        StructureTemplateManager structureTemplateManager = generationContext.structureTemplateManager();
        LevelHeightAccessor levelHeightAccessor = generationContext.heightAccessor();
        WorldgenRandom worldgenRandom = generationContext.random();
        Registry<StructureTemplatePool> registry = registryAccess.registryOrThrow(Registries.TEMPLATE_POOL);
        Rotation rotation = Rotation.getRandom(worldgenRandom);
        StructureTemplatePool structureTemplatePool = holder.unwrapKey().flatMap(resourceKey -> registry.getOptional(poolAliasLookup.lookup((ResourceKey<StructureTemplatePool>)resourceKey))).orElse(holder.value());
        StructurePoolElement structurePoolElement = structureTemplatePool.getRandomTemplate(worldgenRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        if (optional.isPresent()) {
            comparable = optional.get();
            optional3 = JigsawPlacement.getRandomNamedJigsaw(structurePoolElement, (ResourceLocation)comparable, blockPos, rotation, structureTemplateManager, worldgenRandom);
            if (optional3.isEmpty()) {
                LOGGER.error("No starting jigsaw {} found in start pool {}", (Object)comparable, (Object)holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unregistered>"));
                return Optional.empty();
            }
            blockPos2 = (BlockPos)optional3.get();
        } else {
            blockPos2 = blockPos;
        }
        comparable = blockPos2.subtract(blockPos);
        optional3 = blockPos.subtract((Vec3i)comparable);
        PoolElementStructurePiece poolElementStructurePiece = new PoolElementStructurePiece(structureTemplateManager, structurePoolElement, (BlockPos)((Object)optional3), structurePoolElement.getGroundLevelDelta(), rotation, structurePoolElement.getBoundingBox(structureTemplateManager, (BlockPos)((Object)optional3), rotation), liquidSettings);
        BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
        int n3 = (boundingBox.maxX() + boundingBox.minX()) / 2;
        int n4 = (boundingBox.maxZ() + boundingBox.minZ()) / 2;
        int n5 = optional2.isPresent() ? blockPos.getY() + chunkGenerator.getFirstFreeHeight(n3, n4, optional2.get(), levelHeightAccessor, generationContext.randomState()) : ((Vec3i)((Object)optional3)).getY();
        int n6 = boundingBox.minY() + poolElementStructurePiece.getGroundLevelDelta();
        poolElementStructurePiece.move(0, n5 - n6, 0);
        int n7 = n5 + ((Vec3i)comparable).getY();
        return Optional.of(new Structure.GenerationStub(new BlockPos(n3, n7, n4), structurePiecesBuilder -> {
            ArrayList arrayList = Lists.newArrayList();
            arrayList.add(poolElementStructurePiece);
            if (n <= 0) {
                return;
            }
            AABB aABB = new AABB(n3 - n2, Math.max(n7 - n2, levelHeightAccessor.getMinBuildHeight() + dimensionPadding.bottom()), n4 - n2, n3 + n2 + 1, Math.min(n7 + n2 + 1, levelHeightAccessor.getMaxBuildHeight() - dimensionPadding.top()), n4 + n2 + 1);
            VoxelShape voxelShape = Shapes.join(Shapes.create(aABB), Shapes.create(AABB.of(boundingBox)), BooleanOp.ONLY_FIRST);
            JigsawPlacement.addPieces(generationContext.randomState(), n, bl, chunkGenerator, structureTemplateManager, levelHeightAccessor, worldgenRandom, registry, poolElementStructurePiece, arrayList, voxelShape, poolAliasLookup, liquidSettings);
            arrayList.forEach(structurePiecesBuilder::addPiece);
        }));
    }

    private static Optional<BlockPos> getRandomNamedJigsaw(StructurePoolElement structurePoolElement, ResourceLocation resourceLocation, BlockPos blockPos, Rotation rotation, StructureTemplateManager structureTemplateManager, WorldgenRandom worldgenRandom) {
        List<StructureTemplate.StructureBlockInfo> list = structurePoolElement.getShuffledJigsawBlocks(structureTemplateManager, blockPos, rotation, worldgenRandom);
        Optional<BlockPos> optional = Optional.empty();
        for (StructureTemplate.StructureBlockInfo structureBlockInfo : list) {
            ResourceLocation resourceLocation2 = ResourceLocation.tryParse(Objects.requireNonNull(structureBlockInfo.nbt(), () -> String.valueOf(structureBlockInfo) + " nbt was null").getString("name"));
            if (!resourceLocation.equals(resourceLocation2)) continue;
            optional = Optional.of(structureBlockInfo.pos());
            break;
        }
        return optional;
    }

    private static void addPieces(RandomState randomState, int n, boolean bl, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, LevelHeightAccessor levelHeightAccessor, RandomSource randomSource, Registry<StructureTemplatePool> registry, PoolElementStructurePiece poolElementStructurePiece, List<PoolElementStructurePiece> list, VoxelShape voxelShape, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings) {
        Placer placer = new Placer(registry, n, chunkGenerator, structureTemplateManager, list, randomSource);
        placer.tryPlacingChildren(poolElementStructurePiece, (MutableObject<VoxelShape>)new MutableObject((Object)voxelShape), 0, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
        while (placer.placing.hasNext()) {
            PieceState pieceState = (PieceState)placer.placing.next();
            placer.tryPlacingChildren(pieceState.piece, pieceState.free, pieceState.depth, bl, levelHeightAccessor, randomState, poolAliasLookup, liquidSettings);
        }
    }

    public static boolean generateJigsaw(ServerLevel serverLevel, Holder<StructureTemplatePool> holder2, ResourceLocation resourceLocation, int n, BlockPos blockPos, boolean bl) {
        ChunkGenerator chunkGenerator = serverLevel.getChunkSource().getGenerator();
        StructureTemplateManager structureTemplateManager = serverLevel.getStructureManager();
        StructureManager structureManager = serverLevel.structureManager();
        RandomSource randomSource = serverLevel.getRandom();
        Structure.GenerationContext generationContext = new Structure.GenerationContext(serverLevel.registryAccess(), chunkGenerator, chunkGenerator.getBiomeSource(), serverLevel.getChunkSource().randomState(), structureTemplateManager, serverLevel.getSeed(), new ChunkPos(blockPos), serverLevel, holder -> true);
        Optional<Structure.GenerationStub> optional = JigsawPlacement.addPieces(generationContext, holder2, Optional.of(resourceLocation), n, blockPos, false, Optional.empty(), 128, PoolAliasLookup.EMPTY, JigsawStructure.DEFAULT_DIMENSION_PADDING, JigsawStructure.DEFAULT_LIQUID_SETTINGS);
        if (optional.isPresent()) {
            StructurePiecesBuilder structurePiecesBuilder = optional.get().getPiecesBuilder();
            for (StructurePiece structurePiece : structurePiecesBuilder.build().pieces()) {
                if (!(structurePiece instanceof PoolElementStructurePiece)) continue;
                PoolElementStructurePiece poolElementStructurePiece = (PoolElementStructurePiece)structurePiece;
                poolElementStructurePiece.place(serverLevel, structureManager, chunkGenerator, randomSource, BoundingBox.infinite(), blockPos, bl);
            }
            return true;
        }
        return false;
    }

    static final class Placer {
        private final Registry<StructureTemplatePool> pools;
        private final int maxDepth;
        private final ChunkGenerator chunkGenerator;
        private final StructureTemplateManager structureTemplateManager;
        private final List<? super PoolElementStructurePiece> pieces;
        private final RandomSource random;
        final SequencedPriorityIterator<PieceState> placing = new SequencedPriorityIterator();

        Placer(Registry<StructureTemplatePool> registry, int n, ChunkGenerator chunkGenerator, StructureTemplateManager structureTemplateManager, List<? super PoolElementStructurePiece> list, RandomSource randomSource) {
            this.pools = registry;
            this.maxDepth = n;
            this.chunkGenerator = chunkGenerator;
            this.structureTemplateManager = structureTemplateManager;
            this.pieces = list;
            this.random = randomSource;
        }

        void tryPlacingChildren(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int n, boolean bl, LevelHeightAccessor levelHeightAccessor, RandomState randomState, PoolAliasLookup poolAliasLookup, LiquidSettings liquidSettings) {
            StructurePoolElement structurePoolElement = poolElementStructurePiece.getElement();
            BlockPos blockPos = poolElementStructurePiece.getPosition();
            Rotation rotation = poolElementStructurePiece.getRotation();
            StructureTemplatePool.Projection projection = structurePoolElement.getProjection();
            boolean bl2 = projection == StructureTemplatePool.Projection.RIGID;
            MutableObject<VoxelShape> mutableObject2 = new MutableObject<VoxelShape>();
            BoundingBox boundingBox = poolElementStructurePiece.getBoundingBox();
            int n2 = boundingBox.minY();
            block0: for (StructureTemplate.StructureBlockInfo structureBlockInfo2 : structurePoolElement.getShuffledJigsawBlocks(this.structureTemplateManager, blockPos, rotation, this.random)) {
                StructurePoolElement structurePoolElement2;
                MutableObject<VoxelShape> mutableObject3;
                Direction direction = JigsawBlock.getFrontFacing(structureBlockInfo2.state());
                BlockPos blockPos2 = structureBlockInfo2.pos();
                BlockPos blockPos3 = blockPos2.relative(direction);
                int n3 = blockPos2.getY() - n2;
                int n4 = -1;
                ResourceKey<StructureTemplatePool> resourceKey2 = Placer.readPoolKey(structureBlockInfo2, poolAliasLookup);
                Optional<Holder.Reference<StructureTemplatePool>> optional = this.pools.getHolder(resourceKey2);
                if (optional.isEmpty()) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceKey2.location());
                    continue;
                }
                Holder holder = optional.get();
                if (((StructureTemplatePool)holder.value()).size() == 0 && !holder.is(Pools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent pool: {}", (Object)resourceKey2.location());
                    continue;
                }
                Holder<StructureTemplatePool> holder2 = ((StructureTemplatePool)holder.value()).getFallback();
                if (holder2.value().size() == 0 && !holder2.is(Pools.EMPTY)) {
                    LOGGER.warn("Empty or non-existent fallback pool: {}", (Object)holder2.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unregistered>"));
                    continue;
                }
                boolean bl3 = boundingBox.isInside(blockPos3);
                if (bl3) {
                    mutableObject3 = mutableObject2;
                    if (mutableObject2.getValue() == null) {
                        mutableObject2.setValue((Object)Shapes.create(AABB.of(boundingBox)));
                    }
                } else {
                    mutableObject3 = mutableObject;
                }
                ArrayList arrayList = Lists.newArrayList();
                if (n != this.maxDepth) {
                    arrayList.addAll(((StructureTemplatePool)holder.value()).getShuffledTemplates(this.random));
                }
                arrayList.addAll(holder2.value().getShuffledTemplates(this.random));
                int n5 = structureBlockInfo2.nbt() != null ? structureBlockInfo2.nbt().getInt("placement_priority") : 0;
                Iterator iterator = arrayList.iterator();
                while (iterator.hasNext() && (structurePoolElement2 = (StructurePoolElement)iterator.next()) != EmptyPoolElement.INSTANCE) {
                    for (Rotation rotation2 : Rotation.getShuffled(this.random)) {
                        List<StructureTemplate.StructureBlockInfo> list = structurePoolElement2.getShuffledJigsawBlocks(this.structureTemplateManager, BlockPos.ZERO, rotation2, this.random);
                        BoundingBox boundingBox2 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, BlockPos.ZERO, rotation2);
                        int n6 = !bl || boundingBox2.getYSpan() > 16 ? 0 : list.stream().mapToInt(structureBlockInfo -> {
                            if (!boundingBox2.isInside(structureBlockInfo.pos().relative(JigsawBlock.getFrontFacing(structureBlockInfo.state())))) {
                                return 0;
                            }
                            ResourceKey<StructureTemplatePool> resourceKey = Placer.readPoolKey(structureBlockInfo, poolAliasLookup);
                            Optional<Holder.Reference<StructureTemplatePool>> optional = this.pools.getHolder(resourceKey);
                            Optional<Holder> optional2 = optional.map(holder -> ((StructureTemplatePool)holder.value()).getFallback());
                            int n = optional.map(holder -> ((StructureTemplatePool)holder.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
                            int n2 = optional2.map(holder -> ((StructureTemplatePool)holder.value()).getMaxSize(this.structureTemplateManager)).orElse(0);
                            return Math.max(n, n2);
                        }).max().orElse(0);
                        for (StructureTemplate.StructureBlockInfo structureBlockInfo3 : list) {
                            int n7;
                            int n8;
                            int n9;
                            if (!JigsawBlock.canAttach(structureBlockInfo2, structureBlockInfo3)) continue;
                            BlockPos blockPos4 = structureBlockInfo3.pos();
                            BlockPos blockPos5 = blockPos3.subtract(blockPos4);
                            BoundingBox boundingBox3 = structurePoolElement2.getBoundingBox(this.structureTemplateManager, blockPos5, rotation2);
                            int n10 = boundingBox3.minY();
                            StructureTemplatePool.Projection projection2 = structurePoolElement2.getProjection();
                            boolean bl4 = projection2 == StructureTemplatePool.Projection.RIGID;
                            int n11 = blockPos4.getY();
                            int n12 = n3 - n11 + JigsawBlock.getFrontFacing(structureBlockInfo2.state()).getStepY();
                            if (bl2 && bl4) {
                                n9 = n2 + n12;
                            } else {
                                if (n4 == -1) {
                                    n4 = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
                                }
                                n9 = n4 - n11;
                            }
                            int n13 = n9 - n10;
                            BoundingBox boundingBox4 = boundingBox3.moved(0, n13, 0);
                            BlockPos blockPos6 = blockPos5.offset(0, n13, 0);
                            if (n6 > 0) {
                                n8 = Math.max(n6 + 1, boundingBox4.maxY() - boundingBox4.minY());
                                boundingBox4.encapsulate(new BlockPos(boundingBox4.minX(), boundingBox4.minY() + n8, boundingBox4.minZ()));
                            }
                            if (Shapes.joinIsNotEmpty((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4).deflate(0.25)), BooleanOp.ONLY_SECOND)) continue;
                            mutableObject3.setValue((Object)Shapes.joinUnoptimized((VoxelShape)mutableObject3.getValue(), Shapes.create(AABB.of(boundingBox4)), BooleanOp.ONLY_FIRST));
                            n8 = poolElementStructurePiece.getGroundLevelDelta();
                            int n14 = bl4 ? n8 - n12 : structurePoolElement2.getGroundLevelDelta();
                            PoolElementStructurePiece poolElementStructurePiece2 = new PoolElementStructurePiece(this.structureTemplateManager, structurePoolElement2, blockPos6, n14, rotation2, boundingBox4, liquidSettings);
                            if (bl2) {
                                n7 = n2 + n3;
                            } else if (bl4) {
                                n7 = n9 + n11;
                            } else {
                                if (n4 == -1) {
                                    n4 = this.chunkGenerator.getFirstFreeHeight(blockPos2.getX(), blockPos2.getZ(), Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
                                }
                                n7 = n4 + n12 / 2;
                            }
                            poolElementStructurePiece.addJunction(new JigsawJunction(blockPos3.getX(), n7 - n3 + n8, blockPos3.getZ(), n12, projection2));
                            poolElementStructurePiece2.addJunction(new JigsawJunction(blockPos2.getX(), n7 - n11 + n14, blockPos2.getZ(), -n12, projection));
                            this.pieces.add(poolElementStructurePiece2);
                            if (n + 1 > this.maxDepth) continue block0;
                            PieceState pieceState = new PieceState(poolElementStructurePiece2, mutableObject3, n + 1);
                            this.placing.add(pieceState, n5);
                            continue block0;
                        }
                    }
                }
            }
        }

        private static ResourceKey<StructureTemplatePool> readPoolKey(StructureTemplate.StructureBlockInfo structureBlockInfo, PoolAliasLookup poolAliasLookup) {
            CompoundTag compoundTag = Objects.requireNonNull(structureBlockInfo.nbt(), () -> String.valueOf(structureBlockInfo) + " nbt was null");
            ResourceKey<StructureTemplatePool> resourceKey = Pools.parseKey(compoundTag.getString("pool"));
            return poolAliasLookup.lookup(resourceKey);
        }
    }

    static final class PieceState
    extends Record {
        final PoolElementStructurePiece piece;
        final MutableObject<VoxelShape> free;
        final int depth;

        PieceState(PoolElementStructurePiece poolElementStructurePiece, MutableObject<VoxelShape> mutableObject, int n) {
            this.piece = poolElementStructurePiece;
            this.free = mutableObject;
            this.depth = n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PieceState.class, "piece;free;depth", "piece", "free", "depth"}, this, object);
        }

        public PoolElementStructurePiece piece() {
            return this.piece;
        }

        public MutableObject<VoxelShape> free() {
            return this.free;
        }

        public int depth() {
            return this.depth;
        }
    }
}

