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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.Piglin;

public class StopAdmiringIfItemTooFarAway<E extends Piglin> {
    public static BehaviorControl<LivingEntity> create(int n) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ADMIRING_ITEM), instance.registered(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            if (!livingEntity.getOffhandItem().isEmpty()) {
                return false;
            }
            Optional optional = instance.tryGet(memoryAccessor2);
            if (optional.isPresent() && ((ItemEntity)optional.get()).closerThan(livingEntity, n)) {
                return false;
            }
            memoryAccessor.erase();
            return true;
        }));
    }
}

