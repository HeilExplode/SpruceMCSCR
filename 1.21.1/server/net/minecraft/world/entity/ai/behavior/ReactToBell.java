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
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class ReactToBell {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.HEARD_BELL_TIME)).apply((Applicative)instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
            if (raid == null) {
                livingEntity.getBrain().setActiveActivityIfPossible(Activity.HIDE);
            }
            return true;
        }));
    }
}

