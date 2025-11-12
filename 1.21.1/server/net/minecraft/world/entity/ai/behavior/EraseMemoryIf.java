/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class EraseMemoryIf {
    public static <E extends LivingEntity> BehaviorControl<E> create(Predicate<E> predicate, MemoryModuleType<?> memoryModuleType) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(memoryModuleType)).apply((Applicative)instance, memoryAccessor -> (serverLevel, livingEntity, l) -> {
            if (predicate.test(livingEntity)) {
                memoryAccessor.erase();
                return true;
            }
            return false;
        }));
    }
}

