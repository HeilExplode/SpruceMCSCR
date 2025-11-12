/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class GoToTargetLocation {
    private static BlockPos getNearbyPos(Mob mob, BlockPos blockPos) {
        RandomSource randomSource = mob.level().random;
        return blockPos.offset(GoToTargetLocation.getRandomOffset(randomSource), 0, GoToTargetLocation.getRandomOffset(randomSource));
    }

    private static int getRandomOffset(RandomSource randomSource) {
        return randomSource.nextInt(3) - 1;
    }

    public static <E extends Mob> OneShot<E> create(MemoryModuleType<BlockPos> memoryModuleType, int n, float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(memoryModuleType), instance.absent(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.registered(MemoryModuleType.LOOK_TARGET)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, mob, l) -> {
            BlockPos blockPos = (BlockPos)instance.get(memoryAccessor);
            boolean bl = blockPos.closerThan(mob.blockPosition(), n);
            if (!bl) {
                BehaviorUtils.setWalkAndLookTargetMemories(mob, GoToTargetLocation.getNearbyPos(mob, blockPos), f, n);
            }
            return true;
        }));
    }
}

