/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.BiPredicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class DismountOrSkipMounting {
    public static <E extends LivingEntity> BehaviorControl<E> create(int n, BiPredicate<E, Entity> biPredicate) {
        return BehaviorBuilder.create(instance -> instance.group(instance.registered(MemoryModuleType.RIDE_TARGET)).apply((Applicative)instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            Entity entity;
            Entity entity2 = livingEntity.getVehicle();
            Entity entity3 = instance.tryGet(memoryAccessor).orElse(null);
            if (entity2 == null && entity3 == null) {
                return false;
            }
            Entity entity4 = entity = entity2 == null ? entity3 : entity2;
            if (!DismountOrSkipMounting.isVehicleValid(livingEntity, entity, n) || biPredicate.test(livingEntity, entity)) {
                livingEntity.stopRiding();
                memoryAccessor.erase();
                return true;
            }
            return false;
        }));
    }

    private static boolean isVehicleValid(LivingEntity livingEntity, Entity entity, int n) {
        return entity.isAlive() && entity.closerThan(livingEntity, n) && entity.level() == livingEntity.level();
    }
}

