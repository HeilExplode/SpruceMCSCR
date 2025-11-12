/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class InteractWith {
    public static <T extends LivingEntity> BehaviorControl<LivingEntity> of(EntityType<? extends T> entityType, int n, MemoryModuleType<T> memoryModuleType, float f, int n2) {
        return InteractWith.of(entityType, n, livingEntity -> true, livingEntity -> true, memoryModuleType, f, n2);
    }

    public static <E extends LivingEntity, T extends LivingEntity> BehaviorControl<E> of(EntityType<? extends T> entityType, int n, Predicate<E> predicate, Predicate<T> predicate2, MemoryModuleType<T> memoryModuleType, float f, int n2) {
        int n3 = n * n;
        Predicate<LivingEntity> predicate3 = livingEntity -> entityType.equals(livingEntity.getType()) && predicate2.test(livingEntity);
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(memoryModuleType), instance.registered(MemoryModuleType.LOOK_TARGET), instance.absent(MemoryModuleType.WALK_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity3, l) -> {
            NearestVisibleLivingEntities nearestVisibleLivingEntities = (NearestVisibleLivingEntities)instance.get(memoryAccessor4);
            if (predicate.test(livingEntity3) && nearestVisibleLivingEntities.contains(predicate3)) {
                Optional<LivingEntity> optional = nearestVisibleLivingEntities.findClosest(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity3) <= (double)n3 && predicate3.test((LivingEntity)livingEntity2));
                optional.ifPresent(livingEntity -> {
                    memoryAccessor.set(livingEntity);
                    memoryAccessor2.set(new EntityTracker((Entity)livingEntity, true));
                    memoryAccessor3.set(new WalkTarget(new EntityTracker((Entity)livingEntity, false), f, n2));
                });
                return true;
            }
            return false;
        }));
    }
}

