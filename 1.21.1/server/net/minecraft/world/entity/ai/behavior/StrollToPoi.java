/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import org.apache.commons.lang3.mutable.MutableLong;

public class StrollToPoi {
    public static BehaviorControl<PathfinderMob> create(MemoryModuleType<GlobalPos> memoryModuleType, float f, int n, int n2) {
        MutableLong mutableLong = new MutableLong(0L);
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.WALK_TARGET), instance.present(memoryModuleType)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, pathfinderMob, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor2);
            if (serverLevel.dimension() != globalPos.dimension() || !globalPos.pos().closerToCenterThan(pathfinderMob.position(), n2)) {
                return false;
            }
            if (l <= mutableLong.getValue()) {
                return true;
            }
            memoryAccessor.set(new WalkTarget(globalPos.pos(), f, n));
            mutableLong.setValue(l + 80L);
            return true;
        }));
    }
}

