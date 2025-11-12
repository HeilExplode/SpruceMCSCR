/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.apache.commons.lang3.mutable.MutableLong;

public class TryFindLandNearWater {
    public static BehaviorControl<PathfinderMob> create(int n, float f) {
        MutableLong mutableLong = new MutableLong(0L);
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, pathfinderMob, l) -> {
            if (serverLevel.getFluidState(pathfinderMob.blockPosition()).is(FluidTags.WATER)) {
                return false;
            }
            if (l < mutableLong.getValue()) {
                mutableLong.setValue(l + 40L);
                return true;
            }
            CollisionContext collisionContext = CollisionContext.of(pathfinderMob);
            BlockPos blockPos = pathfinderMob.blockPosition();
            BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
            block0: for (BlockPos blockPos2 : BlockPos.withinManhattan(blockPos, n, n, n)) {
                if (blockPos2.getX() == blockPos.getX() && blockPos2.getZ() == blockPos.getZ() || !serverLevel.getBlockState(blockPos2).getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty() || serverLevel.getBlockState(mutableBlockPos.setWithOffset((Vec3i)blockPos2, Direction.DOWN)).getCollisionShape(serverLevel, blockPos2, collisionContext).isEmpty()) continue;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    mutableBlockPos.setWithOffset((Vec3i)blockPos2, direction);
                    if (!serverLevel.getBlockState(mutableBlockPos).isAir() || !serverLevel.getBlockState(mutableBlockPos.move(Direction.DOWN)).is(Blocks.WATER)) continue;
                    memoryAccessor3.set(new BlockPosTracker(blockPos2));
                    memoryAccessor2.set(new WalkTarget(new BlockPosTracker(blockPos2), f, 0));
                    break block0;
                }
            }
            mutableLong.setValue(l + 40L);
            return true;
        }));
    }
}

