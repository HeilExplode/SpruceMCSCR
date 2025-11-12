/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

public class SetWalkTargetFromLookTarget {
    public static OneShot<LivingEntity> create(float f, int n) {
        return SetWalkTargetFromLookTarget.create(livingEntity -> true, livingEntity -> Float.valueOf(f), n);
    }

    public static OneShot<LivingEntity> create(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> function, int n) {
        return BehaviorBuilder.create(instance -> instance.group(instance.absent(MemoryModuleType.WALK_TARGET), instance.present(MemoryModuleType.LOOK_TARGET)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            if (!predicate.test(livingEntity)) {
                return false;
            }
            memoryAccessor.set(new WalkTarget((PositionTracker)instance.get(memoryAccessor2), ((Float)function.apply(livingEntity)).floatValue(), n));
            return true;
        }));
    }
}

