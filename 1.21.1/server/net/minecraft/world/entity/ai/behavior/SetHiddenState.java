/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.apache.commons.lang3.mutable.MutableInt;

public class SetHiddenState {
    private static final int HIDE_TIMEOUT = 300;

    public static BehaviorControl<LivingEntity> create(int n, int n2) {
        int n3 = n * 20;
        MutableInt mutableInt = new MutableInt(0);
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.HIDING_PLACE), instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            boolean bl;
            long l2 = (Long)instance.get(memoryAccessor2);
            boolean bl2 = bl = l2 + 300L <= l;
            if (mutableInt.getValue() > n3 || bl) {
                memoryAccessor2.erase();
                memoryAccessor.erase();
                livingEntity.getBrain().updateActivityFromSchedule(serverLevel.getDayTime(), serverLevel.getGameTime());
                mutableInt.setValue(0);
                return true;
            }
            BlockPos blockPos = ((GlobalPos)instance.get(memoryAccessor)).pos();
            if (blockPos.closerThan(livingEntity.blockPosition(), n2)) {
                mutableInt.increment();
            }
            return true;
        }));
    }
}

