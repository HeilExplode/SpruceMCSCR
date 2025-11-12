/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Collections;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InsideBrownianWalk {
    public static BehaviorControl<PathfinderMob> create(float f) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, pathfinderMob, l) -> {
            if (serverLevel.canSeeSky(pathfinderMob.blockPosition())) {
                return false;
            }
            BlockPos blockPos2 = pathfinderMob.blockPosition();
            List list = BlockPos.betweenClosedStream(blockPos2.offset(-1, -1, -1), blockPos2.offset(1, 1, 1)).map(BlockPos::immutable).collect(Util.toMutableList());
            Collections.shuffle(list);
            list.stream().filter(blockPos -> !serverLevel.canSeeSky((BlockPos)blockPos)).filter(blockPos -> serverLevel.loadedAndEntityCanStandOn((BlockPos)blockPos, pathfinderMob)).filter(blockPos -> serverLevel.noCollision(pathfinderMob)).findFirst().ifPresent(blockPos -> memoryAccessor.set(new WalkTarget((BlockPos)blockPos, f, 0)));
            return true;
        }));
    }
}

