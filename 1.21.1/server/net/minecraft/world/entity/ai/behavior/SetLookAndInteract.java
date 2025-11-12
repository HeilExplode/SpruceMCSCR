/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetLookAndInteract {
    public static BehaviorControl<LivingEntity> create(EntityType<?> entityType, int n) {
        int n2 = n * n;
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.LOOK_TARGET), instance.absent(MemoryModuleType.INTERACTION_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, livingEntity, l) -> {
            Optional<LivingEntity> optional = ((NearestVisibleLivingEntities)instance.get(memoryAccessor3)).findClosest(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)n2 && entityType.equals(livingEntity2.getType()));
            if (optional.isEmpty()) {
                return false;
            }
            LivingEntity livingEntity3 = optional.get();
            memoryAccessor2.set(livingEntity3);
            memoryAccessor.set(new EntityTracker(livingEntity3, true));
            return true;
        }));
    }
}

