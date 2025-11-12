/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior.warden;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.warden.Warden;

public class SetRoarTarget {
    public static <E extends Warden> BehaviorControl<E> create(Function<E, Optional<? extends LivingEntity>> function) {
        return BehaviorBuilder.create((BehaviorBuilder.Instance<E> instance) -> instance.group(instance.absent(MemoryModuleType.ROAR_TARGET), instance.absent(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3) -> (serverLevel, warden, l) -> {
            Optional optional = (Optional)function.apply(warden);
            if (optional.filter(warden::canTargetEntity).isEmpty()) {
                return false;
            }
            memoryAccessor.set((LivingEntity)optional.get());
            memoryAccessor3.erase();
            return true;
        }));
    }
}

