/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

public class StructureTemplate {
    public static final String PALETTE_TAG = "palette";
    public static final String PALETTE_LIST_TAG = "palettes";
    public static final String ENTITIES_TAG = "entities";
    public static final String BLOCKS_TAG = "blocks";
    public static final String BLOCK_TAG_POS = "pos";
    public static final String BLOCK_TAG_STATE = "state";
    public static final String BLOCK_TAG_NBT = "nbt";
    public static final String ENTITY_TAG_POS = "pos";
    public static final String ENTITY_TAG_BLOCKPOS = "blockPos";
    public static final String ENTITY_TAG_NBT = "nbt";
    public static final String SIZE_TAG = "size";
    private final List<Palette> palettes = Lists.newArrayList();
    private final List<StructureEntityInfo> entityInfoList = Lists.newArrayList();
    private Vec3i size = Vec3i.ZERO;
    private String author = "?";

    public Vec3i getSize() {
        return this.size;
    }

    public void setAuthor(String string) {
        this.author = string;
    }

    public String getAuthor() {
        return this.author;
    }

    public void fillFromWorld(Level level, BlockPos blockPos, Vec3i vec3i, boolean bl, @Nullable Block block) {
        if (vec3i.getX() < 1 || vec3i.getY() < 1 || vec3i.getZ() < 1) {
            return;
        }
        BlockPos blockPos2 = blockPos.offset(vec3i).offset(-1, -1, -1);
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        ArrayList arrayList3 = Lists.newArrayList();
        BlockPos blockPos3 = new BlockPos(Math.min(blockPos.getX(), blockPos2.getX()), Math.min(blockPos.getY(), blockPos2.getY()), Math.min(blockPos.getZ(), blockPos2.getZ()));
        BlockPos blockPos4 = new BlockPos(Math.max(blockPos.getX(), blockPos2.getX()), Math.max(blockPos.getY(), blockPos2.getY()), Math.max(blockPos.getZ(), blockPos2.getZ()));
        this.size = vec3i;
        for (BlockPos blockPos5 : BlockPos.betweenClosed(blockPos3, blockPos4)) {
            BlockPos blockPos6 = blockPos5.subtract(blockPos3);
            BlockState blockState = level.getBlockState(blockPos5);
            if (block != null && blockState.is(block)) continue;
            BlockEntity blockEntity = level.getBlockEntity(blockPos5);
            StructureBlockInfo structureBlockInfo = blockEntity != null ? new StructureBlockInfo(blockPos6, blockState, blockEntity.saveWithId(level.registryAccess())) : new StructureBlockInfo(blockPos6, blockState, null);
            StructureTemplate.addToLists(structureBlockInfo, arrayList, arrayList2, arrayList3);
        }
        List<StructureBlockInfo> list = StructureTemplate.buildInfoList(arrayList, arrayList2, arrayList3);
        this.palettes.clear();
        this.palettes.add(new Palette(list));
        if (bl) {
            this.fillEntityList(level, blockPos3, blockPos4);
        } else {
            this.entityInfoList.clear();
        }
    }

    private static void addToLists(StructureBlockInfo structureBlockInfo, List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        if (structureBlockInfo.nbt != null) {
            list2.add(structureBlockInfo);
        } else if (!structureBlockInfo.state.getBlock().hasDynamicShape() && structureBlockInfo.state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)) {
            list.add(structureBlockInfo);
        } else {
            list3.add(structureBlockInfo);
        }
    }

    private static List<StructureBlockInfo> buildInfoList(List<StructureBlockInfo> list, List<StructureBlockInfo> list2, List<StructureBlockInfo> list3) {
        Comparator<StructureBlockInfo> comparator = Comparator.comparingInt(structureBlockInfo -> structureBlockInfo.pos.getY()).thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getX()).thenComparingInt(structureBlockInfo -> structureBlockInfo.pos.getZ());
        list.sort(comparator);
        list3.sort(comparator);
        list2.sort(comparator);
        ArrayList arrayList = Lists.newArrayList();
        arrayList.addAll(list);
        arrayList.addAll(list3);
        arrayList.addAll(list2);
        return arrayList;
    }

    private void fillEntityList(Level level, BlockPos blockPos, BlockPos blockPos2) {
        List<Entity> list = level.getEntitiesOfClass(Entity.class, AABB.encapsulatingFullBlocks(blockPos, blockPos2), entity -> !(entity instanceof Player));
        this.entityInfoList.clear();
        for (Entity entity2 : list) {
            Vec3 vec3 = new Vec3(entity2.getX() - (double)blockPos.getX(), entity2.getY() - (double)blockPos.getY(), entity2.getZ() - (double)blockPos.getZ());
            CompoundTag compoundTag = new CompoundTag();
            entity2.save(compoundTag);
            BlockPos blockPos3 = entity2 instanceof Painting ? ((Painting)entity2).getPos().subtract(blockPos) : BlockPos.containing(vec3);
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos3, compoundTag.copy()));
        }
    }

    public List<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block) {
        return this.filterBlocks(blockPos, structurePlaceSettings, block, true);
    }

    public ObjectArrayList<StructureBlockInfo> filterBlocks(BlockPos blockPos, StructurePlaceSettings structurePlaceSettings, Block block, boolean bl) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        if (this.palettes.isEmpty()) {
            return objectArrayList;
        }
        for (StructureBlockInfo structureBlockInfo : structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks(block)) {
            BlockPos blockPos2;
            BlockPos blockPos3 = blockPos2 = bl ? StructureTemplate.calculateRelativePosition(structurePlaceSettings, structureBlockInfo.pos).offset(blockPos) : structureBlockInfo.pos;
            if (boundingBox != null && !boundingBox.isInside(blockPos2)) continue;
            objectArrayList.add((Object)new StructureBlockInfo(blockPos2, structureBlockInfo.state.rotate(structurePlaceSettings.getRotation()), structureBlockInfo.nbt));
        }
        return objectArrayList;
    }

    public BlockPos calculateConnectedPosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos, StructurePlaceSettings structurePlaceSettings2, BlockPos blockPos2) {
        BlockPos blockPos3 = StructureTemplate.calculateRelativePosition(structurePlaceSettings, blockPos);
        BlockPos blockPos4 = StructureTemplate.calculateRelativePosition(structurePlaceSettings2, blockPos2);
        return blockPos3.subtract(blockPos4);
    }

    public static BlockPos calculateRelativePosition(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return StructureTemplate.transform(blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot());
    }

    public boolean placeInWorld(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, RandomSource randomSource, int n) {
        FluidState fluidState;
        Object object;
        StateHolder n9;
        Object n8;
        Object object2;
        if (this.palettes.isEmpty()) {
            return false;
        }
        List<StructureBlockInfo> list = structurePlaceSettings.getRandomPalette(this.palettes, blockPos).blocks();
        if (list.isEmpty() && (structurePlaceSettings.isIgnoreEntities() || this.entityInfoList.isEmpty()) || this.size.getX() < 1 || this.size.getY() < 1 || this.size.getZ() < 1) {
            return false;
        }
        BoundingBox boundingBox = structurePlaceSettings.getBoundingBox();
        ArrayList arrayList = Lists.newArrayListWithCapacity((int)(structurePlaceSettings.shouldApplyWaterlogging() ? list.size() : 0));
        ArrayList arrayList2 = Lists.newArrayListWithCapacity((int)(structurePlaceSettings.shouldApplyWaterlogging() ? list.size() : 0));
        ArrayList arrayList3 = Lists.newArrayListWithCapacity((int)list.size());
        int n2 = Integer.MAX_VALUE;
        int n3 = Integer.MAX_VALUE;
        int n4 = Integer.MAX_VALUE;
        int n5 = Integer.MIN_VALUE;
        int n6 = Integer.MIN_VALUE;
        int n7 = Integer.MIN_VALUE;
        List<StructureBlockInfo> list2 = StructureTemplate.processBlockInfos(serverLevelAccessor, blockPos, blockPos2, structurePlaceSettings, list);
        for (StructureBlockInfo directionArray2 : list2) {
            BlockEntity blockEntity;
            object2 = directionArray2.pos;
            if (boundingBox != null && !boundingBox.isInside((Vec3i)object2)) continue;
            n8 = structurePlaceSettings.shouldApplyWaterlogging() ? serverLevelAccessor.getFluidState((BlockPos)object2) : null;
            n9 = directionArray2.state.mirror(structurePlaceSettings.getMirror()).rotate(structurePlaceSettings.getRotation());
            if (directionArray2.nbt != null) {
                blockEntity = serverLevelAccessor.getBlockEntity((BlockPos)object2);
                Clearable.tryClear(blockEntity);
                serverLevelAccessor.setBlock((BlockPos)object2, Blocks.BARRIER.defaultBlockState(), 20);
            }
            if (!serverLevelAccessor.setBlock((BlockPos)object2, (BlockState)n9, n)) continue;
            n2 = Math.min(n2, ((Vec3i)object2).getX());
            n3 = Math.min(n3, ((Vec3i)object2).getY());
            n4 = Math.min(n4, ((Vec3i)object2).getZ());
            n5 = Math.max(n5, ((Vec3i)object2).getX());
            n6 = Math.max(n6, ((Vec3i)object2).getY());
            n7 = Math.max(n7, ((Vec3i)object2).getZ());
            arrayList3.add(Pair.of((Object)object2, (Object)directionArray2.nbt));
            if (directionArray2.nbt != null && (blockEntity = serverLevelAccessor.getBlockEntity((BlockPos)object2)) != null) {
                if (blockEntity instanceof RandomizableContainer) {
                    directionArray2.nbt.putLong("LootTableSeed", randomSource.nextLong());
                }
                blockEntity.loadWithComponents(directionArray2.nbt, serverLevelAccessor.registryAccess());
            }
            if (n8 == null) continue;
            if (((BlockBehaviour.BlockStateBase)n9).getFluidState().isSource()) {
                arrayList2.add(object2);
                continue;
            }
            if (!(((BlockBehaviour.BlockStateBase)n9).getBlock() instanceof LiquidBlockContainer)) continue;
            ((LiquidBlockContainer)((Object)((BlockBehaviour.BlockStateBase)n9).getBlock())).placeLiquid(serverLevelAccessor, (BlockPos)object2, (BlockState)n9, (FluidState)n8);
            if (((FluidState)n8).isSource()) continue;
            arrayList.add(object2);
        }
        boolean bl = true;
        Direction[] directionArray = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        while (bl && !arrayList.isEmpty()) {
            bl = false;
            object2 = arrayList.iterator();
            while (object2.hasNext()) {
                BlockState n10;
                n8 = (BlockPos)object2.next();
                n9 = serverLevelAccessor.getFluidState((BlockPos)n8);
                for (int blockState = 0; blockState < directionArray.length && !((FluidState)n9).isSource(); ++blockState) {
                    object = ((BlockPos)n8).relative(directionArray[blockState]);
                    fluidState = serverLevelAccessor.getFluidState((BlockPos)object);
                    if (!fluidState.isSource() || arrayList2.contains(object)) continue;
                    n9 = fluidState;
                }
                if (!((FluidState)n9).isSource() || !((object = (n10 = serverLevelAccessor.getBlockState((BlockPos)n8)).getBlock()) instanceof LiquidBlockContainer)) continue;
                ((LiquidBlockContainer)object).placeLiquid(serverLevelAccessor, (BlockPos)n8, n10, (FluidState)n9);
                bl = true;
                object2.remove();
            }
        }
        if (n2 <= n5) {
            if (!structurePlaceSettings.getKnownShape()) {
                object2 = new BitSetDiscreteVoxelShape(n5 - n2 + 1, n6 - n3 + 1, n7 - n4 + 1);
                int pair = n2;
                int blockPos4 = n3;
                int blockState = n4;
                object = arrayList3.iterator();
                while (object.hasNext()) {
                    fluidState = (Pair)object.next();
                    BlockPos blockPos3 = (BlockPos)fluidState.getFirst();
                    ((DiscreteVoxelShape)object2).fill(blockPos3.getX() - pair, blockPos3.getY() - blockPos4, blockPos3.getZ() - blockState);
                }
                StructureTemplate.updateShapeAtEdge(serverLevelAccessor, n, (DiscreteVoxelShape)object2, pair, blockPos4, blockState);
            }
            for (Pair pair : arrayList3) {
                BlockEntity blockEntity;
                BlockPos blockPos4 = (BlockPos)pair.getFirst();
                if (!structurePlaceSettings.getKnownShape()) {
                    BlockState blockEntity2 = serverLevelAccessor.getBlockState(blockPos4);
                    if (blockEntity2 != (object = Block.updateFromNeighbourShapes(blockEntity2, serverLevelAccessor, blockPos4))) {
                        serverLevelAccessor.setBlock(blockPos4, (BlockState)object, n & 0xFFFFFFFE | 0x10);
                    }
                    serverLevelAccessor.blockUpdated(blockPos4, ((BlockBehaviour.BlockStateBase)object).getBlock());
                }
                if (pair.getSecond() == null || (blockEntity = serverLevelAccessor.getBlockEntity(blockPos4)) == null) continue;
                blockEntity.setChanged();
            }
        }
        if (!structurePlaceSettings.isIgnoreEntities()) {
            this.placeEntities(serverLevelAccessor, blockPos, structurePlaceSettings.getMirror(), structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), boundingBox, structurePlaceSettings.shouldFinalizeEntities());
        }
        return true;
    }

    public static void updateShapeAtEdge(LevelAccessor levelAccessor, int n, DiscreteVoxelShape discreteVoxelShape, BlockPos blockPos) {
        StructureTemplate.updateShapeAtEdge(levelAccessor, n, discreteVoxelShape, blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public static void updateShapeAtEdge(LevelAccessor levelAccessor, int n, DiscreteVoxelShape discreteVoxelShape, int n2, int n3, int n4) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos mutableBlockPos2 = new BlockPos.MutableBlockPos();
        discreteVoxelShape.forAllFaces((direction, n5, n6, n7) -> {
            BlockState blockState;
            mutableBlockPos.set(n2 + n5, n3 + n6, n4 + n7);
            mutableBlockPos2.setWithOffset((Vec3i)mutableBlockPos, direction);
            BlockState blockState2 = levelAccessor.getBlockState(mutableBlockPos);
            BlockState blockState3 = levelAccessor.getBlockState(mutableBlockPos2);
            BlockState blockState4 = blockState2.updateShape(direction, blockState3, levelAccessor, mutableBlockPos, mutableBlockPos2);
            if (blockState2 != blockState4) {
                levelAccessor.setBlock(mutableBlockPos, blockState4, n & 0xFFFFFFFE);
            }
            if (blockState3 != (blockState = blockState3.updateShape(direction.getOpposite(), blockState4, levelAccessor, mutableBlockPos2, mutableBlockPos))) {
                levelAccessor.setBlock(mutableBlockPos2, blockState, n & 0xFFFFFFFE);
            }
        });
    }

    public static List<StructureBlockInfo> processBlockInfos(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, BlockPos blockPos2, StructurePlaceSettings structurePlaceSettings, List<StructureBlockInfo> list) {
        ArrayList<StructureBlockInfo> arrayList = new ArrayList<StructureBlockInfo>();
        List<StructureBlockInfo> list2 = new ArrayList<StructureBlockInfo>();
        for (StructureBlockInfo object : list) {
            BlockPos blockPos3 = StructureTemplate.calculateRelativePosition(structurePlaceSettings, object.pos).offset(blockPos);
            StructureBlockInfo structureBlockInfo = new StructureBlockInfo(blockPos3, object.state, object.nbt != null ? object.nbt.copy() : null);
            Iterator<StructureProcessor> iterator = structurePlaceSettings.getProcessors().iterator();
            while (structureBlockInfo != null && iterator.hasNext()) {
                structureBlockInfo = iterator.next().processBlock(serverLevelAccessor, blockPos, blockPos2, object, structureBlockInfo, structurePlaceSettings);
            }
            if (structureBlockInfo == null) continue;
            list2.add(structureBlockInfo);
            arrayList.add(object);
        }
        for (StructureProcessor structureProcessor : structurePlaceSettings.getProcessors()) {
            list2 = structureProcessor.finalizeProcessing(serverLevelAccessor, blockPos, blockPos2, arrayList, list2, structurePlaceSettings);
        }
        return list2;
    }

    private void placeEntities(ServerLevelAccessor serverLevelAccessor, BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2, @Nullable BoundingBox boundingBox, boolean bl) {
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            BlockPos blockPos3 = StructureTemplate.transform(structureEntityInfo.blockPos, mirror, rotation, blockPos2).offset(blockPos);
            if (boundingBox != null && !boundingBox.isInside(blockPos3)) continue;
            CompoundTag compoundTag = structureEntityInfo.nbt.copy();
            Vec3 vec3 = StructureTemplate.transform(structureEntityInfo.pos, mirror, rotation, blockPos2);
            Vec3 vec32 = vec3.add(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            ListTag listTag = new ListTag();
            listTag.add(DoubleTag.valueOf(vec32.x));
            listTag.add(DoubleTag.valueOf(vec32.y));
            listTag.add(DoubleTag.valueOf(vec32.z));
            compoundTag.put("Pos", listTag);
            compoundTag.remove("UUID");
            StructureTemplate.createEntityIgnoreException(serverLevelAccessor, compoundTag).ifPresent(entity -> {
                float f = entity.rotate(rotation);
                entity.moveTo(vec3.x, vec3.y, vec3.z, f += entity.mirror(mirror) - entity.getYRot(), entity.getXRot());
                if (bl && entity instanceof Mob) {
                    ((Mob)entity).finalizeSpawn(serverLevelAccessor, serverLevelAccessor.getCurrentDifficultyAt(BlockPos.containing(vec32)), MobSpawnType.STRUCTURE, null);
                }
                serverLevelAccessor.addFreshEntityWithPassengers((Entity)entity);
            });
        }
    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor serverLevelAccessor, CompoundTag compoundTag) {
        try {
            return EntityType.create(compoundTag, serverLevelAccessor.getLevel());
        }
        catch (Exception exception) {
            return Optional.empty();
        }
    }

    public Vec3i getSize(Rotation rotation) {
        switch (rotation) {
            case COUNTERCLOCKWISE_90: 
            case CLOCKWISE_90: {
                return new Vec3i(this.size.getZ(), this.size.getY(), this.size.getX());
            }
        }
        return this.size;
    }

    public static BlockPos transform(BlockPos blockPos, Mirror mirror, Rotation rotation, BlockPos blockPos2) {
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                n3 = -n3;
                break;
            }
            case FRONT_BACK: {
                n = -n;
                break;
            }
            default: {
                bl = false;
            }
        }
        int n4 = blockPos2.getX();
        int n5 = blockPos2.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new BlockPos(n4 + n4 - n, n2, n5 + n5 - n3);
            }
            case COUNTERCLOCKWISE_90: {
                return new BlockPos(n4 - n5 + n3, n2, n4 + n5 - n);
            }
            case CLOCKWISE_90: {
                return new BlockPos(n4 + n5 - n3, n2, n5 - n4 + n);
            }
        }
        return bl ? new BlockPos(n, n2, n3) : blockPos;
    }

    public static Vec3 transform(Vec3 vec3, Mirror mirror, Rotation rotation, BlockPos blockPos) {
        double d = vec3.x;
        double d2 = vec3.y;
        double d3 = vec3.z;
        boolean bl = true;
        switch (mirror) {
            case LEFT_RIGHT: {
                d3 = 1.0 - d3;
                break;
            }
            case FRONT_BACK: {
                d = 1.0 - d;
                break;
            }
            default: {
                bl = false;
            }
        }
        int n = blockPos.getX();
        int n2 = blockPos.getZ();
        switch (rotation) {
            case CLOCKWISE_180: {
                return new Vec3((double)(n + n + 1) - d, d2, (double)(n2 + n2 + 1) - d3);
            }
            case COUNTERCLOCKWISE_90: {
                return new Vec3((double)(n - n2) + d3, d2, (double)(n + n2 + 1) - d);
            }
            case CLOCKWISE_90: {
                return new Vec3((double)(n + n2 + 1) - d3, d2, (double)(n2 - n) + d);
            }
        }
        return bl ? new Vec3(d, d2, d3) : vec3;
    }

    public BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation) {
        return StructureTemplate.getZeroPositionWithTransform(blockPos, mirror, rotation, this.getSize().getX(), this.getSize().getZ());
    }

    public static BlockPos getZeroPositionWithTransform(BlockPos blockPos, Mirror mirror, Rotation rotation, int n, int n2) {
        int n3 = mirror == Mirror.FRONT_BACK ? --n : 0;
        int n4 = mirror == Mirror.LEFT_RIGHT ? --n2 : 0;
        BlockPos blockPos2 = blockPos;
        switch (rotation) {
            case NONE: {
                blockPos2 = blockPos.offset(n3, 0, n4);
                break;
            }
            case CLOCKWISE_90: {
                blockPos2 = blockPos.offset(n2 - n4, 0, n3);
                break;
            }
            case CLOCKWISE_180: {
                blockPos2 = blockPos.offset(n - n3, 0, n2 - n4);
                break;
            }
            case COUNTERCLOCKWISE_90: {
                blockPos2 = blockPos.offset(n4, 0, n - n3);
            }
        }
        return blockPos2;
    }

    public BoundingBox getBoundingBox(StructurePlaceSettings structurePlaceSettings, BlockPos blockPos) {
        return this.getBoundingBox(blockPos, structurePlaceSettings.getRotation(), structurePlaceSettings.getRotationPivot(), structurePlaceSettings.getMirror());
    }

    public BoundingBox getBoundingBox(BlockPos blockPos, Rotation rotation, BlockPos blockPos2, Mirror mirror) {
        return StructureTemplate.getBoundingBox(blockPos, rotation, blockPos2, mirror, this.size);
    }

    @VisibleForTesting
    protected static BoundingBox getBoundingBox(BlockPos blockPos, Rotation rotation, BlockPos blockPos2, Mirror mirror, Vec3i vec3i) {
        Vec3i vec3i2 = vec3i.offset(-1, -1, -1);
        BlockPos blockPos3 = StructureTemplate.transform(BlockPos.ZERO, mirror, rotation, blockPos2);
        BlockPos blockPos4 = StructureTemplate.transform(BlockPos.ZERO.offset(vec3i2), mirror, rotation, blockPos2);
        return BoundingBox.fromCorners(blockPos3, blockPos4).move(blockPos);
    }

    /*
     * WARNING - void declaration
     */
    public CompoundTag save(CompoundTag compoundTag) {
        Object object;
        AbstractList abstractList;
        if (this.palettes.isEmpty()) {
            compoundTag.put(BLOCKS_TAG, new ListTag());
            compoundTag.put(PALETTE_TAG, new ListTag());
        } else {
            Object object2;
            Object object32;
            Iterator iterator;
            void object4;
            abstractList = Lists.newArrayList();
            SimplePalette simplePalette = new SimplePalette();
            abstractList.add(simplePalette);
            boolean i = true;
            while (object4 < this.palettes.size()) {
                abstractList.add(new SimplePalette());
                ++object4;
            }
            ListTag listTag = new ListTag();
            object = this.palettes.get(0).blocks();
            for (int j = 0; j < object.size(); ++j) {
                iterator = (StructureBlockInfo)object.get(j);
                object32 = new CompoundTag();
                ((CompoundTag)object32).put("pos", this.newIntegerList(((StructureBlockInfo)((Object)iterator)).pos.getX(), ((StructureBlockInfo)((Object)iterator)).pos.getY(), ((StructureBlockInfo)((Object)iterator)).pos.getZ()));
                int n = simplePalette.idFor(((StructureBlockInfo)((Object)iterator)).state);
                ((CompoundTag)object32).putInt(BLOCK_TAG_STATE, n);
                if (((StructureBlockInfo)((Object)iterator)).nbt != null) {
                    ((CompoundTag)object32).put("nbt", ((StructureBlockInfo)((Object)iterator)).nbt);
                }
                listTag.add(object32);
                for (int k = 1; k < this.palettes.size(); ++k) {
                    object2 = (SimplePalette)abstractList.get(k);
                    ((SimplePalette)object2).addMapping(this.palettes.get((int)k).blocks().get((int)j).state, n);
                }
            }
            compoundTag.put(BLOCKS_TAG, listTag);
            if (abstractList.size() == 1) {
                var6_11 = new ListTag();
                iterator = simplePalette.iterator();
                while (iterator.hasNext()) {
                    object32 = (BlockState)iterator.next();
                    var6_11.add(NbtUtils.writeBlockState((BlockState)object32));
                }
                compoundTag.put(PALETTE_TAG, var6_11);
            } else {
                var6_11 = new ListTag();
                for (Object object32 : abstractList) {
                    ListTag listTag2 = new ListTag();
                    Iterator<BlockState> iterator2 = ((SimplePalette)object32).iterator();
                    while (iterator2.hasNext()) {
                        object2 = iterator2.next();
                        listTag2.add(NbtUtils.writeBlockState((BlockState)object2));
                    }
                    var6_11.add(listTag2);
                }
                compoundTag.put(PALETTE_LIST_TAG, var6_11);
            }
        }
        abstractList = new ListTag();
        for (StructureEntityInfo structureEntityInfo : this.entityInfoList) {
            object = new CompoundTag();
            ((CompoundTag)object).put("pos", this.newDoubleList(structureEntityInfo.pos.x, structureEntityInfo.pos.y, structureEntityInfo.pos.z));
            ((CompoundTag)object).put(ENTITY_TAG_BLOCKPOS, this.newIntegerList(structureEntityInfo.blockPos.getX(), structureEntityInfo.blockPos.getY(), structureEntityInfo.blockPos.getZ()));
            if (structureEntityInfo.nbt != null) {
                ((CompoundTag)object).put("nbt", structureEntityInfo.nbt);
            }
            abstractList.add(object);
        }
        compoundTag.put(ENTITIES_TAG, (Tag)((Object)abstractList));
        compoundTag.put(SIZE_TAG, this.newIntegerList(this.size.getX(), this.size.getY(), this.size.getZ()));
        return NbtUtils.addCurrentDataVersion(compoundTag);
    }

    public void load(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
        int n;
        ListTag listTag;
        this.palettes.clear();
        this.entityInfoList.clear();
        ListTag listTag2 = compoundTag.getList(SIZE_TAG, 3);
        this.size = new Vec3i(listTag2.getInt(0), listTag2.getInt(1), listTag2.getInt(2));
        ListTag listTag3 = compoundTag.getList(BLOCKS_TAG, 10);
        if (compoundTag.contains(PALETTE_LIST_TAG, 9)) {
            listTag = compoundTag.getList(PALETTE_LIST_TAG, 9);
            for (n = 0; n < listTag.size(); ++n) {
                this.loadPalette(holderGetter, listTag.getList(n), listTag3);
            }
        } else {
            this.loadPalette(holderGetter, compoundTag.getList(PALETTE_TAG, 10), listTag3);
        }
        listTag = compoundTag.getList(ENTITIES_TAG, 10);
        for (n = 0; n < listTag.size(); ++n) {
            CompoundTag compoundTag2 = listTag.getCompound(n);
            ListTag listTag4 = compoundTag2.getList("pos", 6);
            Vec3 vec3 = new Vec3(listTag4.getDouble(0), listTag4.getDouble(1), listTag4.getDouble(2));
            ListTag listTag5 = compoundTag2.getList(ENTITY_TAG_BLOCKPOS, 3);
            BlockPos blockPos = new BlockPos(listTag5.getInt(0), listTag5.getInt(1), listTag5.getInt(2));
            if (!compoundTag2.contains("nbt")) continue;
            CompoundTag compoundTag3 = compoundTag2.getCompound("nbt");
            this.entityInfoList.add(new StructureEntityInfo(vec3, blockPos, compoundTag3));
        }
    }

    private void loadPalette(HolderGetter<Block> holderGetter, ListTag listTag, ListTag listTag2) {
        SimplePalette simplePalette = new SimplePalette();
        for (int i = 0; i < listTag.size(); ++i) {
            simplePalette.addMapping(NbtUtils.readBlockState(holderGetter, listTag.getCompound(i)), i);
        }
        ArrayList arrayList = Lists.newArrayList();
        ArrayList arrayList2 = Lists.newArrayList();
        ArrayList arrayList3 = Lists.newArrayList();
        for (int i = 0; i < listTag2.size(); ++i) {
            CompoundTag compoundTag = listTag2.getCompound(i);
            ListTag listTag3 = compoundTag.getList("pos", 3);
            BlockPos blockPos = new BlockPos(listTag3.getInt(0), listTag3.getInt(1), listTag3.getInt(2));
            BlockState blockState = simplePalette.stateFor(compoundTag.getInt(BLOCK_TAG_STATE));
            CompoundTag compoundTag2 = compoundTag.contains("nbt") ? compoundTag.getCompound("nbt") : null;
            StructureBlockInfo structureBlockInfo = new StructureBlockInfo(blockPos, blockState, compoundTag2);
            StructureTemplate.addToLists(structureBlockInfo, arrayList, arrayList2, arrayList3);
        }
        List<StructureBlockInfo> list = StructureTemplate.buildInfoList(arrayList, arrayList2, arrayList3);
        this.palettes.add(new Palette(list));
    }

    private ListTag newIntegerList(int ... nArray) {
        ListTag listTag = new ListTag();
        for (int n : nArray) {
            listTag.add(IntTag.valueOf(n));
        }
        return listTag;
    }

    private ListTag newDoubleList(double ... dArray) {
        ListTag listTag = new ListTag();
        for (double d : dArray) {
            listTag.add(DoubleTag.valueOf(d));
        }
        return listTag;
    }

    public static final class StructureBlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        @Nullable
        final CompoundTag nbt;

        public StructureBlockInfo(BlockPos blockPos, BlockState blockState, @Nullable CompoundTag compoundTag) {
            this.pos = blockPos;
            this.state = blockState;
            this.nbt = compoundTag;
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT, "<StructureBlockInfo | %s | %s | %s>", this.pos, this.state, this.nbt);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StructureBlockInfo.class, "pos;state;nbt", "pos", "state", "nbt"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StructureBlockInfo.class, "pos;state;nbt", "pos", "state", "nbt"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        @Nullable
        public CompoundTag nbt() {
            return this.nbt;
        }
    }

    public static final class Palette {
        private final List<StructureBlockInfo> blocks;
        private final Map<Block, List<StructureBlockInfo>> cache = Maps.newHashMap();

        Palette(List<StructureBlockInfo> list) {
            this.blocks = list;
        }

        public List<StructureBlockInfo> blocks() {
            return this.blocks;
        }

        public List<StructureBlockInfo> blocks(Block block2) {
            return this.cache.computeIfAbsent(block2, block -> this.blocks.stream().filter(structureBlockInfo -> structureBlockInfo.state.is((Block)block)).collect(Collectors.toList()));
        }
    }

    public static class StructureEntityInfo {
        public final Vec3 pos;
        public final BlockPos blockPos;
        public final CompoundTag nbt;

        public StructureEntityInfo(Vec3 vec3, BlockPos blockPos, CompoundTag compoundTag) {
            this.pos = vec3;
            this.blockPos = blockPos;
            this.nbt = compoundTag;
        }
    }

    static class SimplePalette
    implements Iterable<BlockState> {
        public static final BlockState DEFAULT_BLOCK_STATE = Blocks.AIR.defaultBlockState();
        private final IdMapper<BlockState> ids = new IdMapper(16);
        private int lastId;

        SimplePalette() {
        }

        public int idFor(BlockState blockState) {
            int n = this.ids.getId(blockState);
            if (n == -1) {
                n = this.lastId++;
                this.ids.addMapping(blockState, n);
            }
            return n;
        }

        @Nullable
        public BlockState stateFor(int n) {
            BlockState blockState = this.ids.byId(n);
            return blockState == null ? DEFAULT_BLOCK_STATE : blockState;
        }

        @Override
        public Iterator<BlockState> iterator() {
            return this.ids.iterator();
        }

        public void addMapping(BlockState blockState, int n) {
            this.ids.addMapping(blockState, n);
        }
    }
}

