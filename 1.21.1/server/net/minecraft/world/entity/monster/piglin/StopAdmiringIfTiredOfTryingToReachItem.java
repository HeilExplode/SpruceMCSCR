/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class StopAdmiringIfTiredOfTryingToReachItem {
    public static BehaviorControl<LivingEntity> create(int n, int n2) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ADMIRING_ITEM), instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), instance.registered(MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM), instance.registered(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
            if (!livingEntity.getOffhandItem().isEmpty()) {
                return false;
            }
            Optional optional = instance.tryGet(memoryAccessor3);
            if (optional.isEmpty()) {
                memoryAccessor3.set(0);
            } else {
                int n3 = (Integer)optional.get();
                if (n3 > n) {
                    memoryAccessor.erase();
                    memoryAccessor3.erase();
                    memoryAccessor4.setWithExpiry(true, n2);
                } else {
                    memoryAccessor3.set(n3 + 1);
                }
            }
            return true;
        }));
    }
}

