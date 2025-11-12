/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.BiPredicate;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.GameRules;

public class StartCelebratingIfTargetDead {
    public static BehaviorControl<LivingEntity> create(int n, BiPredicate<LivingEntity, LivingEntity> biPredicate) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.ANGRY_AT), instance.absent(MemoryModuleType.CELEBRATE_LOCATION), instance.registered(MemoryModuleType.DANCING)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
            LivingEntity livingEntity2 = (LivingEntity)instance.get(memoryAccessor);
            if (!livingEntity2.isDeadOrDying()) {
                return false;
            }
            if (biPredicate.test(livingEntity, livingEntity2)) {
                memoryAccessor4.setWithExpiry(true, n);
            }
            memoryAccessor3.setWithExpiry(livingEntity2.blockPosition(), n);
            if (livingEntity2.getType() != EntityType.PLAYER || serverLevel.getGameRules().getBoolean(GameRules.RULE_FORGIVE_DEAD_PLAYERS)) {
                memoryAccessor.erase();
                memoryAccessor2.erase();
            }
            return true;
        }));
    }
}

