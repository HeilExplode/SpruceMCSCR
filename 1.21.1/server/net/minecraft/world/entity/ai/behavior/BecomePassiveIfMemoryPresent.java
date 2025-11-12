/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BecomePassiveIfMemoryPresent {
    public static BehaviorControl<LivingEntity> create(MemoryModuleType<?> memoryModuleType, int n) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.ATTACK_TARGET), instance.absent(MemoryModuleType.PACIFIED), instance.present(memoryModuleType)).apply((Applicative)instance, instance.point(() -> "[BecomePassive if " + String.valueOf(memoryModuleType) + " present]", (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            memoryAccessor2.setWithExpiry(true, n);
            memoryAccessor.erase();
            return true;
        })));
    }
}

