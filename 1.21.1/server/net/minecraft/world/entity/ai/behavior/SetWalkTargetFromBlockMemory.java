/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class SetWalkTargetFromBlockMemory {
    public static OneShot<Villager> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int n, int n2, int n3) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, villager, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor3);
            Optional optional = instance.tryGet(memoryAccessor);
            if (globalPos.dimension() != serverLevel.dimension() || optional.isPresent() && serverLevel.getGameTime() - (Long)optional.get() > (long)n3) {
                villager.releasePoi(memoryModuleType);
                memoryAccessor3.erase();
                memoryAccessor.set(l);
            } else if (globalPos.pos().distManhattan(villager.blockPosition()) > n2) {
                Vec3 vec3 = null;
                int n4 = 0;
                int n5 = 1000;
                while (vec3 == null || BlockPos.containing(vec3).distManhattan(villager.blockPosition()) > n2) {
                    vec3 = DefaultRandomPos.getPosTowards(villager, 15, 7, Vec3.atBottomCenterOf(globalPos.pos()), 1.5707963705062866);
                    if (++n4 != 1000) continue;
                    villager.releasePoi(memoryModuleType);
                    memoryAccessor3.erase();
                    memoryAccessor.set(l);
                    return true;
                }
                memoryAccessor2.set(new WalkTarget(vec3, f, n));
            } else if (globalPos.pos().distManhattan(villager.blockPosition()) > n) {
                memoryAccessor2.set(new WalkTarget(globalPos.pos(), f, n));
            }
            return true;
        }));
    }
}

