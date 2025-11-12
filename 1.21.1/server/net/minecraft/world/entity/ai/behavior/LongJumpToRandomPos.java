/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.LongJumpUtil;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class LongJumpToRandomPos<E extends Mob>
extends Behavior<E> {
    protected static final int FIND_JUMP_TRIES = 20;
    private static final int PREPARE_JUMP_DURATION = 40;
    protected static final int MIN_PATHFIND_DISTANCE_TO_VALID_JUMP = 8;
    private static final int TIME_OUT_DURATION = 200;
    private static final List<Integer> ALLOWED_ANGLES = Lists.newArrayList((Object[])new Integer[]{65, 70, 75, 80});
    private final UniformInt timeBetweenLongJumps;
    protected final int maxLongJumpHeight;
    protected final int maxLongJumpWidth;
    protected final float maxJumpVelocityMultiplier;
    protected List<PossibleJump> jumpCandidates = Lists.newArrayList();
    protected Optional<Vec3> initialPosition = Optional.empty();
    @Nullable
    protected Vec3 chosenJump;
    protected int findJumpTries;
    protected long prepareJumpStart;
    private final Function<E, SoundEvent> getJumpSound;
    private final BiPredicate<E, BlockPos> acceptableLandingSpot;

    public LongJumpToRandomPos(UniformInt uniformInt, int n, int n2, float f, Function<E, SoundEvent> function) {
        this(uniformInt, n, n2, f, function, LongJumpToRandomPos::defaultAcceptableLandingSpot);
    }

    public static <E extends Mob> boolean defaultAcceptableLandingSpot(E e, BlockPos blockPos) {
        BlockPos blockPos2;
        Level level = e.level();
        return level.getBlockState(blockPos2 = blockPos.below()).isSolidRender(level, blockPos2) && e.getPathfindingMalus(WalkNodeEvaluator.getPathTypeStatic(e, blockPos)) == 0.0f;
    }

    public LongJumpToRandomPos(UniformInt uniformInt, int n, int n2, float f, Function<E, SoundEvent> function, BiPredicate<E, BlockPos> biPredicate) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of(MemoryModuleType.LOOK_TARGET, (Object)((Object)MemoryStatus.REGISTERED), MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, (Object)((Object)MemoryStatus.VALUE_ABSENT), MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryStatus.VALUE_ABSENT)), 200);
        this.timeBetweenLongJumps = uniformInt;
        this.maxLongJumpHeight = n;
        this.maxLongJumpWidth = n2;
        this.maxJumpVelocityMultiplier = f;
        this.getJumpSound = function;
        this.acceptableLandingSpot = biPredicate;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        boolean bl;
        boolean bl2 = bl = mob.onGround() && !mob.isInWater() && !mob.isInLava() && !serverLevel.getBlockState(mob.blockPosition()).is(Blocks.HONEY_BLOCK);
        if (!bl) {
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
        }
        return bl;
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        boolean bl;
        boolean bl2 = bl = this.initialPosition.isPresent() && this.initialPosition.get().equals(mob.position()) && this.findJumpTries > 0 && !mob.isInWaterOrBubble() && (this.chosenJump != null || !this.jumpCandidates.isEmpty());
        if (!bl && mob.getBrain().getMemory(MemoryModuleType.LONG_JUMP_MID_JUMP).isEmpty()) {
            mob.getBrain().setMemory(MemoryModuleType.LONG_JUMP_COOLDOWN_TICKS, this.timeBetweenLongJumps.sample(serverLevel.random) / 2);
            mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        }
        return bl;
    }

    @Override
    protected void start(ServerLevel serverLevel, E e, long l) {
        this.chosenJump = null;
        this.findJumpTries = 20;
        this.initialPosition = Optional.of(((Entity)e).position());
        BlockPos blockPos = ((Entity)e).blockPosition();
        int n = blockPos.getX();
        int n2 = blockPos.getY();
        int n3 = blockPos.getZ();
        this.jumpCandidates = BlockPos.betweenClosedStream(n - this.maxLongJumpWidth, n2 - this.maxLongJumpHeight, n3 - this.maxLongJumpWidth, n + this.maxLongJumpWidth, n2 + this.maxLongJumpHeight, n3 + this.maxLongJumpWidth).filter(blockPos2 -> !blockPos2.equals(blockPos)).map(blockPos2 -> new PossibleJump(blockPos2.immutable(), Mth.ceil(blockPos.distSqr((Vec3i)blockPos2)))).collect(Collectors.toCollection(Lists::newArrayList));
    }

    @Override
    protected void tick(ServerLevel serverLevel, E e, long l) {
        if (this.chosenJump != null) {
            if (l - this.prepareJumpStart >= 40L) {
                ((Entity)e).setYRot(((Mob)e).yBodyRot);
                ((LivingEntity)e).setDiscardFriction(true);
                double d = this.chosenJump.length();
                double d2 = d + (double)((LivingEntity)e).getJumpBoostPower();
                ((Entity)e).setDeltaMovement(this.chosenJump.scale(d2 / d));
                ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.LONG_JUMP_MID_JUMP, true);
                serverLevel.playSound(null, (Entity)e, this.getJumpSound.apply(e), SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        } else {
            --this.findJumpTries;
            this.pickCandidate(serverLevel, e, l);
        }
    }

    protected void pickCandidate(ServerLevel serverLevel, E e, long l) {
        while (!this.jumpCandidates.isEmpty()) {
            Vec3 vec3;
            Vec3 vec32;
            PossibleJump possibleJump;
            BlockPos blockPos;
            Optional<PossibleJump> optional = this.getJumpCandidate(serverLevel);
            if (optional.isEmpty() || !this.isAcceptableLandingPosition(serverLevel, e, blockPos = (possibleJump = optional.get()).getJumpTarget()) || (vec32 = this.calculateOptimalJumpVector((Mob)e, vec3 = Vec3.atCenterOf(blockPos))) == null) continue;
            ((LivingEntity)e).getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new BlockPosTracker(blockPos));
            PathNavigation pathNavigation = ((Mob)e).getNavigation();
            Path path = pathNavigation.createPath(blockPos, 0, 8);
            if (path != null && path.canReach()) continue;
            this.chosenJump = vec32;
            this.prepareJumpStart = l;
            return;
        }
    }

    protected Optional<PossibleJump> getJumpCandidate(ServerLevel serverLevel) {
        Optional<PossibleJump> optional = WeightedRandom.getRandomItem(serverLevel.random, this.jumpCandidates);
        optional.ifPresent(this.jumpCandidates::remove);
        return optional;
    }

    private boolean isAcceptableLandingPosition(ServerLevel serverLevel, E e, BlockPos blockPos) {
        BlockPos blockPos2 = ((Entity)e).blockPosition();
        int n = blockPos2.getX();
        int n2 = blockPos2.getZ();
        if (n == blockPos.getX() && n2 == blockPos.getZ()) {
            return false;
        }
        return this.acceptableLandingSpot.test(e, blockPos);
    }

    @Nullable
    protected Vec3 calculateOptimalJumpVector(Mob mob, Vec3 vec3) {
        ArrayList arrayList = Lists.newArrayList(ALLOWED_ANGLES);
        Collections.shuffle(arrayList);
        float f = (float)(mob.getAttributeValue(Attributes.JUMP_STRENGTH) * (double)this.maxJumpVelocityMultiplier);
        Iterator iterator = arrayList.iterator();
        while (iterator.hasNext()) {
            int n = (Integer)iterator.next();
            Optional<Vec3> optional = LongJumpUtil.calculateJumpVectorForAngle(mob, vec3, f, n, true);
            if (!optional.isPresent()) continue;
            return optional.get();
        }
        return null;
    }

    @Override
    protected /* synthetic */ boolean canStillUse(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        return this.canStillUse(serverLevel, (Mob)livingEntity, l);
    }

    @Override
    protected /* synthetic */ void start(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.start(serverLevel, (E)((Mob)livingEntity), l);
    }

    public static class PossibleJump
    extends WeightedEntry.IntrusiveBase {
        private final BlockPos jumpTarget;

        public PossibleJump(BlockPos blockPos, int n) {
            super(n);
            this.jumpTarget = blockPos;
        }

        public BlockPos getJumpTarget() {
            return this.jumpTarget;
        }
    }
}

