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
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;

public class SetEntityLookTarget {
    public static BehaviorControl<LivingEntity> create(MobCategory mobCategory, float f) {
        return SetEntityLookTarget.create((LivingEntity livingEntity) -> mobCategory.equals(livingEntity.getType().getCategory()), f);
    }

    public static OneShot<LivingEntity> create(EntityType<?> entityType, float f) {
        return SetEntityLookTarget.create((LivingEntity livingEntity) -> entityType.equals(livingEntity.getType()), f);
    }

    public static OneShot<LivingEntity> create(float f) {
        return SetEntityLookTarget.create((LivingEntity livingEntity) -> true, f);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> predicate, float f) {
        float f2 = f * f;
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.LOOK_TARGET), instance.present(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            Optional<LivingEntity> optional = ((NearestVisibleLivingEntities)instance.get(memoryAccessor2)).findClosest(predicate.and(livingEntity2 -> livingEntity2.distanceToSqr(livingEntity) <= (double)f2 && !livingEntity.hasPassenger((Entity)livingEntity2)));
            if (optional.isEmpty()) {
                return false;
            }
            memoryAccessor.set(new EntityTracker(optional.get(), true));
            return true;
        }));
    }
}

