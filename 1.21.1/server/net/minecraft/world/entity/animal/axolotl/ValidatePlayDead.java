/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.animal.axolotl;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class ValidatePlayDead {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.PLAY_DEAD_TICKS), instance.registered(MemoryModuleType.HURT_BY_ENTITY)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            int n = (Integer)instance.get(memoryAccessor);
            if (n <= 0) {
                memoryAccessor.erase();
                memoryAccessor2.erase();
                livingEntity.getBrain().useDefaultActivity();
            } else {
                memoryAccessor.set(n - 1);
            }
            return true;
        }));
    }
}

