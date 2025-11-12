/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level.block;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.SculkBehaviour;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class SculkSpreader {
    public static final int MAX_GROWTH_RATE_RADIUS = 24;
    public static final int MAX_CHARGE = 1000;
    public static final float MAX_DECAY_FACTOR = 0.5f;
    private static final int MAX_CURSORS = 32;
    public static final int SHRIEKER_PLACEMENT_RATE = 11;
    final boolean isWorldGeneration;
    private final TagKey<Block> replaceableBlocks;
    private final int growthSpawnCost;
    private final int noGrowthRadius;
    private final int chargeDecayRate;
    private final int additionalDecayRate;
    private List<ChargeCursor> cursors = new ArrayList<ChargeCursor>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public SculkSpreader(boolean bl, TagKey<Block> tagKey, int n, int n2, int n3, int n4) {
        this.isWorldGeneration = bl;
        this.replaceableBlocks = tagKey;
        this.growthSpawnCost = n;
        this.noGrowthRadius = n2;
        this.chargeDecayRate = n3;
        this.additionalDecayRate = n4;
    }

    public static SculkSpreader createLevelSpreader() {
        return new SculkSpreader(false, BlockTags.SCULK_REPLACEABLE, 10, 4, 10, 5);
    }

    public static SculkSpreader createWorldGenSpreader() {
        return new SculkSpreader(true, BlockTags.SCULK_REPLACEABLE_WORLD_GEN, 50, 1, 5, 10);
    }

    public TagKey<Block> replaceableBlocks() {
        return this.replaceableBlocks;
    }

    public int growthSpawnCost() {
        return this.growthSpawnCost;
    }

    public int noGrowthRadius() {
        return this.noGrowthRadius;
    }

    public int chargeDecayRate() {
        return this.chargeDecayRate;
    }

    public int additionalDecayRate() {
        return this.additionalDecayRate;
    }

    public boolean isWorldGeneration() {
        return this.isWorldGeneration;
    }

    @VisibleForTesting
    public List<ChargeCursor> getCursors() {
        return this.cursors;
    }

    public void clear() {
        this.cursors.clear();
    }

    public void load(CompoundTag compoundTag) {
        if (compoundTag.contains("cursors", 9)) {
            this.cursors.clear();
            List list = ChargeCursor.CODEC.listOf().parse(new Dynamic((DynamicOps)NbtOps.INSTANCE, (Object)compoundTag.getList("cursors", 10))).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElseGet(ArrayList::new);
            int n = Math.min(list.size(), 32);
            for (int i = 0; i < n; ++i) {
                this.addCursor((ChargeCursor)list.get(i));
            }
        }
    }

    public void save(CompoundTag compoundTag) {
        ChargeCursor.CODEC.listOf().encodeStart((DynamicOps)NbtOps.INSTANCE, this.cursors).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(tag -> compoundTag.put("cursors", (Tag)tag));
    }

    public void addCursors(BlockPos blockPos, int n) {
        while (n > 0) {
            int n2 = Math.min(n, 1000);
            this.addCursor(new ChargeCursor(blockPos, n2));
            n -= n2;
        }
    }

    private void addCursor(ChargeCursor chargeCursor) {
        if (this.cursors.size() >= 32) {
            return;
        }
        this.cursors.add(chargeCursor);
    }

    public void updateCursors(LevelAccessor levelAccessor, BlockPos blockPos2, RandomSource randomSource, boolean bl) {
        BlockPos blockPos3;
        if (this.cursors.isEmpty()) {
            return;
        }
        ArrayList<ChargeCursor> arrayList = new ArrayList<ChargeCursor>();
        HashMap<BlockPos, ChargeCursor> hashMap = new HashMap<BlockPos, ChargeCursor>();
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (ChargeCursor chargeCursor : this.cursors) {
            chargeCursor.update(levelAccessor, blockPos2, randomSource, this, bl);
            if (chargeCursor.charge <= 0) {
                levelAccessor.levelEvent(3006, chargeCursor.getPos(), 0);
                continue;
            }
            blockPos3 = chargeCursor.getPos();
            object2IntOpenHashMap.computeInt((Object)blockPos3, (blockPos, n) -> (n == null ? 0 : n) + chargeCursor.charge);
            ChargeCursor chargeCursor2 = (ChargeCursor)hashMap.get(blockPos3);
            if (chargeCursor2 == null) {
                hashMap.put(blockPos3, chargeCursor);
                arrayList.add(chargeCursor);
                continue;
            }
            if (!this.isWorldGeneration() && chargeCursor.charge + chargeCursor2.charge <= 1000) {
                chargeCursor2.mergeWith(chargeCursor);
                continue;
            }
            arrayList.add(chargeCursor);
            if (chargeCursor.charge >= chargeCursor2.charge) continue;
            hashMap.put(blockPos3, chargeCursor);
        }
        for (ChargeCursor chargeCursor : object2IntOpenHashMap.object2IntEntrySet()) {
            Set<Direction> set;
            blockPos3 = (BlockPos)chargeCursor.getKey();
            int n2 = chargeCursor.getIntValue();
            ChargeCursor chargeCursor3 = (ChargeCursor)hashMap.get(blockPos3);
            Set<Direction> set2 = set = chargeCursor3 == null ? null : chargeCursor3.getFacingData();
            if (n2 <= 0 || set == null) continue;
            int n3 = (int)(Math.log1p(n2) / (double)2.3f) + 1;
            int n4 = (n3 << 6) + MultifaceBlock.pack(set);
            levelAccessor.levelEvent(3006, blockPos3, n4);
        }
        this.cursors = arrayList;
    }

    private static /* synthetic */ Integer lambda$save$1(ChargeCursor chargeCursor) {
        return 1;
    }

    public static class ChargeCursor {
        private static final ObjectArrayList<Vec3i> NON_CORNER_NEIGHBOURS = Util.make(new ObjectArrayList(18), objectArrayList -> BlockPos.betweenClosedStream(new BlockPos(-1, -1, -1), new BlockPos(1, 1, 1)).filter(blockPos -> (blockPos.getX() == 0 || blockPos.getY() == 0 || blockPos.getZ() == 0) && !blockPos.equals(BlockPos.ZERO)).map(BlockPos::immutable).forEach(arg_0 -> ((ObjectArrayList)objectArrayList).add(arg_0)));
        public static final int MAX_CURSOR_DECAY_DELAY = 1;
        private BlockPos pos;
        int charge;
        private int updateDelay;
        private int decayDelay;
        @Nullable
        private Set<Direction> facings;
        private static final Codec<Set<Direction>> DIRECTION_SET = Direction.CODEC.listOf().xmap(list -> Sets.newEnumSet((Iterable)list, Direction.class), Lists::newArrayList);
        public static final Codec<ChargeCursor> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockPos.CODEC.fieldOf("pos").forGetter(ChargeCursor::getPos), (App)Codec.intRange((int)0, (int)1000).fieldOf("charge").orElse((Object)0).forGetter(ChargeCursor::getCharge), (App)Codec.intRange((int)0, (int)1).fieldOf("decay_delay").orElse((Object)1).forGetter(ChargeCursor::getDecayDelay), (App)Codec.intRange((int)0, (int)Integer.MAX_VALUE).fieldOf("update_delay").orElse((Object)0).forGetter(chargeCursor -> chargeCursor.updateDelay), (App)DIRECTION_SET.lenientOptionalFieldOf("facings").forGetter(chargeCursor -> Optional.ofNullable(chargeCursor.getFacingData()))).apply((Applicative)instance, ChargeCursor::new));

        private ChargeCursor(BlockPos blockPos, int n, int n2, int n3, Optional<Set<Direction>> optional) {
            this.pos = blockPos;
            this.charge = n;
            this.decayDelay = n2;
            this.updateDelay = n3;
            this.facings = optional.orElse(null);
        }

        public ChargeCursor(BlockPos blockPos, int n) {
            this(blockPos, n, 1, 0, Optional.empty());
        }

        public BlockPos getPos() {
            return this.pos;
        }

        public int getCharge() {
            return this.charge;
        }

        public int getDecayDelay() {
            return this.decayDelay;
        }

        @Nullable
        public Set<Direction> getFacingData() {
            return this.facings;
        }

        private boolean shouldUpdate(LevelAccessor levelAccessor, BlockPos blockPos, boolean bl) {
            if (this.charge <= 0) {
                return false;
            }
            if (bl) {
                return true;
            }
            if (levelAccessor instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)levelAccessor;
                return serverLevel.shouldTickBlocksAt(blockPos);
            }
            return false;
        }

        public void update(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource, SculkSpreader sculkSpreader, boolean bl) {
            if (!this.shouldUpdate(levelAccessor, blockPos, sculkSpreader.isWorldGeneration)) {
                return;
            }
            if (this.updateDelay > 0) {
                --this.updateDelay;
                return;
            }
            BlockState blockState = levelAccessor.getBlockState(this.pos);
            SculkBehaviour sculkBehaviour = ChargeCursor.getBlockBehaviour(blockState);
            if (bl && sculkBehaviour.attemptSpreadVein(levelAccessor, this.pos, blockState, this.facings, sculkSpreader.isWorldGeneration())) {
                if (sculkBehaviour.canChangeBlockStateOnSpread()) {
                    blockState = levelAccessor.getBlockState(this.pos);
                    sculkBehaviour = ChargeCursor.getBlockBehaviour(blockState);
                }
                levelAccessor.playSound(null, this.pos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            this.charge = sculkBehaviour.attemptUseCharge(this, levelAccessor, blockPos, randomSource, sculkSpreader, bl);
            if (this.charge <= 0) {
                sculkBehaviour.onDischarged(levelAccessor, blockState, this.pos, randomSource);
                return;
            }
            BlockPos blockPos2 = ChargeCursor.getValidMovementPos(levelAccessor, this.pos, randomSource);
            if (blockPos2 != null) {
                sculkBehaviour.onDischarged(levelAccessor, blockState, this.pos, randomSource);
                this.pos = blockPos2.immutable();
                if (sculkSpreader.isWorldGeneration() && !this.pos.closerThan(new Vec3i(blockPos.getX(), this.pos.getY(), blockPos.getZ()), 15.0)) {
                    this.charge = 0;
                    return;
                }
                blockState = levelAccessor.getBlockState(blockPos2);
            }
            if (blockState.getBlock() instanceof SculkBehaviour) {
                this.facings = MultifaceBlock.availableFaces(blockState);
            }
            this.decayDelay = sculkBehaviour.updateDecayDelay(this.decayDelay);
            this.updateDelay = sculkBehaviour.getSculkSpreadDelay();
        }

        void mergeWith(ChargeCursor chargeCursor) {
            this.charge += chargeCursor.charge;
            chargeCursor.charge = 0;
            this.updateDelay = Math.min(this.updateDelay, chargeCursor.updateDelay);
        }

        private static SculkBehaviour getBlockBehaviour(BlockState blockState) {
            SculkBehaviour sculkBehaviour;
            Block block = blockState.getBlock();
            return block instanceof SculkBehaviour ? (sculkBehaviour = (SculkBehaviour)((Object)block)) : SculkBehaviour.DEFAULT;
        }

        private static List<Vec3i> getRandomizedNonCornerNeighbourOffsets(RandomSource randomSource) {
            return Util.shuffledCopy(NON_CORNER_NEIGHBOURS, randomSource);
        }

        @Nullable
        private static BlockPos getValidMovementPos(LevelAccessor levelAccessor, BlockPos blockPos, RandomSource randomSource) {
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
            BlockPos.MutableBlockPos mutableBlockPos2 = blockPos.mutable();
            for (Vec3i vec3i : ChargeCursor.getRandomizedNonCornerNeighbourOffsets(randomSource)) {
                mutableBlockPos2.setWithOffset((Vec3i)blockPos, vec3i);
                BlockState blockState = levelAccessor.getBlockState(mutableBlockPos2);
                if (!(blockState.getBlock() instanceof SculkBehaviour) || !ChargeCursor.isMovementUnobstructed(levelAccessor, blockPos, mutableBlockPos2)) continue;
                mutableBlockPos.set(mutableBlockPos2);
                if (!SculkVeinBlock.hasSubstrateAccess(levelAccessor, blockState, mutableBlockPos2)) continue;
                break;
            }
            return mutableBlockPos.equals(blockPos) ? null : mutableBlockPos;
        }

        private static boolean isMovementUnobstructed(LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
            if (blockPos.distManhattan(blockPos2) == 1) {
                return true;
            }
            BlockPos blockPos3 = blockPos2.subtract(blockPos);
            Direction direction = Direction.fromAxisAndDirection(Direction.Axis.X, blockPos3.getX() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction2 = Direction.fromAxisAndDirection(Direction.Axis.Y, blockPos3.getY() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            Direction direction3 = Direction.fromAxisAndDirection(Direction.Axis.Z, blockPos3.getZ() < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            if (blockPos3.getX() == 0) {
                return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction2) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction3);
            }
            if (blockPos3.getY() == 0) {
                return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction3);
            }
            return ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction) || ChargeCursor.isUnobstructed(levelAccessor, blockPos, direction2);
        }

        private static boolean isUnobstructed(LevelAccessor levelAccessor, BlockPos blockPos, Direction direction) {
            BlockPos blockPos2 = blockPos.relative(direction);
            return !levelAccessor.getBlockState(blockPos2).isFaceSturdy(levelAccessor, blockPos2, direction.getOpposite());
        }
    }
}

